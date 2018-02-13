package com.jpro.web

import simplefx.core._
import simplefx.all._

trait View extends Result {
  def title: String
  var url: String = null
  def realContent: Node = content
  def content: Node
  def saveScrollPosition = true
  def nativeScrolling = true
}

