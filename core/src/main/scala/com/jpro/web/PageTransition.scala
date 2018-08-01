package com.jpro.web

import simplefx.core._
import simplefx.all._

abstract class PageTransition {
  def doTransition(parent: StackPane, oldView: View, newView: View): Unit
}

object PageTransition {
  val t = 1.0 s
  object InstantTransition extends PageTransition {
    override def doTransition(parent: StackPane, oldView: View, newView: View): Unit = {
      if(oldView != null) parent.children := parent.children.filter(_ != oldView.realContent)
      parent.children := newView.realContent :: parent.children
    }
  }
  object FadingTransition extends PageTransition {
    override def doTransition(parent: StackPane, oldView: View, newView: View): Unit = {
      parent.children = if(oldView == null) List(newView.realContent) else List(oldView.realContent, newView.realContent)
      newView.realContent.opacity := 0.0
      newView.realContent.opacity := 1.0 in t
      if(oldView != null) oldView.realContent.opacity := 0.0 in t
      in(t) --> {
        if(oldView != null) parent.children = parent.children.filter(_ != oldView.realContent)
      }
    }
  }
  object MoveUp extends PageTransition {
    override def doTransition(parent: StackPane, oldView: View, newView: View): Unit = {
      assert(oldView != null, "oldView was null")
      parent.children = List(oldView.realContent, newView.realContent)
      newView.realContent.translateY = parent.height
      newView.realContent.translateY := 0.0 in t using Interpolator.EASE_BOTH
      nextFrame -->(in(t) --> {
        if(oldView != null) parent.children = parent.children.filter(_ != oldView.realContent)
      })
    }
  }
  object MoveDown extends PageTransition {
    override def doTransition(parent: StackPane, oldView: View, newView: View): Unit = {
      assert(oldView != null, "oldView was null")
      parent.children =  List(newView.realContent, oldView.realContent)
      oldView.realContent.translateY := parent.height in t using Interpolator.EASE_BOTH
      nextFrame -->(in(t) --> {
        if(oldView != null) parent.children = parent.children.filter(_ != oldView.realContent)
      })
    }
  }
}
