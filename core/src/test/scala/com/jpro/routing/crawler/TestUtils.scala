package com.jpro.routing.crawler

import com.jpro.routing.{Util, View}
import simplefx.all._

object TestUtils {
  def pageWithLink(links: List[String]) = new View {
    def title = "title"
    def description = "desc"
    override def content: Node = new HBox {
      links.map { link =>
        this <++ new Label("Asdf") {
          Util.setLink(this,link, "desc1")
        }
      }
    }
  }
  class Page1 extends View {
    def title = "title"
    def description = "desc"

    override def content: Node = new HBox {
      this <++ new Label("Asdf") {
        Util.setLink(this,"/page2", "desc1")
      }
      this <++ new Label("222") {
        Util.setLink(this,"/page2")
      }
      this <++ new Label("mail") {
        Util.setLink(this,"mailto:a@b.com")
      }
      this <++ new ImageView() {
        image = new Image("/testfiles/test.jpg")
        this.setAccessibleRoleDescription("The Description")
      }
    }
  }
  class Page2 extends View {
    def title = "title"
    def description = "desc"

    override def content: Node = new HBox {
      this <++ new Label("Asdf") {
        Util.setLink(this,"/page2", "desc1")
      }
      this <++ new Label("222") {
        Util.setLink(this,"/page3", "desc2")
      }
      this <++ new Label("222") {
        Util.setLink(this,"http://localhost/page4", "desc2")
      }
      this <++ new Label("222") {
        Util.setLink(this,"http://external/link", "desc2")
      }
    }
  }
}
