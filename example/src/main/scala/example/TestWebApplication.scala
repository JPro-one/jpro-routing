package example

import com.jpro.web._
import simplefx.core._
import simplefx.all._
import com.jpro.web.Util._
import com.jpro.webapi.WebAPI

class MyApp(stage: Stage) extends WebApp(stage) {

  override def requestLayout(): Unit = {
    //println("request layout called!")
    if ((this.scene ne null) && WebAPI.isBrowser) {
      WebAPI.getWebAPI(stage).requestLayout(this.scene)
    }
    super.requestLayout
  }

  addRoute { case "/"                => new MainView()}
  addRoute { case "/?page=main"      => new MainView()}
  addRoute { case "/?page=sub"       => new SubView()}
  addRoute { case "/?page=redirect"  => Redirect("/sub")}
  addRoute { case "/?page=paralax"   => new ParalaxPage()}
  addRoute { case x                  => new UnknownPage(x)}
}

class Header extends HBox {
  spacing = 10
  this <++ new Label("main"    ) { setLink(this, "/?page=main") }
  this <++ new Label("subpage" ) { setLink(this, "/?page=sub" ) }
  this <++ new Label("redirect") { setLink(this, "/?page=redirect" ) }
  this <++ new Label("google"  ) { setLinkExternal(this, "http://google.com" ) }
  this <++ new Label("paralax" ) { setLink(this, "/?page=paralax" ) }
  this <++ new Label("dead"    ) { setLink(this, "/?page=asdf" ) }
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

  val content = new VBox {
    spacing = 200
    this <++ new Label("MAIN") { font = new Font(60)}
    this <++ new Label(" label 123") { font = new Font(60)}
    this <++ new Label(" label 123") { font = new Font(60)}
    this <++ new Label(" label 123") { font = new Font(60)}
    this <++ new Label("paralax" ) { font = new Font(60); setLink(this, "/?page=paralax" ) }

  }
}

class SubView extends Page {
  def title = "SubView"

  val content = new Label("SUBVIEW") { font = new Font(60)}
}

class ParalaxPage extends Page {
  def title = "Paralax"

  override def nativeScrolling = false

  override def saveScrollPosition: Boolean = false

  val img1 = getClass().getResource("/images/img1.jpg")

  val content = new StackPane {
    this <++ new Region {
      maxWidthProp = 5
      style = "-fx-background-color: green;"
    }
    this <++ new VBox {
      spacing = 200
      this <++ new ParalaxView(img1) {
        minWH = (250,300)
        style = "-fx-border-width:1; -fx-border-color:black;"
      }
      this <++ new ParalaxView(img1) {
        minWH = (250,700)
        style = "-fx-border-width:1; -fx-border-color:black;"
      }
      this <++ new ParalaxView(img1) {
        minWH = (250,700)
        style = "-fx-border-width:1; -fx-border-color:black;"
      }
      this <++ new Label("asdf")
      this <++ new Label("asdf")
      this <++ new Label("asdf")
    }
    this <++ new Region {
      maxWidthProp = 5
      style = "-fx-background-color: green;"
    }
  }

}


object TestWebApplication extends App
@SimpleFXApp class TestWebApplication {
  val app = new MyApp(stage)
  root = app
  app.start()
}
