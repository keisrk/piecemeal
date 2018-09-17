package piecemeal.views.scene

import piecemeal.routing.{RootState, RoutingState, SceneState}
import piecemeal.scene._
import piecemeal.services.{RenderingContextService, SceneContextService, ModelDB}
import piecemeal.views.{Program, Piece}
import io.udash._

/* Experimental */
import piecemeal.facade.twgl.{M4, Primitives, TWGL}
import scala.scalajs.js
/* Experimental */
class SceneViewFactory (
  name: String,
  renderingService: RenderingContextService,
  sceneService: SceneContextService,
  modelDB: ModelDB
) extends FinalViewFactory[SceneState] {
  import scala.concurrent.ExecutionContext.Implicits.global

  def setup(e: Environment): Unit = {
    for (c <- e.children) setupActuator(c)
    for ((v, m) <- e.getRenderingInfo) renderingService.register(v, m)

    for (ph <- e.ioPhs) sceneService.registerStackOp(e.stack, ph)
    for ((fromId, toId) <- e.replaceSpec) sceneService.registerReplace(fromId, toId)
    for ((stepId, cmd) <- e.macroSpec) sceneService.registerMacro(stepId, cmd)

  }
  def setupActuator(a: Actuator): Unit = {
    for (step <- a.getSteps) sceneService.registerStep(step)
    for ((v, m) <- a.getRenderingInfo) renderingService.register(v, m)
    for (c <- a.children) setupActuator(c)
  }

  /* Experimental --> */
  val mgqLoc = M4.multiply(M4.translation(js.Array(0.0, 2.0, 0.0)), M4.rotationZ(90 * Math.PI / 180))
  val pnp01Loc = M4.translation(js.Array( 1.0, 0.0, 0.0))
  val pnp02Loc = M4.translation(js.Array(-1.0, 0.0, 0.0))
  val convLoc = M4.translation(js.Array(2.0, 1.0, 0.0))
  val act = new Environment(
    replaceSpec = js.Array(),
    macroSpec = js.Array((("macro"), js.Array(("depomy_conv_input", On), ("my_cyl", On), ("my_cyl", Off), ("my_cyl", On), ("my_cyl", Off), ("my_conv", On)))),
    ioPhs = js.Array("my_conv_input", "my_conv_output"),
    children = js.Array(
      new MGQ("my_cyl", M4.identity(), js.Array(
      )),
      DefaultServoMotor("my_servo", mgqLoc, js.Array(0.0, 0.5, 1.0), js.Array(
        DefaultPickAndPlace("my_pnp01", pnp01Loc),
        DefaultPickAndPlace("my_pnp02", pnp02Loc),
      )),
      new Conveyor("my_conv", convLoc, 5),
    ))
  val programs = Seq[Program](
    Program(0)
  )
  val pieces = Seq[Piece](
    Piece("my_cyl",   Seq("on", "off")),
    Piece("my_servo", Seq("0", "1", "2")),
    Piece("my_conv",  Seq("on")),
    Piece("depomy_conv_input",  Seq("on", "off")),
  )
  val model = ModelProperty(SceneModel(programs, pieces))
  override def create(): (FinalView, Presenter[SceneState]) = {
    println("SceneVF create")
    renderingService.reset()
    sceneService.reset()
    sceneService.setup(act.getSceneTree)
    setup(act)

    /* ModelDB */
    modelDB.setup()
    val presenter = new ScenePresenter(model, renderingService, sceneService)
    val view = new SceneView(model, presenter)

    (view, presenter)
  }
}
