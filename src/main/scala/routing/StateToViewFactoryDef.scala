package piecemeal.routing

import piecemeal.ApplicationContext
import piecemeal.views.RootViewFactory
import piecemeal.views.scene.SceneViewFactory
import io.udash._

class StatesToViewFactoryDef extends ViewFactoryRegistry[RoutingState] {
  def matchStateToResolver(state: RoutingState): ViewFactory[_ <: RoutingState] =
    state match {
      case RootState => new RootViewFactory(ApplicationContext.renderService, ApplicationContext.sceneService)
      case SceneState(name) => new SceneViewFactory(name, ApplicationContext.renderService, ApplicationContext.sceneService)
    }
}
