package com.jpro.web

import com.jpro.webapi.{HTMLView, WebAPI}
import simplefx.core._
import simplefx.all._
import simplefx.util.ReflectionUtil._
import java.net.URLEncoder

import com.jpro.web.sessionmanager.SessionManager
import javafx.collections.ObservableList

object Util {

  def getSessionManager(node: Node): SessionManager = {
    SessionManagerContext.getContext(node)
  }

  def setLink(node: Node, url: String): Unit = {
    setLink(node,url,None, null)
  }
  def setLink(node: Node, url: String, text: String): Unit = {
    setLink(node,url,Some(text), null)
  }
  def setLink(node: Node, url: String, text: String, children: ObservableList[Node]): Unit = {
    setLink(node,url,Some(text), children)
  }
  def setLink(node: Node, url: String, text: Option[String] = None, children: ObservableList[Node] = null): Unit = {
    if(url.startsWith("/")) {
      setLinkInternal(node,url, text, children)
    } else {
      setLinkExternal(node,url, text, children)
    }
  }
  def setLinkInternal(node: Node, url: String, text: Option[String] = None, children: ObservableList[Node] = null) = {
    node.cursor = javafx.scene.Cursor.HAND
    if(!WebAPI.isBrowser) {
      node.onMouseClicked --> { e =>
        if(e.isStillSincePress) Util.getSessionManager(node).goto(url)
      }
    } else {
      setLinkSimple(url, text, true)(node, children)
    }
  }
  def setLinkExternal(node: Node, url: String, text: Option[String] = None, children: ObservableList[Node] = null) = {
    node.cursor = javafx.scene.Cursor.HAND
    setLinkSimple(url, text, false)(node, children)
  }

  def goBack(node: Node): Unit = {
    SessionManagerContext.getContext(node).goBack()
  }

  def goForward(node: Node): Unit = {
    SessionManagerContext.getContext(node).goForward()
  }
  def gotoPage(node: Node, url: String) = {
    Util.getSessionManager(node).goto(url)
  }
  def getCurrentPage(node: Node): String = {
    Util.getSessionManager(node).url
  }
  def refresh(node: Node): Unit = {
    val man = Util.getSessionManager(node)
    man.goto(man.url)
  }

  private def setLinkSimple(url: String, text: Option[String], pushState: Boolean)(theNode: Node, children: ObservableList[Node] = null) = if(WebAPI.isBrowser) onceWhen(theNode.parent != null) --> {
    assert(children != null || theNode.parent.isInstanceOf[Region], "The parent at setLink has to be a Pane")
    //val parent = theNode.parent.asInstanceOf[Region]
    val id = "linkid_"+random[Int].abs
    val htmlNode = new HTMLView { htmlNode =>
      layoutXY    <-- theNode.bipXY
      theNode.bipWH --> { x =>
        this.resize(x._1,x._2)
      }
      val script = if(pushState) {
        s"""<script>var x = document.getElementById("${id}");
           | x.addEventListener("click", function(event) {
           |   if(!event.shiftKey && !event.metaKey) {
           |     jpro.jproGotoURL(\"${url.replace("\"","\\\"")}\"); event.preventDefault();
           |   }
           | });</script>
        """.stripMargin
      } else ""

      val styleAnchorText = if(text.isEmpty) "" else "line-height: 0; font-size: 0; color: transparent; "
      setContent(
        s"""<a id="$id" href="${url.replace(" ","%20").replace("\"","&quot;")}" style="$styleAnchorText display: block; width: 100%; height: 100%;">${text.getOrElse("")}</a>
           |$script
         """.stripMargin
      )
      hover --> { x =>
        theNode.useReflection.setHover(x)
      }
      setManaged(false)
    }

    val theChildren = if(children == null) theNode.parent.asInstanceOf[Pane].getChildren else children
    theChildren.add(htmlNode)
  }
}
