package com.jpro.routing

import simplefx.experimental._

@FunctionalInterface
trait Route {
  def apply(r: Request): FXFuture[Response]

  def and(x: Route): Route = { request =>
    val r = apply(request)
    if(r == null) {
      x.apply(request)
    } else {
      r.flatMap{ r =>
        if(r == null) {
          x.apply(request)
        } else FXFuture.unit(r)
      }
    }
  }
  def path(path: String, route: Route): Route = and((r: Request) => {
    if(r.path.startsWith(path + "/")) {
      val r2 = r.copy(path = r.path.drop(path.length))
      route.apply(r2)
    } else {
      FXFuture.unit(null)
    }
  })
  def filter(filter: Route=>Route): Route = filter(this)
}
