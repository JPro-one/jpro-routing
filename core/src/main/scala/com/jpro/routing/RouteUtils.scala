package com.jpro.routing

import simplefx.all
import simplefx.all._
import simplefx.core._
import simplefx.experimental._

import java.util.function.Supplier

object RouteUtils {

  val EmptyRoute: Route = (x) => null

  def get(path: String, f: Supplier[Response]): Route = (request: Request) => if(request.path == path) FXFuture.unit(f.get()) else null
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

  def transitionFilter(seconds: Double): Route => Route = route => { request => {
    route.apply(request).map{
      case x: View =>
        val oldNode = request.oldContent
        val newNode = x.content
        val t = (seconds s)
        if(oldNode == null) {
          x
        } else {
          oldNode.opacity = 1.0
          newNode.opacity = 0.0
          oldNode.opacity := 0.0 in t
          newNode.opacity := 1.0 in t
          val res = new StackPane(oldNode,newNode)
          in(t) --> {res.children = List(newNode)}
          x.mapContent(x => res)
        }
      case x => x
    }
  }}
  def sideTransitionFilter(seconds: Double): Route => Route = route => { request => {
    route.apply(request).map{
      case x: View =>
        val oldNode = request.oldContent
        val newNode = x.content
        val t = (seconds s)
        if(oldNode == null) {
          x
        } else {
          val startTime: Time = systemTime
          def timeLeft: Time = (startTime + (seconds * second)) - time
          def progress: Double = 1.0 - (timeLeft / (seconds * second))
          val res = new StackPane(oldNode,newNode)
          val finishedB: B[Boolean] = Bindable(false)
          when(!finishedB && timeLeft > (0 s)) ==> {
            oldNode.translateX <-- (-progress * res.width)
            newNode.translateX <-- ((1 - progress) * res.width)
          }
          onceWhen(timeLeft <= (0. s)) --> {
            oldNode.translateX = 0
            newNode.translateX = 0
            finishedB := true
          }
          in(t) --> {res.children = List(newNode)}
          x.mapContent(x => res)
        }
      case x => x
    }
  }}

  def mapViewFilter(request: Request, f: Node => Node): View = ???

  def viewFromNode(x: Node): View = new View {
    override def title: String = ""
    override def description: String = ""
    override def content: all.Node = x
  }
}
