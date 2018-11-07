package example

import com.jpro.web._
import simplefx.core._
import simplefx.all._
import com.jpro.web.Util._
import com.jpro.webapi.{HTMLView, WebAPI}
import org.controlsfx.control.PopOver

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
  addRoute { case "/?page=green"      => new GreenView()}
  addRoute { case "/?page=orange"      => new OrangeView()}
  addRoute { case "/?page=sub"       => new SubView()}
  addRoute { case "/?page=redirect"  => Redirect("/?page=sub")}
  addRoute { case "/?page=paralax"   => new ParalaxPage()}
  addRoute { case "/?page=it's\" tricky" => new MainView()}
  addRoute { case x                  => new UnknownPage(x)}

 // addTransition{ case (null,view2,true ) => PageTransition.InstantTransition }
 // addTransition{ case (view,view2,true ) => PageTransition.MoveDown }
 // addTransition{ case (view,view2,false) => PageTransition.MoveUp }
}

class Header extends HBox {
  padding = Insets(10)
  spacing = 10
  class HeaderLink(str: String, url: String) extends Label (str) {
    styleClass ::= "header-link"
    if(!url.isEmpty) {
      setLink(this, url, Some(str))
    }
  }
  this <++ new HeaderLink("main"    , "/?page=main")
  this <++ new HeaderLink("subpage" , "/?page=sub" )
  this <++ new HeaderLink("redirect", "/?page=redirect" )
  this <++ new HeaderLink("tricky!" , "/?page=it's\" tricky" )
  this <++ new HeaderLink("google"  , "http://google.com" )
  this <++ new HeaderLink("paralax" , "/?page=paralax" )
  this <++ new HeaderLink("dead"    , "/?page=as df" )
  this <++ new HeaderLink("green"   , "/?page=green" )
  this <++ new HeaderLink("orange"  , "/?page=orange" )
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
  override lazy val realContent = {
    new VBox {
      style = "-fx-background-color: white;"
    //  transform = Scale(1.3,1.3)
      spacing = 10
      this <++ new Header
      val theContent = content
      javafx.scene.layout.VBox.setVgrow(theContent,Priority.ALWAYS)
      this <++ theContent
      //this <++ new Footer
      this <++ new Header
    }
  }
}

class UnknownPage(x: String) extends Page {
  def title = "Unknown page: " + x
  def description = "Unknown page: " + x

  override def fullscreen = false

  def content = new Label("UNKNOWN PAGE: " + x) { font = new Font(60)}
}
class OrangeView() extends Page {
  def title = "Orange Page"
  def description = "desc Main"

  override def fullscreen = false

  def content = new StackPane { style = "-fx-background-color: orange;"}
}
class GreenView() extends Page {
  def title = "Green Page"
  def description = "desc Main"

  override def fullscreen = false

  def content = new StackPane { style = "-fx-background-color: green;"}
}

class MainView extends Page {
  def title = "Main"
  def description = "desc Main"

  val content = new VBox {
    spacing = 100
    def addGoogle: Node = {
      new StackPane(new Label("GOOGL") {
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
    }
    this <++ addGoogle
    this <++ new Label(" label 123") { font = new Font(60)}
    this <++ new Button("Open Popup") { button =>
      onAction --> {
        val content = new VBox { box =>
          this <++ new Label("Im A Link to Google!") {
            setLink(this,"http://google.com")
          }
          this <++ addGoogle
        }
        new PopOver(content) {
        }.show(button)
      }
    }
    this <++ new Label(" label 123") { font = new Font(60)}
    this <++ new Label(" label 123") { font = new Font(60)}
    this <++ addGoogle
    this <++ new Label("paralax" ) { font = new Font(60); setLink(this, "/?page=paralax" ) }

  }
}

class SubView extends Page {
  def title = "SubView"
  def description = "desc Sub"
  override def fullscreen=true

  val content = new VBox {
    this <++ new Label("SUBVIEW") { font = new Font(60)}
    this <++ new Label("I'm fullscreen!") { font = new Font(60)}
  }
}

class ParalaxPage extends Page {
  def title = "Paralax"
  def description = "desc Para"

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
