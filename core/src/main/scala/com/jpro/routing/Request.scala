package com.jpro.routing

import javafx.scene.Node

case class Request (
  domain: String,
  origPath: String,
  path: String,
  queryParameters: Map[String,String],
  origOldContent: Node,
  oldContent: Node
)
object Request {
  def fromString(x: String) = Request("",x,x,Map(),null,null)
}
