package piecemeal.views.scene

import piecemeal.routing.{RootState, RoutingState, SceneState}
import piecemeal.scene._
import piecemeal.views.{Program, Piece}
import io.udash._

/* Experimental */
import piecemeal.services.{RenderingContextService, SceneContextService}
import piecemeal.facade.twgl.{M4, Primitives, TWGL}
import scala.scalajs.js
/* Experimental */

class SceneViewFactory (
  name: String,
  renderingService: RenderingContextService,
  sceneService: SceneContextService
) extends FinalViewFactory[SceneState] {
  import scala.concurrent.ExecutionContext.Implicits.global

  /* Experimental --> */
  val mgqLoc = M4.multiply(M4.translation(js.Array(0.0, 1.0, 0.0)), M4.rotationZ(90 * Math.PI / 180))
  val pnp01Loc = M4.translation(js.Array( 1.0, 0.0, 0.0))
  val pnp02Loc = M4.translation(js.Array(-1.0, 0.0, 0.0))
  val convLoc = M4.translation(js.Array(2.0, 1.0, 0.0))
  val act = /*new Environment(children = js.Array(*/
    new MGQ("my_cyl", M4.identity(), js.Array(
      DefaultServoMotor("my_servo", mgqLoc, js.Array(-1.0, 0.3, 1.2), js.Array(
        DefaultPickAndPlace("my_pnp01", pnp01Loc),
        DefaultPickAndPlace("my_pnp02", pnp02Loc),
        DefaultConveyor("my_conv", convLoc, 5),
      ))
    ))//))

  val programs = Seq[Program](
    Program(0)
  )
  val pieces = Seq[Piece](
    Piece("my_cyl",   Seq("on", "off")),
    Piece("my_servo", Seq("0", "1", "2")),
    Piece("my_conv",Seq("on")),
  )

  val model = ModelProperty(SceneModel(programs, pieces))
  override def create(): (FinalView, Presenter[SceneState]) = {
    println("SceneVF create")
    val presenter = new ScenePresenter(act, model, renderingService, sceneService)
    val view = new SceneView(model, presenter)

    (view, presenter)
  }
}
