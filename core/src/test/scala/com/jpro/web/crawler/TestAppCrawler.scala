package com.jpro.web.crawler

import com.jpro.web.crawl.AppCrawler
import com.jpro.web.crawl.AppCrawler._
import com.jpro.web.{Util, View, WebApp}
import org.junit.Test
import simplefx.all._
import simplefx.core._
import TestUtils._

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
    assert(result.links contains LinkInfo("/page2", "desc2"))
    assert(!result.pictures.filter(x => x.description == "The Description").isEmpty)

  }

  @Test
  def testCrawlApp(): Unit = {
    def app = new WebApp(null) {
      addRoute { case "/" => new Page1}
      addRoute { case "/page2" => new Page2}
    }
    val result = AppCrawler.crawlApp(() => app)

    assert(result.pages.contains("/"), result.pages)
    assert(result.pages.contains("/page2"), result.pages)
    assert(!result.pages.contains("/page3"), result.pages)
    assert(result.deadLinks.contains("/page3"), result.pages)
  }

}
