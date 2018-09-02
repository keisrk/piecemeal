package piecemeal.routing

import piecemeal.ApplicationContext
import piecemeal.views.RootViewFactory
import io.udash._

class StatesToViewFactoryDef extends ViewFactoryRegistry[RoutingState] {
  def matchStateToResolver(state: RoutingState): ViewFactory[_ <: RoutingState] =
    state match {
      case RootState => new RootViewFactory(ApplicationContext.renderService, ApplicationContext.sceneService)
    }
}
