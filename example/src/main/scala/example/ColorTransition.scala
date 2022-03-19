package example

import com.jpro.routing.RouteUtils.{EmptyRoute, get, getNode}
import com.jpro.routing.{Filters, Redirect, RouteUtils, Util, WebApp}
import com.jpro.routing.sessionmanager.SessionManager
import com.jpro.webapi.WebAPI
import simplefx.all._
import simplefx.core._

import java.util.function.Supplier

class ColorTransition(stage: Stage) extends WebApp(stage) {
  private def format(v: Double) = {
    val in = Integer.toHexString((v * 255).round.toInt)
    if (in.length == 1) "0" + in
    else in
  }
  def toHexString(value: Color): String = "#" + (format(value.getRed) + format(value.getGreen) + format(value.getBlue) + format(value.getOpacity)).toUpperCase
  def gen(x: String, next: String, color: Color): Supplier[Node] = () => new StackPane {
    Util.setLink(this,next)
    this <++ new Label(x) {
      style = "-fx-font-size: 36px;"
    }
    style = s"-fx-background-color: ${toHexString(color)};"
  }

  setRoute(
    EmptyRoute
      .andThen(get("/", () => Redirect("/green")))
      .andThen(getNode("/green", gen("Green","/red", Color.GREEN)))
      .andThen(getNode("/red", gen("Red", "/blue", Color.RED)))
      .andThen(getNode("/blue", gen("Blue", "/yellow", Color.BLUE)))
      .andThen(getNode("/yellow", gen("Yellow", "/red", Color.YELLOW)))
      .filter(Filters.FullscreenFilter(true))
      .filter(RouteUtils.transitionFilter(1))
      .filter(RouteUtils.SimpleLogin("username","password"))
      .filter(CreateHeader())
    //.filter(GoogleOAuthFilter)
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
