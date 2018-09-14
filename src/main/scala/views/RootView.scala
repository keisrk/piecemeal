package piecemeal.views

import piecemeal.facade.twgl.{M4, TWGL, Primitives}
import piecemeal.routing.RootState
import scala.concurrent.{ExecutionContext, Future}

import io.udash._
import io.udash.css.CssView._
import io.udash.bootstrap.{UdashBootstrap, BootstrapStyles}
import io.udash.bootstrap.button._
import io.udash.bootstrap.collapse.UdashCollapse
import io.udash.bootstrap.navs.UdashNav
import io.udash.bootstrap.panel.UdashPanel
import io.udash.bootstrap.BootstrapTags
import io.udash.bootstrap.form.{InputGroupSize, UdashForm, UdashInputGroup}

import io.udash.bootstrap.utils.UdashListGroup

import io.udash.bootstrap.utils.Icons.{Glyphicon}
import org.scalajs.dom.{UIEvent, MouseEvent, Element}
import org.scalajs.dom.webgl.RenderingContext
import org.scalajs.dom.window


import scala.scalajs.js

case class RootView(/*model: ModelProperty[RootModel],*/ presenter: RootPresenter) extends ContainerView {
  import scalatags.JsDom.all._

  val glCanvas = canvas(BootstrapStyles.pullLeft, style := """
width: 100vw;
height: 100vh;
display: block;
"""
  ).render

  presenter.renderingSetup(glCanvas)

  /* Modeling Canvas rotation/translation. */
  /* FIXME: Any logic cannot stay in this file. It must be in RootPresenter. */
  /* variables binding rotation/translation. */
  val isDown = Property(false)
  val rotated = Property(true)

  val rotateMouseOrigin = Property.blank[Double]
  val rotateMouseCurrent = Property.blank[Double]
  val rotateY = rotateMouseCurrent.combine(rotateMouseOrigin)(_ - _)

  val translateMouseOrigin = Property.blank[Double]
  val translateMouseCurrent = Property.blank[Double]//(-10.0)//
  val translateZ = translateMouseCurrent.combine(translateMouseOrigin)(_ - _)

  /* Mouse Events to update rotation/translation. */
  glCanvas.onmouseup = ((me: MouseEvent) => {
    isDown.set(false)
  })

  glCanvas.onmousedown = ((me: MouseEvent) => {
    isDown.set(true)
    if (rotated.get) {
      val cache = rotateY.get
      rotateMouseOrigin.set(me.clientX - cache)
      rotateMouseCurrent.set(me.clientX)
    } else {
      val cache = translateZ.get
      translateMouseOrigin.set(me.clientY - cache)
      translateMouseCurrent.set(me.clientY)
    }})

  glCanvas.onmousemove = ((me: MouseEvent) =>
    if (isDown.get) {
      if (rotated.get) {
        rotateMouseCurrent.set(me.clientX)
      } else {
        translateMouseCurrent.set(me.clientY)
      }
    })

  translateZ.listen((z: Double) => {
    presenter.renderingService.translationZ(z)
  })
  rotateY.listen((y: Double) => {
    presenter.sceneService.rotationY(y)
  })

  override def getTemplate: Modifier = {
    div(BootstrapStyles.containerFluid)(
      div()(
        glCanvas,
      ),
      childViewContainer,
    )
  }
}
