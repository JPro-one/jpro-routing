package example

import com.jpro.routing.RouteUtils.{EmptyRoute, get, getNode}
import com.jpro.routing.{ContainerFactory, Filters, Redirect, Request, RouteUtils, LinkUtil, WebApp}
import com.jpro.routing.sessionmanager.SessionManager
import com.jpro.webapi.WebAPI
import simplefx.all._
import simplefx.core._

import java.util.function.Supplier

object HeaderFactory extends RouteUtils.SFXContainerFactory {
  override def createContainer() = new MyContainer
  class MyContainer extends VBox with Container {

    this <++ new Label {
      text <-- (if(request != null) request.origPath else "---")
    }
    this <++ new StackPane {
      javafx.scene.layout.VBox.setVgrow(this, Priority.ALWAYS)
      children <-- (if(content != null) List(content) else Nil)
    }
  }
}

class ColorTransition(stage: Stage) extends WebApp(stage) {
  private def format(v: Double) = {
    val in = Integer.toHexString((v * 255).round.toInt)
    if (in.length == 1) "0" + in
    else in
  }
  def toHexString(value: Color): String = "#" + (format(value.getRed) + format(value.getGreen) + format(value.getBlue) + format(value.getOpacity)).toUpperCase
  def gen(x: String, next: String, color: Color): Supplier[Node] = () => new StackPane {
    LinkUtil.setLink(this,next)
    this <++ new Label(x) {
      style = "-fx-font-size: 36px;"
    }
    style = s"-fx-background-color: ${toHexString(color)};"
  }
  /* Util rename into LinkUtil */
  setRoute(
    EmptyRoute /* StartRoute? */
      .andThen(get("/", () => Redirect("/green")))
      .andThen(getNode("/green", gen("Green","/red", Color.GREEN)))
      .andThen(getNode("/red", gen("Red", "/blue", Color.RED)))
      .andThen(getNode("/blue", gen("Blue", "/yellow", Color.BLUE)))
      .andThen(getNode("/yellow", gen("Yellow", "/red", Color.YELLOW)))
      .filter(Filters.FullscreenFilter(true))
      //.filter(RouteUtils.sideTransitionFilter(1))
      .filter(RouteUtils.containerFilter(HeaderFactory))
      .filter(RouteUtils.containerFilter(HeaderFactory))
      .filter(RouteUtils.containerFilter(HeaderFactory))
  )
}


object ColorTransitionApp extends App
@SimpleFXApp class ColorTransitionApp {
  val app = new ColorTransition(stage)
  if(WebAPI.isBrowser) {
    root = app
  } else {
    scene = new Scene(app, 1400,800)
  }
  app.start(SessionManager.getDefault(app,stage))
}
