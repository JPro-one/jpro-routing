package com.jpro.web

import com.jpro.webapi.{HTMLView, WebAPI}
import simplefx.core._
import simplefx.all._
import simplefx.util.ReflectionUtil._
import java.net.URLEncoder
import javafx.collections.ObservableList

object Util {

  def getSessionManager(node: Node): SessionManager = {
    SessionManagerContext.getContext(node)
  }

  def setLink(node: Node, url: String): Unit = {
    setLink(node,url,None)
  }
  def setLink(node: Node, url: String, text: String): Unit = {
    setLink(node,url,Some(text))
  }
  def setLink(node: Node, url: String, text: Option[String] = None): Unit = {
    if(url.startsWith("/")) {
      setLinkInternal(node,url, text)
    } else {
      setLinkExternal(node,url, text)
    }
  }
  def setLinkInternal(node: Node, url: String, text: Option[String] = None) = {
    node.cursor = javafx.scene.Cursor.HAND
    if(!WebAPI.isBrowser) {
      node.onMouseClicked --> { e =>
        if(e.isStillSincePress) Util.getSessionManager(node).goto(url)
      }
    } else {
      setLinkSimple(url, text, true)(node)
    }
  }
  def setLinkExternal(node: Node, url: String, text: Option[String] = None) = {
    node.cursor = javafx.scene.Cursor.HAND
    setLinkSimple(url, text, false)(node)
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

  private def setLinkSimple(url: String, text: Option[String], pushState: Boolean)(theNode: Node) = if(WebAPI.isBrowser) onceWhen(theNode.scene != null) --> {
    assert(theNode.parent.isInstanceOf[Pane], "The parent at setLink has to be a Pane")
    val parent = theNode.parent.asInstanceOf[Pane]
    val id = "linkid_"+random[Int].abs
    val htmlNode = new HTMLView { htmlNode =>
      layoutXY    <-- theNode.bipXY
      theNode.bipWH --> { x =>
        this.resize(x._1,x._2)
      }
      val script = if(pushState) {
        s"""<script>var x = document.getElementById("${id}");
           | x.addEventListener("click", function(event) {
           |   jpro.jproGotoURL(\"${url.replace("\"","\\\"")}\"); event.preventDefault();
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


      managed = false
    }

    parent.children = parent.children ::: htmlNode :: Nil
  }
}
