package com.jpro.routing

import com.jpro.routing.LinkUtil.isValidLink
import javafx.scene.Node

import java.lang.ref.WeakReference
import java.net.{URI, URLEncoder}

case class Request (
  url: String,
  domain: String,
  origPath: String,
  path: String,
  queryParameters: Map[String,String],
  origOldContent: WeakReference[Node],
  oldContent: WeakReference[Node]
) {
  def mapContent(f: Node => Node) = {
    val oldContentV = oldContent.get()
    val oldContentVW = if(oldContentV eq null) {
      Request.wref_null
    } else {
      new WeakReference(f(oldContentV))
    }
    this.copy(oldContent = oldContentVW)
  }
}
object Request {
  private var wref_null = new WeakReference[Node](null)
  def fromString(x: String): Request = {
    if(!isValidLink(x)) {
      println("Warning - Invalid Link: " + x)
    }
    val uri = new URI(x)
    val query: Map[String,String] = if(uri.getQuery == null) Map() else uri.getQuery.split("&").map(x => {
      val Array(a,b) = x.split("=")
      a -> b
    }).toMap
    val path = uri.getPath
    val res = Request(x, uri.getHost,path,path,query,wref_null,wref_null)
    res
  }
}
