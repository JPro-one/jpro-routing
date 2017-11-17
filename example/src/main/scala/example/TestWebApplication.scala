package example

import com.jpro.web.{NodeView, WebApp}
import simplefx.core._
import simplefx.all._
import com.jpro.web.Util._

class MyApp(stage: Stage) extends WebApp(stage) {
  addRoute { case "/"     => new MainView()}
  addRoute { case "/main" => new MainView()}
  addRoute { case "/sub"  => new SubView()}
  addRoute { case x      => new UnknownPage(x)}
}

class Header extends HBox {
  this <++ new Label("main"   ) { setLink(this, "/main") }
  this <++ new Label("subpage") { setLink(this, "/sub" ) }
  this <++ new Label("google" ) { setLinkExternal(this, "http://google.com" ) }
  this <++ new Label("dead"   ) { setLink(this, "/asdf" ) }
}
class UnknownPage(x: String) extends VBox with NodeView {
  def title = "Unknown page: " + x

  this <++ new Label("UNKNOWN PAGE: " + x) { font = new Font(60)}
}
class MainView extends VBox with NodeView {
  def title = "Main"

  this <++ new Header
  this <++ new Label("MAIN") { font = new Font(60)}
}
class SubView extends VBox with NodeView {
  def title = "SubView"

  this <++ new Header
  this <++ new Label("SUBVIEW") { font = new Font(60)}

}


object TestWebApplication extends App
@SimpleFXApp class TestWebApplication {
  val app = new MyApp(stage)
  root = app
  app.start()
}
