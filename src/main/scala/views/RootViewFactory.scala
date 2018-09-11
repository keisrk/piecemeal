package piecemeal.views

import piecemeal.routing.{RootState, RoutingState}
import piecemeal.scene._
import io.udash._

/* Experimental */
import piecemeal.services.{RenderingContextService, SceneContextService}
import piecemeal.facade.twgl.{M4, Primitives, TWGL}
import piecemeal.scene
import scala.scalajs.js
/* Experimental */

class RootViewFactory (
  renderingService: RenderingContextService,
  sceneService: SceneContextService
) extends ViewFactory[RootState.type] {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def create(): (ContainerView, Presenter[RootState.type]) = {
    val presenter = new RootPresenter(/*model,*/ renderingService, sceneService)
    val view = new RootView(/*model,*/ presenter)
    println("RootState")
    (view, presenter)
  }
}
