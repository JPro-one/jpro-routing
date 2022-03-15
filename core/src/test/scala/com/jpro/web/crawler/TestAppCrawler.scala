package com.jpro.web.crawler

import com.jpro.web.crawl.AppCrawler
import com.jpro.web.crawl.AppCrawler._
import com.jpro.web.{Util, View, WebApp}
import org.junit.Test
import simplefx.all._
import simplefx.core._
import TestUtils._
import simplefx.all

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
      addRoute { case "/" => new Page1}
      addRoute { case "/page2" => new Page2}
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
      addRoute { case "/" => new View {
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
              Util.setLink(this, "/list" + n)
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
          Util.setLink(this, "/scrollpane")
        }
      }
    }
    val r = AppCrawler.crawlPage(view)
    assert(r.links.contains(LinkInfo("/scrollpane","")))
  }

}
