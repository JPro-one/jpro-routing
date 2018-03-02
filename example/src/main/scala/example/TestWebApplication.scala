package example

import com.jpro.web._
import simplefx.core._
import simplefx.all._
import com.jpro.web.Util._
import com.jpro.webapi.{HTMLView, WebAPI}

class MyApp(stage: Stage) extends WebApp(stage) {

  stylesheets ::= "test.css"

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
  addRoute { case "/?page=it's\" tricky" => new MainView()}
  addRoute { case x                  => new UnknownPage(x)}
}

class Header extends HBox {
  padding = Insets(10)
  spacing = 10
  class HeaderLink(str: String, url: String) extends Label (str) {
    styleClass ::= "header-link"
    if(!url.isEmpty) {
      setLink(this, url)
    }
  }
  this <++ new HeaderLink("main"    , "/?page=main")
  this <++ new HeaderLink("subpage" , "/?page=sub" )
  this <++ new HeaderLink("redirect", "/?page=redirect" )
  this <++ new HeaderLink("tricky!" , "/?page=it's\" tricky" )
  this <++ new HeaderLink("google"  , "http://google.com" )
  this <++ new HeaderLink("paralax" , "/?page=paralax" )
  this <++ new HeaderLink("dead"    , "/?page=as df" )
  this <++ new HeaderLink("No Link" , "" )
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
    //  transform = Scale(1.3,1.3)
      spacing = 10
      this <++ new Header
      this <++ content
      this <++ new Footer
      this <++ new Header
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
    this <++ new StackPane(new Label("GOOGL") {
      font = new Font(60);

    }) {
      this <++ new HTMLView {
        setContent(
          """
            |<a style="display: block; width: 100%; height: 100%; background-color: #66666666;" href="http://google.com"></a>
          """.stripMargin
        )
      }
    }
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

  //override def saveScrollPosition: Boolean = false

  val img1 = getClass().getResource("/images/img1.jpg")

  val content = new StackPane {
    this <++ new Region {
      maxWidthProp = 5
      style = "-fx-background-color: green;"
    }
    this <++ new VBox {
      spacing = 200

      this <++ new ParallaxView(img1) {
        minWH = (250,300)
        style = "-fx-border-width:1; -fx-border-color:black;"
      }
      this <++ new ParallaxView(img1) {
        minWH = (250,400)
        style = "-fx-border-width:1; -fx-border-color:black;"
      }
      this <++ new ParallaxView(img1) {
        minWH = (250,400)
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
object TestWebApplicationNative extends App
@SimpleFXApp class TestWebApplicationNative {
  val app = new MyApp(stage)
  root = new ScrollPane(app) {
    fitToWidth = true
  }
  app.start()
}
