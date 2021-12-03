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
    }
  }
}
