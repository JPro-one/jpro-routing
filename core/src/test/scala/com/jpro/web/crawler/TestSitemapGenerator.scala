package com.jpro.web.crawler

import com.jpro.web.WebApp
import org.junit.Test
import TestUtils._
import com.jpro.web.crawl.{AppCrawler, SitemapGenerator}
import simplefx.core._

class TestSitemapGenerator {
  @Test
  def test(): Unit = {
    def app = new WebApp(null) {
      addRoute { case "/" => new Page1}
      addRoute { case "/page2" => new Page2}
      addRoute { case "/page4" => new Page2}
    }
    val result = AppCrawler.crawlApp("http://localhost", () => app)
    val sm = SitemapGenerator.createSitemap("http://localhost", result)
    println("SiteMap: " + sm)
    assert(sm.contains("<loc>http://localhost/page4</loc>"))
    assert(!sm.contains("<loc>http://external/link</loc>"))
  }
}
