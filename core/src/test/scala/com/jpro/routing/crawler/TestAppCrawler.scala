package com.jpro.routing.crawler

import com.jpro.routing.crawl.AppCrawler
import com.jpro.routing.crawl.AppCrawler._
import com.jpro.routing.{LinkUtil, View, WebApp}
import simplefx.all._
import simplefx.core._
import TestUtils._
import simplefx.all
import org.junit.jupiter.api.Test

class TestAppCrawler {

  @Test
  def crawlPage(): Unit = inFX {
    println("test")
    val page = new Page1
    val result = AppCrawler.crawlPage(page)
    assert(result.title == "title")
    assert(result.description == "desc")
    println("Links: " + result.links)
    println("Links: " + result.pictures)
    assert(result.links contains LinkInfo("/page2", "desc1"))
    assert(result.links contains LinkInfo("/page2", ""), result.links)
    assert(!result.pictures.filter(x => x.description == "The Description").isEmpty)
  }

  @Test
  def nullFails(): Unit = inFX {
    val page = pageWithLink(List(null))
    val result = AppCrawler.crawlPage(page)
    assert(result.links.isEmpty, result.links)
  }

  @Test
  def testCrawlApp(): Unit = {
    def app = new WebApp(null) {
      addRouteScala { case "/" => new Page1}
      addRouteScala { case "/page2" => new Page2}
    }
    val result = AppCrawler.crawlApp("http://localhost", () => app)

    assert(result.pages.contains("/"), result.pages)
    assert(result.pages.contains("/page2"), result.pages)
    assert(!result.pages.contains("/page3"), result.pages)
    assert(result.deadLinks.contains("/page3"), result.pages)
  }

  @Test
  def testEmptyImage(): Unit = {
    def app = new WebApp(null) {
      addRouteScala { case "/" => new View {
        override def title: String = ""

        override def description: String = ""

        override def content: all.Node = new ImageView(null: Image)
      }}
    }
    val result = AppCrawler.crawlApp("http://localhost", () => app)
  }

  @Test
  def testIndexListview(): Unit = inFX{
    val view = new View {
      override def title: String = ""
      override def description: String = ""
      val content: all.Node = new ListView[String] {
        items = (List(1,2,3,4,5,6,7,8,9,10).map(_.toString): List[String])
        class MyListCell extends ListCell[String] { listCell =>
          listCell.setGraphic(new Label("123") {
            listCell.itemProperty().addListener((p,o,n) => {
              LinkUtil.setLink(this, "/list" + n)
            })
          })
        }
        cellFactory = (v: ListView[String]) => new MyListCell
      }
    }
    val r = AppCrawler.crawlPage(view)
    assert(r.links.contains(LinkInfo("/list1","")))
    assert(r.links.contains(LinkInfo("/list9","")))
  }

  @Test
  def testScrollPane(): Unit = inFX{
    val view = new View {
      override def title: String = ""
      override def description: String = ""
      val content: all.Node = new ScrollPane {
        this.content = new Label() {
          LinkUtil.setLink(this, "/scrollpane")
        }
      }
    }
    val r = AppCrawler.crawlPage(view)
    assert(r.links.contains(LinkInfo("/scrollpane","")))
  }

  @Test
  def testImageInStyle (): Unit = inFX {
    val view = new View {
      override def title: String = ""
      override def description: String = ""
      val content: Node = new Region() {
        style = ("-fx-background-image: url('/testfiles/test.jpg');")
      }
    }
    val r = AppCrawler.crawlPage(view)
    assert(!r.pictures.isEmpty)
  }

  @Test
  def testAccessingSessionManager (): Unit = inFX {
    val view = new View {
      override def title: String = ""
      override def description: String = ""
      val content: Node = new Region() {
        this.sceneProperty.addListener((p,o,n) =>{
          if(n != null) {
            LinkUtil.getSessionManager(this)
          }
        })
      }
    }
    val r = AppCrawler.crawlPage(view)
  }

}
