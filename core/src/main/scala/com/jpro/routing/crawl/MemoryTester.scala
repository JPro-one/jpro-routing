package com.jpro.routing.crawl

import com.jpro.routing.WebApp
import com.jpro.routing.crawl.AppCrawler.CrawlReportApp
import de.sandec.jmemorybuddy.JMemoryBuddy
import simplefx.cores.default.inFX

import java.util.function.Supplier

object MemoryTester {

  def testForLeaks(x: CrawlReportApp, appFactory: Supplier[WebApp]): Unit = {
    x.pages.map { pageURL =>
      println("Checking for leak for the url: " + pageURL)
      JMemoryBuddy.memoryTest(checker1 => {
        JMemoryBuddy.memoryTest(checker2 => {
          val factory = inFX(appFactory.get())
          val view = inFX(appFactory.get().route(pageURL)).await

          checker2.setAsReferenced(factory)
          checker2.assertCollectable(view)
          checker1.assertCollectable(factory)
        })
      })
    }
  }
}
