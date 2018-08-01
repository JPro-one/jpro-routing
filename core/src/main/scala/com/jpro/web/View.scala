package com.jpro.web

import simplefx.core._
import simplefx.all._

trait View extends Result {
  def title: String
  def description: String
  var url: String = null
  var isMobile: Boolean = false
  lazy val realContent: Node = content
  def content: Node
  def saveScrollPosition = true
  def nativeScrolling = true
}

