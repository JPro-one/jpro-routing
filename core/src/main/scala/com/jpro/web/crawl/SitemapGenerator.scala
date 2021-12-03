package com.jpro.web.crawl

import com.jpro.web.crawl.AppCrawler.CrawlReportApp

object SitemapGenerator {
  def createSitemap(prefix: String, report: CrawlReportApp): String = {
    import javax.xml.parsers.DocumentBuilder
    import javax.xml.parsers.DocumentBuilderFactory
    import javax.xml.transform.TransformerFactory
    import javax.xml.transform.dom.DOMSource
    import javax.xml.transform.stream.StreamResult

    val docFactory = DocumentBuilderFactory.newInstance
    val docBuilder = docFactory.newDocumentBuilder
    val doc = docBuilder.newDocument
    val urlset = doc.createElement("urlset")
    urlset.setAttribute("xmlns","http://www.sitemaps.org/schemas/sitemap/0.9")
    doc.appendChild(urlset)

    report.pages.map { page =>
      val child1 = doc.createElement("url")
      val loc = doc.createElement("loc")
      loc.setTextContent(prefix + page)

      child1.appendChild(loc)
      urlset.appendChild(child1)
    }

    val transformerFactory = TransformerFactory.newInstance
    val transformer = transformerFactory.newTransformer
    val source = new DOMSource(doc)

    import javax.xml.transform.stream.StreamResult
    import java.io.StringWriter
    val writer = new StringWriter
    val result = new StreamResult(writer)
    transformer.transform(source, result)
    writer.toString
  }

}
