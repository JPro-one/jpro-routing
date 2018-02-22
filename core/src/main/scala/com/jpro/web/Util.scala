package com.jpro.web

import com.jpro.webapi.{HTMLView, WebAPI}
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
    if(!WebAPI.isBrowser) {
      node.onMouseClicked --> { e =>
        if(e.isStillSincePress) Util.getSessionManager(node).goto(url)
      }
    } else {
      setLinkSimple(url, true)(node)
    }
  }
  def setLinkExternal(node: Node, url: String) = {
    node.cursor = javafx.scene.Cursor.HAND
    setLinkSimple(url, false)(node)
  }

  def gotoPage(node: Node, url: String) = {
    Util.getSessionManager(node).goto(url)
  }

  private def setLinkSimple(url: String, pushState: Boolean)(theNode: Node) = if(WebAPI.isBrowser) onceWhen(theNode.scene != null) --> {
    assert(theNode.parent.isInstanceOf[Pane], "The parent at setLink has to be a Pane")
    val parent = theNode.parent.asInstanceOf[Pane]
    parent <++ new Group(new HTMLView {
      layoutXY    <-- /*(100,100) */theNode.bipXY
      this.minWH  <-- /*(100,100) */theNode.bipWH
      this.prefWH <-- /*(100,100) */theNode.bipWH
      val fun = if(!pushState) "" else
      s"""onclick="console.log('BLUBBLUB!'); jpro.jproGotoURL('$url'); event.preventDefault();" """.stripMargin

      setContent(
       s"""<a $fun href="$url" style="display: block; width: 100%; height: 100%;"></a>""")
    }) {
      managed = false
    }
  }
}
