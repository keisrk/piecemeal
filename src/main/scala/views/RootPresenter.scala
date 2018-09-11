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
  //model: ModelProperty[RootModel],
  val renderingService: RenderingContextService,
  val sceneService: SceneContextService
)(implicit ec: ExecutionContext) extends Presenter[RootState.type]{

  override def handleState(state: RootState.type): Unit = {
//    render()
  }
  def renderingSetup(canvas: html.Canvas): Future[Unit] = {
    renderingService.setup(canvas).map(_ => ()).andThen {
      case Success(_) => {render()}
      case Failure(e: RuntimeException) => println(e)
    }
  }
  def render(): Unit = {
    window.requestAnimationFrame(animation);
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

