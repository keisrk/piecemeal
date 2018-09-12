package piecemeal.views.scene

import piecemeal.facade.twgl.{Constants, M4, TWGL, Primitives}
import piecemeal.routing.RootState
import piecemeal.views.{Program, Piece}
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

case class SceneView(model: ModelProperty[SceneModel], presenter: ScenePresenter) extends FinalView {
  import scalatags.JsDom.all._

  println("SceneView")
  /* Input components binding rotation/translation. */
  val sceneButton = UdashButton()(i(Glyphicon.play))/*UdashButton.toggle(active = rotated)(
    showIfElse(rotated)(i(Glyphicon.globe).render, i(Glyphicon.move).render))*/

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
      btn.listen { case _ => presenter.exec(piece.subProp(_.id).get, c.get) }
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
      div(BootstrapStyles.affix)(
        panel.render
      )
    )
  }
}
