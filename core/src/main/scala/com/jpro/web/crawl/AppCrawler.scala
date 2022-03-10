package com.jpro.web.crawl

import com.jpro.web._
import simplefx.all._
import simplefx.core._
import simplefx.experimental._

import java.util.function.Supplier
import scala.collection.JavaConverters.asScalaBufferConverter

object AppCrawler {
  case class LinkInfo(url: String, description: String)

  case class ImageInfo(url: String, description: String)

  case class CrawlReportPage(path: String, links: List[LinkInfo], pictures: List[ImageInfo], title: String, description: String)

  case class CrawlReportApp(pages: List[String], reports: List[CrawlReportPage], deadLinks: List[String])

  def crawlPage(page: View): CrawlReportPage = {
    var foundLinks: List[LinkInfo] = Nil
    var images: List[ImageInfo] = Nil

    var visitedNodes: Set[Node] = Set()

    def crawlNode(x: Node): Unit = {
      if (visitedNodes.contains(x)) return
      visitedNodes += x
      if (x.getProperties.containsKey("link")) {
        val link = x.getProperties.get("link").asInstanceOf[String]
        var desc = x.getProperties.get("description").asInstanceOf[String]
        if(desc == null) desc = ""
        foundLinks ::= LinkInfo(link, desc)
      }

      if (x.isInstanceOf[Parent]) {
        x.asInstanceOf[Parent].childrenUnmodifiable.map(x => crawlNode(x))
      }
      if (x.isInstanceOf[Region]) {
        val region = x.asInstanceOf[Region]
        var rimages = List.empty[Image]
        if(region.border != null) rimages :::= region.border.getImages.asScala.map(_.getImage).toList
        if(region.background != null) rimages :::= region.background.getImages.asScala.map(_.getImage).toList
        rimages.foreach{ image =>
          images ::= ImageInfo(getImageURL(image),region.accessibleRoleDescription)
        }
      }
      if(x.isInstanceOf[ImageView]) {
        val view = x.asInstanceOf[ImageView]
        val url = getImageURL(view.image)
        val description = view.accessibleRoleDescription
        images ::= ImageInfo(url,description)
      }
    }

    crawlNode(page.realContent)

    CrawlReportPage(page.url, foundLinks.reverse, images.reverse, page.title, page.description)
  }

  def crawlApp(prefix: String, createApp: Supplier[WebApp]): CrawlReportApp = {
    var toIndex = Set[String]("/")
    var indexed = Set[String]()
    var redirects = Set[String]()
    var emptyLinks = Set[String]()
    var reports: List[CrawlReportPage] = List()

    while (!toIndex.isEmpty) {
      val crawlNext = toIndex.head
      toIndex -= crawlNext
      indexed += crawlNext
      val result = inFX {
        createApp.get().route.lift(crawlNext).getOrElse(FXFuture(null))
      }.await
      result match {
        case Redirect(url) =>
          redirects += crawlNext
          if (!indexed.contains(url) && !toIndex.contains(url)) {
            toIndex += url
          }
        case view: View =>
          view.url = crawlNext
          val newReport = inFX(crawlPage(view))
          reports = newReport :: reports
          def simplifyLink(x: String) = {
            if(x.startsWith(prefix)) x.drop(prefix.length) else x
          }
          newReport.links.filter(x => x.url.startsWith(prefix) || !x.url.startsWith("http")).map { link =>
            val url = simplifyLink(link.url)
            if (!indexed.contains(url) && !toIndex.contains(url)) {
              toIndex += url
            }
          }
        case null =>
          emptyLinks += crawlNext
      }
    }

    CrawlReportApp((indexed -- redirects -- emptyLinks).toList, reports.reverse, emptyLinks.toList)
  }

  def getImageURL(x: Image): String = {
    val url = simplifyURL(x.getUrl())
    if(url.startsWith("http")) {
      url
    } else {
      "/app/default/resourcesencoded/" + url
    }
  }


  private val cpTriggers = List[String]("jar!","classes", "main")
  def simplifyURL(x: String): String = {
    cpTriggers.collectFirst{
      Function.unlift{ (cpTrigger: String) =>
        val split = x.split(cpTrigger)
        if(split.length > 1) {
          val cp = split.last
          val idea = "cp://" + cp
          val url = classOf[javafx.scene.Node].getResource(cp)
          if (url != null && x == url.toString) {
            Some(s"cp://$cp")
          } else None
        } else None
      }}.getOrElse(x)
  }
}
