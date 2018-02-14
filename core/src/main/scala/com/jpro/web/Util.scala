package com.jpro.web

import com.jpro.webapi.WebAPI
import simplefx.core._
import simplefx.all._

object Util {

  def getSessionManager(node: Node): SessionManager = {
    SessionManagerContext.getContext(node)
  }

  def setLink(node: Node, url: String): Unit = {
    if(url.startsWith("/")) {
      setLinkInternal(node,url)
    } else {
      setLinkExternal(node,url)
    }
  }
  def setLinkInternal(node: Node, url: String) = {
    node.cursor = javafx.scene.Cursor.HAND
    node.onMouseClicked --> { e =>
      if(e.isStillSincePress) Util.getSessionManager(node).goto(url)
    }
  }
  def setLinkExternal(node: Node, url: String) = {
    node.cursor = javafx.scene.Cursor.HAND
    setLinkSimple(url)(node)
  }

  def gotoPage(node: Node, url: String) = {
    Util.getSessionManager(node).goto(url)
  }

  private def setLinkSimple(url: String)(theNode: Node) = if(WebAPI.isBrowser) onceWhen(theNode.scene != null) --> {
    def webAPI = WebAPI.getWebAPI(theNode.getScene)
    webAPI.registerValue("tmpNode",theNode)
    // touchstart is important for iOS!
    webAPI.executeScript(
      s"""
         |jpro.tmpNode.addEventListener("click"   , function(e) {window.location.href = '$url';});
         |jpro.tmpNode.addEventListener("touchend", function(e) {window.location.href = '$url';});
            """.stripMargin)
  }
}
