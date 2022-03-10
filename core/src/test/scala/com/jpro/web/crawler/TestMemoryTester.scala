package com.jpro.web.crawler

import com.jpro.web.WebApp
import com.jpro.web.crawl.{AppCrawler, MemoryTester}
import com.jpro.web.crawler.TestUtils.{Page1, Page2}
import org.junit.Test
import simplefx.cores.default.inFX
import simplefx.util.Predef.intercept

class TestMemoryTest {

  @Test
  def simpleTest(): Unit = {
    def app = new WebApp(null) {
      addRoute { case "/" => new Page1}
      addRoute { case "/page2" => new Page2}
      addRoute { case "/page4" => new Page2}
    }
    val result = AppCrawler.crawlApp("http://localhost", () => app)
    MemoryTester.testForLeaks(result, () => app)
  }

  @Test
  def simpleTestfail(): Unit = {
    val page2 = new Page2
    def app = new WebApp(null) {
      addRoute { case "/" => new Page1}
      addRoute { case "/page2" => page2}
      addRoute { case "/page4" => new Page2}
    }
    val result = AppCrawler.crawlApp("http://localhost", () => app)
    intercept[Throwable](MemoryTester.testForLeaks(result, () => app))
  }

  @Test
  def simpleTestfail2(): Unit = {
    val app = inFX(new WebApp(null) {
      addRoute { case "/" => new Page1}
      addRoute { case "/page2" => new Page2}
      addRoute { case "/page4" => new Page2}
    })
    val result = AppCrawler.crawlApp("http://localhost", () => app)
    intercept[Throwable](MemoryTester.testForLeaks(result, () => app)) // fails because the webapp is not collectable
  }

}
