package com.jpro.web

import simplefx.core._
import simplefx.all._

trait NodeView extends View { this: Node =>
  final override def content = this
}
