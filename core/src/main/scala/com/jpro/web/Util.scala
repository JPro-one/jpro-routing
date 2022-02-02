package com.jpro.web

import com.jpro.webapi.{HTMLView, WebAPI}
import simplefx.core._
import simplefx.all._
import simplefx.experimental._
import simplefx.util.ReflectionUtil._
import simplefx.util.Predef._
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
    setLinkSimple(url, text, true)(node, children)
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
    Util.getSessionManager(node).gotoURL(url)
  }
  def getCurrentPage(node: Node): String = {
    Util.getSessionManager(node).url
  }
  def refresh(node: Node): Unit = {
    val man = Util.getSessionManager(node)
    man.gotoURL(man.url)
  }

  @extension
  class ExtendNodeWithLink(node: Node) {
    var children: ObservableList[Node] = null

    def setNewLink(link: String, text: Option[String], pushState: Boolean, children: ObservableList[Node]): Unit = {
      this.children = children
      this.pushState = pushState
      this.link = link
      this.text = text
    }

    @Bind var pushState: Boolean = false
    @Bind var link: String = ""
    @Bind var text: Option[String] = None

    val id = "linkid_"+random[Int].abs
    @Bind val htmlNode = new HTMLView { htmlNode =>
      layoutXY    <-- node.bipXY
      node.bipWH --> { x =>
        this.resize(x._1,x._2)
      }
      def script = if(pushState) {
        s"""<script>var x = document.getElementById("${id}");
           | x.addEventListener("click", function(event) {
           |   if(!event.shiftKey && !event.metaKey) {
           |     jpro.jproGotoURL(\"${link.replace("\"","\\\"")}\"); event.preventDefault();
           |   }
           | });</script>
        """.stripMargin
      } else ""

      def styleAnchorText = if(text.isEmpty) "" else "line-height: 0; font-size: 0; color: transparent; "
      @Bind var content = contentProperty.toBindable
      content <-- {
        s"""<a id="$id" href="${link.replace(" ","%20").replace("\"","&quot;")}" style="$styleAnchorText display: block; width: 100%; height: 100%;">${text.getOrElse("")}</a>
           |$script
         """.stripMargin
      }
      hover --> { x =>
        node.useReflection.setHover(x)
      }
      setManaged(false)
    }
    if(!WebAPI.isBrowser) {
      node.onMouseClicked --> { e =>
        if(e.isStillSincePress) Util.getSessionManager(node).gotoURL(link)
      }
    } else {
      when(node.parent != null && link != null) ==> {
        assert(children != null || node.parent.isInstanceOf[Region] || node.parent.isInstanceOf[Group], s"The parent at setLink has to be a Pane but was ${node.parent}")
        val theChildren = if(node.parent.isInstanceOf[Region]) node.parent.asInstanceOf[Pane].getChildren
          else if(node.parent.isInstanceOf[Group])  node.parent.asInstanceOf[Group].getChildren
          else children
        val currentNode = htmlNode
        theChildren.add(htmlNode)
        onDispose(theChildren.remove(htmlNode))
      }
    }
  }


  private def setLinkSimple(url: String, text: Option[String], pushState: Boolean)(theNode: Node, children: ObservableList[Node] = null) = {
    theNode.setNewLink(url,text,pushState,children)
  }
}
