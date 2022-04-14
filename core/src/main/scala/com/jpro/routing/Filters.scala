package com.jpro.routing
import simplefx.all

object Filters {
  def FullscreenFilter(fullscreenValue: Boolean): Filter = { route => { request =>
      route.apply(request).map {
        case x: View =>
          new View {
            override def title: String = x.title
            override def description: String = x.description
            override def content: all.Node = x.content

            override def fullscreen: Boolean = fullscreenValue
          }
        case x => x
      }
    }
  }

}
