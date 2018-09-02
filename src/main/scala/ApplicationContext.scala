package piecemeal

import piecemeal.routing.{RoutingRegistryDef, RoutingState, StatesToViewFactoryDef}
import piecemeal.services.{RenderingContextService, SceneContextService}

import io.udash._

object ApplicationContext {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val routingRegistry = new RoutingRegistryDef
  private val viewFactoryRegistry = new StatesToViewFactoryDef
  val renderService = new RenderingContextService
  val sceneService = new SceneContextService
  val application = new Application[RoutingState](
    routingRegistry, viewFactoryRegistry, WindowUrlPathChangeProvider
  )
}
