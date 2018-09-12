package piecemeal.routing

import io.udash._

sealed abstract class RoutingState(val parentState: Option[ContainerRoutingState]) extends State {
  override type HierarchyRoot = RoutingState
}

sealed abstract class ContainerRoutingState(parentState: Option[ContainerRoutingState])
  extends RoutingState(parentState) with ContainerState

sealed abstract class FinalRoutingState(parentState: Option[ContainerRoutingState])
  extends RoutingState(parentState) with FinalState

case object RootState extends ContainerRoutingState(None)
case object ErrorState extends FinalRoutingState(Some(RootState))
case class SceneState(name: String) extends FinalRoutingState(Some(RootState))
