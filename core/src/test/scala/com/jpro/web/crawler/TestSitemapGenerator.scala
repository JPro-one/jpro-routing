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
    }
    val result = AppCrawler.crawlApp(() => app)
    println("SiteMap: " + SitemapGenerator.createSitemap("http://localhost:8080", result))
  }
}
