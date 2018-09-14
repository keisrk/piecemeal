package piecemeal.views.scene

import piecemeal.scene.{On, Off, Move}
import piecemeal.routing.{RootState, SceneState}
import piecemeal.services.{RenderingContextService, SceneContextService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.scalajs.js

import org.scalajs.dom.html
import org.scalajs.dom.window

import io.udash._

/* Experimental */
import piecemeal.services.{RenderingContextService, SceneContextService}
import piecemeal.facade.twgl.{M4, Primitives, TWGL}
import piecemeal.scene._
import scala.scalajs.js
/* Experimental */

class ScenePresenter(
  model: ModelProperty[SceneModel],
  val renderingService: RenderingContextService,
  val sceneService: SceneContextService
)(implicit ec: ExecutionContext) extends Presenter[SceneState] {

  override def handleState(state: SceneState): Unit = state match {
    case SceneState(name) => {
      println(name);
    }
  }

  def exec(id: String, cmd: String) = cmd match {
    case "on" => sceneService.getStep(id).exec(On)
    case "off" => sceneService.getStep(id).exec(Off)
    case i =>  sceneService.getStep(id).exec(Move(i.toInt))
  }
}

