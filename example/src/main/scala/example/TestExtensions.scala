package example


import com.jpro.routing.RouteUtils.{EmptyRoute, get, getNode, redirect}
import com.jpro.routing.{ContainerFactory, Filters, Redirect, Request, RouteUtils, LinkUtil, WebApp}
import com.jpro.routing.sessionmanager.SessionManager
import com.jpro.webapi.WebAPI
import simplefx.all._
import simplefx.core._

import java.util.function.Supplier
import com.jpro.routing.extensions.linkheader.LinkHeaderFilter
import com.jpro.routing.extensions.linkheader.LinkHeaderFilter.Link


class TestExtensionsApp(stage: Stage) extends WebApp(stage) {
  
  setRoute(
    EmptyRoute /* StartRoute? */
      .and(redirect("/", "/home"))
      .and(getNode("/home", () => new Label("HOME")))
      .and(getNode("/secret", () => new Label("SECRET")))
      .filter(LinkHeaderFilter.create(Link("HOME","/home"), Link("SECRET","/secret")))
  )
}


object TestExtensions extends App
@SimpleFXApp class TestExtensions {
  val app = new TestExtensionsApp(stage)
  if(WebAPI.isBrowser) {
    root = app
  } else {
    scene = new Scene(app, 1400,800)
  }
  app.start(SessionManager.getDefault(app,stage))
}


