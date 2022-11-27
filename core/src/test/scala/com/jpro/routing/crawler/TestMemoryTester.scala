package com.jpro.routing.crawler

import com.jpro.routing.RouteNode
import com.jpro.routing.RouteUtils._
import com.jpro.routing.crawl.{AppCrawler, MemoryTester}
import com.jpro.routing.crawler.TestUtils.{Page1, Page2}
import simplefx.cores.default.inFX
import simplefx.util.Predef.intercept
import org.junit.jupiter.api.Test

class TestMemoryTest {

  @Test
  def simpleTest(): Unit = {
    def app = new RouteNode(null) {
      setRoute(EmptyRoute
        .and(get("/", r => new Page1))
        .and(get("/page2", r => new Page2))
        .and(get("/page4", r => new Page2)))
    }
    val result = AppCrawler.crawlApp("http://localhost", () => app)
    MemoryTester.testForLeaks(result, () => app)
  }

  @Test
  def simpleFailingTest(): Unit = {
    val page2 = new Page2
    def app = new RouteNode(null) {
      setRoute(EmptyRoute
        .and(get("/", r => new Page1))
        .and(get("/page2", r => page2))
        .and(get("/page4", r => new Page2)))
    }
    val result = AppCrawler.crawlApp("http://localhost", () => app)
    intercept[Throwable](MemoryTester.testForLeaks(result, () => app))
  }

  @Test
  def simpleFailingTest2(): Unit = {
    val app = inFX(new RouteNode(null) {
      setRoute(EmptyRoute
        .and(get("/", r => new Page1))
        .and(get("/page2", r => new Page2))
        .and(get("/page4", r => new Page2)))
    })
    val result = AppCrawler.crawlApp("http://localhost", () => app)
    intercept[Throwable](MemoryTester.testForLeaks(result, () => app)) // fails because the webapp is not collectable
  }

}
