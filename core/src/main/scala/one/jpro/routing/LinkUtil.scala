package one.jpro.routing

import com.jpro.webapi.{HTMLView, WebAPI}
import simplefx.core._
import simplefx.all._
import simplefx.experimental._
import simplefx.util.ReflectionUtil._
import simplefx.util.Predef._

import java.net.URLEncoder
import javafx.collections.ObservableList
import one.jpro.routing.sessionmanager.SessionManager
import org.apache.commons.validator.routines.UrlValidator

object LinkUtil {

  private var openLinkExternalFun: String => Unit = { link =>
    // Open link with awt
    import java.awt.Desktop
    import java.net.URI
    Desktop.getDesktop.browse(new URI(link))
  }
  def setOpenLinkExternalFun(x: String => Unit): Unit = {
    openLinkExternalFun = x
  }

  private val urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS)
  def isValidLink(x: String): Boolean = {
    if(x.startsWith("http")) {
      urlValidator.isValid(x)
    } else {
      urlValidator.isValid("http://a.com"+x)
    }
  }

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
    assert(url != "", s"Empty link: ''")
    assert(isValidLink(url), s"Invalid link: '$url''")
    node.getProperties.put("link",url)
    text.map {desc =>
      node.getProperties.put("description",desc)
    }
    if(url == null || url.startsWith("/") || url.startsWith("./") || url.startsWith("../")) {
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
    LinkUtil.getSessionManager(node).gotoURL(url)
  }
  def getCurrentPage(node: Node): String = {
    LinkUtil.getSessionManager(node).url
  }
  def refresh(node: Node): Unit = {
    val man = LinkUtil.getSessionManager(node)
    assert(man.url != null, "current url was null")
    man.gotoURL(man.url)
  }

  @extension
  class ExtendNodeWithLink(node: Node) {
    var children: ObservableList[Node] = null

    def setNewLink(link: String, text: Option[String], pushState: Boolean, children: ObservableList[Node]): Unit = {
      if (link != null && !isValidLink(link)) {
        println("Warning, link is not valid: " + link)
      }
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
        if(link == null) ""
        else s"""<a id="$id" href="${link.replace(" ","%20").replace("\"","&quot;")}" style="$styleAnchorText display: block; width: 100%; height: 100%;">${text.getOrElse("")}</a>
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
        def isExternalLink(x: String) = x.startsWith("http") || x.startsWith("mailto")
        if(e.isStillSincePress) {
          if(isExternalLink(link)) {
            openLinkExternalFun(link)
          } else {
            LinkUtil.getSessionManager(node).gotoURL(link)
          }
        }
      }
    } else {
      var currentParent: ObservableList[Node] = null
      def cleanCurrentParent(): Unit = {
        if(currentParent != null) {
          if(currentParent.contains(htmlNode)) {
            currentParent.remove(htmlNode)
          }
          currentParent = null
        }
      }
      when(node.parent != null && link != null) --> {
        assert(children != null || node.parent.isInstanceOf[Region] || node.parent.isInstanceOf[Group], s"The parent at setLink has to be a Pane but was ${node.parent}")
        cleanCurrentParent()
        currentParent = if(node.parent.isInstanceOf[Region]) node.parent.asInstanceOf[Pane].getChildren
          else if(node.parent.isInstanceOf[Group])  node.parent.asInstanceOf[Group].getChildren
          else children
        currentParent.add(htmlNode)
        onDispose(cleanCurrentParent())
      }
    }
  }


  private def setLinkSimple(url: String, text: Option[String], pushState: Boolean)(theNode: Node, children: ObservableList[Node] = null) = {
    theNode.setNewLink(url,text,pushState,children)
  }

  def setImageViewDescription(view: ImageView, description: String): Unit = {
    view.setAccessibleRoleDescription(description)
  }
}
