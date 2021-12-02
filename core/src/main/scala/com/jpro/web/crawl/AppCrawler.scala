package com.jpro.web.crawl

import com.jpro.web.{Redirect, View, WebApp}
import simplefx.all.{Node, Parent}

object AppCrawler {
  case class LinkInfo(url: String, description: String)

  case class ImageInfo(url: String, description: String)

  case class CrawlReportPage(links: List[LinkInfo], pictures: List[ImageInfo], title: String, description: String)

  case class CrawlReportApp(pages: List[String], deadLinks: List[String])

  def crawlPage(page: View): CrawlReportPage = {
    var foundLinks: List[LinkInfo] = Nil
    var images: List[ImageInfo] = Nil

    var visitedNodes: Set[Node] = Set()

    def crawlNode(x: Node): Unit = {
      if (visitedNodes.contains(x)) return
      visitedNodes += x
      if (x.getProperties.containsKey("link")) {
        foundLinks ::= LinkInfo(x.getProperties.get("link").asInstanceOf[String], x.getProperties.get("description").asInstanceOf[String])
      }

      if (x.isInstanceOf[Parent]) {
        x.asInstanceOf[Parent].childrenUnmodifiable.map(x => crawlNode(x))
      }
    }

    crawlNode(page.content)

    CrawlReportPage(foundLinks, images, page.title, page.description)
  }

  def crawlApp(createApp: () => WebApp): CrawlReportApp = {
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
        createApp().route.lift(crawlNext).getOrElse(FXFuture(null))
      }.await
      result match {
        case Redirect(url) =>
          redirects += crawlNext
          if (!indexed.contains(url) && !toIndex.contains(url)) {
            toIndex += url
          }
        case view: View =>
          val newReport = inFX(crawlPage(view))
          reports = newReport :: reports
          newReport.links.map { link =>
            if (!indexed.contains(link.url) && !toIndex.contains(link.url)) {
              toIndex += link.url
            }
          }
        case null =>
          emptyLinks += crawlNext
      }
    }

    CrawlReportApp((indexed -- redirects -- emptyLinks).toList, emptyLinks.toList)
  }

}
