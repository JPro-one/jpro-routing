package com.jpro.routing.filter.container

import com.jpro.routing.{Filter, Request, View}
import simplefx.core._
import simplefx.experimental._
import simplefx.all._
import java.util.function.Supplier

object ContainerFilter {

  private val factoryKey = new Object()
  def create(supplier: Supplier[Container]): Filter = {
    create(new ContainerFactory {
      override def isContainer(x:  Node): Boolean = {
        x.getProperties.get(factoryKey) == this
      }
      override def createContainer(): _root_.javafx.scene.Node = {
        val res = supplier.get().asInstanceOf[Node]
        res.getProperties.put(factoryKey, this)
        res
      }
      override def setContent(c:  _root_.javafx.scene.Node, x:  _root_.javafx.scene.Node): Unit = {
        c.asInstanceOf[Container].setContent(x)
      }
      override def getContent(c:  _root_.javafx.scene.Node): _root_.javafx.scene.Node = {
        c.asInstanceOf[Container].getContent()
      }
      override def setRequest(c:  _root_.javafx.scene.Node, r:  _root_.com.jpro.routing.Request): Unit = {
        c.asInstanceOf[Container].setRequest(r)
      }
    })
  }

  def create[A <: javafx.scene.Node](containerLogic: ContainerFactory): Filter = route => request => {
    var container: Node = null
    val request2: Request = {
      val oldContentV = request.oldContent.get()
      if (oldContentV != null && containerLogic.isContainer(oldContentV)) {
        container = oldContentV
        request.mapContent(x => containerLogic.getContent(container))
      } else {
        request
      }
    }
    val r = route(request2)
    if (r == null) null
    else r.map {
      case view: View =>
        if (container == null) {
          container = containerLogic.createContainer()
        }
        view.mapContent(x => {
          containerLogic.setRequest(container, request)
          containerLogic.setContent(container, view.content)
          container
        })
      case x => x
    }
  }
}
