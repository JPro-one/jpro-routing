package example

import com.jpro.web.{NodeView, Redirect, View, WebApp}
import simplefx.core._
import simplefx.all._
import com.jpro.web.Util._

class MyApp(stage: Stage) extends WebApp(stage) {
  addRoute { case "/"          => new MainView()}
  addRoute { case "/main"      => new MainView()}
  addRoute { case "/sub"       => new SubView()}
  addRoute { case "/redirect"  => Redirect("/sub")}
  addRoute { case x            => new UnknownPage(x)}
}

class Header extends HBox {
  spacing = 10
  this <++ new Label("main"    ) { setLink(this, "/main") }
  this <++ new Label("subpage" ) { setLink(this, "/sub" ) }
  this <++ new Label("redirect") { setLink(this, "/redirect" ) }
  this <++ new Label("google"  ) { setLinkExternal(this, "http://google.com" ) }
  this <++ new Label("dead"    ) { setLink(this, "/asdf" ) }
}

class Footer extends HBox {
  spacing = 10
  this <++ new Label("asd")
  this <++ new Label("asd")
  this <++ new Label("asd")
  this <++ new Label("asd")
}

trait Page extends View {
  override def realContent = {
    new VBox {
      spacing = 10
      this <++ new Header
      this <++ content
      this <++ new Footer
    }
  }
}

class UnknownPage(x: String) extends Page {
  def title = "Unknown page: " + x

  def content = new Label("UNKNOWN PAGE: " + x) { font = new Font(60)}
}
class MainView extends Page {
  def title = "Main"

  val content = new Label("MAIN") { font = new Font(60)}
}
class SubView extends Page {
  def title = "SubView"

  val content = new Label("SUBVIEW") { font = new Font(60)}

}


object TestWebApplication extends App
@SimpleFXApp class TestWebApplication {
  val app = new MyApp(stage)
  root = app
  app.start()
}
