package piecemeal.views

import piecemeal.facade.twgl.{Constants, M4, TWGL, Primitives}
import piecemeal.routing.RootState
import scala.concurrent.{ExecutionContext, Future}

import io.udash._
import io.udash.css.CssView._
import io.udash.bootstrap.{UdashBootstrap, BootstrapStyles}
import io.udash.bootstrap.button._//UdashButton
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

case class RootView(model: ModelProperty[RootModel], presenter: RootPresenter) extends FinalView {
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

  /* Input components binding rotation/translation. */
  val sceneButton = UdashButton.toggle(active = rotated)(
    showIfElse(rotated)(i(Glyphicon.globe).render, i(Glyphicon.move).render))

  val playButton = UdashButton()(i(Glyphicon.play))
  playButton.listen{ case _ => presenter.exec("macro",  "on")}

  val stopButton = UdashButton()(i(Glyphicon.stop))

  val toolbar = UdashButtonToolbar(
    UdashButtonGroup()(
      playButton.render,
      stopButton.render,
    ).render,
    UdashButtonGroup()(
      sceneButton.render,
    ).render,
  )

  val homePanel = div()(
    toolbar.render,
  ).render

  def makeButtonGroup(piece: ModelProperty[Piece]): Element = 
    UdashButtonGroup.reactive(piece.subSeq(_.commands))(c => {
      val btn = UdashButton()(c.get)
      btn.listen{case _ => presenter.exec(piece.subProp(_.id).get, c.get)}
      btn.render
    }).render

  val piecesPanel = {
    UdashListGroup(model.subSeq(_.pieces))((a) =>
      div()(label(bind(a.asModel.subProp(_.id))).render,
        div(`class` := "text-right")(
          makeButtonGroup(a.asModel)
        ).render
      ).render
    )
  }

  val panels = SeqProperty[String]("Home", "Pieces", "Setting")

  val console = UdashCollapse()(
    div(BootstrapStyles.Well.wellSm)(
      // Tab panel titles
      UdashNav.tabs()(panels)(
        elemFactory = (panel) => {
          a(BootstrapTags.dataToggle := "tab",
            href := "#" ++ panel.get,
          )(panel.get).render
        }
      ).render,

      // Tab panel contents
      div(`class` := "tab-content")(
        div(id := "Home", `class` := "tab-pane")(
          div(BootstrapStyles.Well.wellSm)(
            homePanel.render
          ).render,
        ).render,
        div(id := "Pieces", `class` := "tab-pane")(
          div(BootstrapStyles.Well.wellSm)(
            piecesPanel.render,
          ).render,
        )(
        ).render,
        div(id := "Setting", `class` := "tab-pane")(
          div(BootstrapStyles.Well.wellSm)(
            label("Coming soon..."),
          ).render,
        ).render,
      ).render
    ).render
  )

  val menuButton = UdashButton()(console.toggleButtonAttrs(), i(Glyphicon.menuHamburger))

  val panel = UdashPanel()(
    div(`class` := "text-right")(
      menuButton.render,
    ),
    console.render,
  )

  override def getTemplate: Modifier = {
    div(BootstrapStyles.containerFluid)(
      div()(
        glCanvas,
      ),
      div(BootstrapStyles.affix)(
        panel.render
      )
    )
  }
}
