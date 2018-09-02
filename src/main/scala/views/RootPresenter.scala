package piecemeal.views

import piecemeal.scene.{On, Off, Move}
import piecemeal.routing.{RootState}
import piecemeal.services.{RenderingContextService, SceneContextService/*, Procedure*/}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.scalajs.js

import org.scalajs.dom.html
import org.scalajs.dom.window

import io.udash._

class RootPresenter(
  model: ModelProperty[RootModel],
  val renderingService: RenderingContextService,
  val sceneService: SceneContextService
)(implicit ec: ExecutionContext) extends Presenter[RootState.type]{

  override def handleState(state: RootState.type): Unit = {
  }

  def render(): Unit ={
    window.requestAnimationFrame(animation);
  }
/*
  val proc = new Procedure(sceneService, js.Array(
    ("macro", On),
    ("my_cyl", On),
    ("my_cyl", Off)))
 */
  def exec(id: String, cmd: String) = cmd match {
    case "on" => sceneService.getStep(id).exec(On)
    case "off" => sceneService.getStep(id).exec(Off)
    case i =>  sceneService.getStep(id).exec(Move(i.toInt))
  }
  /* <-- Experimental */
  def renderingSetup(canvas: html.Canvas): Future[Unit] = {
    renderingService.setup(canvas).map(_ => ()).andThen {
      case Success(_) => {render()}
      case Failure(e: RuntimeException) => println(e)
    }
  }
  def animation(time: Double): Int = {
    if (sceneService.currentContext.isDefined) {
      sceneService.update()
    }
    if (renderingService.currentContext.isDefined) {
      renderingService.update()
    }
    window.requestAnimationFrame(animation)
  }
}

