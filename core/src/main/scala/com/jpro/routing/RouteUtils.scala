package com.jpro.routing

import simplefx.all._
import simplefx.core._
import simplefx.experimental._

import java.util.function.Supplier

object RouteUtils {

  val EmptyRoute: Route = (x) => null

  def get(path: String, x: View): Route = ???

  def getView(path: String, view: Supplier[View]): Route = (request: Request) => if(request.path == path) FXFuture.unit(view.get()) else null
  def getNode(path: String, node: Supplier[Node]): Route = (request: Request) => if(request.path == path) FXFuture.unit(viewFromNode(node.get())) else null

  implicit def toRoute(f: Response => FXFuture[Request]): Route = new Route {
    override def apply(r: Request): FXFuture[Response] = f(r)
  }

  def withStackpane: Route => Route = (route: Route) => { request: Request => {
    route.apply(request).map {
      case x: View => x.mapContent(n => new StackPane(n))
      case x => x
    }
  }}

  def transitionFilter(seconds: Int): Route => Route = route => { request => {
    route.apply(request).map{
      case x: View =>
        val oldNode = request.oldContent
        val newNode = x.content
        val t = (seconds s)
        oldNode.opacity = 1.0
        newNode.opacity = 0.0
        oldNode.opacity := 0.0 in t
        newNode.opacity := 1.0 in t
        val res = new StackPane(oldNode,newNode)
        in(t) --> {res.children = List(newNode)}
        x.mapContent(x => res)
    }
  }}
  def mapViewFilter(request: Request, f: Node => Node): View = ???

  def viewFromNode(x: Node): View = ???
}
