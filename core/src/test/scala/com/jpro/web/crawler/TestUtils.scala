package com.jpro.web.crawler

import com.jpro.web.{Util, View}
import simplefx.all._

object TestUtils {
  class Page1 extends View {
    def title = "title"
    def description = "desc"

    override def content: Node = new HBox {
      this <++ new Label("Asdf") {
        Util.setLink(this,"/page2", "desc1")
      }
      this <++ new Label("222") {
        Util.setLink(this,"/page2", "desc2")
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
