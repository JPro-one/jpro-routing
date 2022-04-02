package com.jpro.routing.crawler

import com.jpro.routing.{Redirect, WebApp}

import TestUtils._
import com.jpro.routing.crawl.{AppCrawler, SitemapGenerator}
import simplefx.core._
import org.junit.jupiter.api.Test

class TestSitemapGenerator {
  @Test
  def test(): Unit = {
    def app = new WebApp(null) {
      addRouteScala { case "/" => new Page1}
      addRouteScala { case "/page2" => new Page2}
      addRouteScala { case "/page4" => new Page2}
      addRouteScala { case _ => new Page1} // as error page
    }
    val result = AppCrawler.crawlApp("http://localhost", () => app)
    val sm = SitemapGenerator.createSitemap("http://localhost", result)
    println("SiteMap: " + sm)
    assert(sm.contains("<loc>http://localhost/page4</loc>"))
    assert(!sm.contains("<loc>http://external/link</loc>"))
    assert(!sm.contains("mailto"))
  }

  @Test
  def testMailToRedirect(): Unit = {
    def app = new WebApp(null) {
      addRouteScala { case "/" => pageWithLink(List("/page2", "/page3", "mailto:something"))}
      addRouteScala { case "/page2" => new Redirect("mailto:something-2")}
    }
    val result = AppCrawler.crawlApp("http://localhost", () => app)
    println("got result: " + result)
    val sm = SitemapGenerator.createSitemap("http://localhost", result)
    println("SiteMap2: " + sm)
    assert(!sm.contains("mailto"), "sitemap contained mailto!")
  }
}
