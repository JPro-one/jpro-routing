package com.jpro.routing

import simplefx.experimental._

@FunctionalInterface
trait Route {
  def apply(r: Request): FXFuture[Response]

  def andThen(x: Route): Route = { request =>
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
  def filter(filter: Route=>Route): Route = filter(this)
}
