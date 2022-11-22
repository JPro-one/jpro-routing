package com.jpro.routing

import javafx.scene.Node
import java.net.URI

case class Request (
  url: String,
  domain: String,
  origPath: String,
  path: String,
  queryParameters: Map[String,String],
  origOldContent: Node,
  oldContent: Node
) {
  def mapContent(f: Node => Node) = this.copy(oldContent = f(oldContent))
}
object Request {
  def fromString(x: String): Request = {
    val uri = new URI(x)
    val query: Map[String,String] = if(uri.getQuery == null) Map() else uri.getQuery.split("&").map(x => {
      val Array(a,b) = x.split("=")
      a -> b
    }).toMap
    val path = uri.getPath
    val res = Request(x, uri.getHost,path,path,query,null,null)
    res
  }
}
