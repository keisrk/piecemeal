package piecemeal.services

import piecemeal.facade.twgl.{Constants, M4, TWGL}

import org.scalajs.dom.html.Canvas
import org.scalajs.dom.webgl.RenderingContext
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class DrawInfo(val vertices: js.Dynamic){
  private var obj: Option[js.Dynamic] = None
  def isDefined: Boolean = obj.isDefined
  def set(drawInfo: js.Dynamic): Unit = { obj = Some(drawInfo) }
  def get: js.Dynamic = obj.get
}

class RenderingContextService()(implicit ec: ExecutionContext) {
  private var renderingContext: Option[RenderingContext] = None  
  private var programInfo: Option[js.Dynamic] = None  
  private var uniforms: Option[js.Dynamic] = None                
  private val constants = new Constants
  private val objectList = js.Array[(DrawInfo, Float32Array)]()
  def isDefined: Boolean = renderingContext.isDefined && programInfo.isDefined && uniforms.isDefined
  def currentContext: Option[RenderingContext] = renderingContext
  def getCurrentContext: RenderingContext = renderingContext
    .getOrElse(throw new RuntimeException("RenderingContext is yet to be made."))
  def currentProgramInfo: Option[js.Dynamic] = programInfo
  def getCurrentProgramInfo: js.Dynamic = programInfo.getOrElse(throw new RuntimeException("ProgramInfo is yet to be made."))
  def currentUniforms: Option[js.Dynamic] = uniforms
  def getCurrentUniforms: js.Dynamic = uniforms.getOrElse(throw new RuntimeException("Uniforms is yet to be made."))

  def translationZ(z: Double): Unit = {
    //M4.translate(constants.world, js.Array(-0.01 * x, 0.0, 0.0))
    constants.posZ = 0.01 * z -10.0
  }

  def reset(): Unit = {
    objectList.clear()
    /*
    renderingContext = None
    programInfo = None
    uniforms = None
     */
  }
  /** Get rendering context and saves returned context. */
  def setup(canvas: Canvas): Future[RenderingContext] = {
    if (renderingContext.isDefined) Future{ renderingContext.get }
    else {
      Future{
        val rawCtx = canvas.getContext("webgl")
        if (rawCtx == null) {
          throw new RuntimeException("Canvas does not support WebGL.")
        } else {
          println("getContext success")
          val ctx = rawCtx.asInstanceOf[RenderingContext]
          renderingContext = Some(ctx)
          programInfo = Some(TWGL.createProgramInfo(ctx, js.Array(constants.vs, constants.fs)))
          uniforms = Some(constants.uniforms(ctx))
          setupDrawInfoList()
          ctx
        }
      }
    }
  }
  def setupDrawInfo(d: DrawInfo): Unit = {
    if (isDefined && !d.isDefined) {
      val bufferInfo = TWGL.createBufferInfoFromArrays(getCurrentContext, d.vertices)
      val drawInfo = js.Dynamic.literal(
        "programInfo" -> getCurrentProgramInfo,
        "bufferInfo" -> bufferInfo,
        "uniforms" -> constants.cloneUniforms(getCurrentUniforms))
      d.set(drawInfo)
    }
  }
  def setupDrawInfoList(): Unit = {
    if (isDefined) for ((d, _) <- objectList) setupDrawInfo(d)
  }
  def register(vertices: js.Dynamic, m: Float32Array): Unit = {
    val drawInfo = new DrawInfo(vertices)
    if (isDefined) setupDrawInfo(drawInfo)
    objectList.push((drawInfo, m))
  }

  def syncObjectList(viewProjection: Float32Array): Unit =
    for ((obj, m) <- objectList) {
      obj.get.uniforms.u_world = m
      obj.get.uniforms.u_worldInverseTranspose = M4.transpose(M4.inverse(obj.get.uniforms.u_world.asInstanceOf[Float32Array]))
      M4.multiply(viewProjection, obj.get.uniforms.u_world.asInstanceOf[Float32Array], obj.get.uniforms.u_worldViewProjection.asInstanceOf[Float32Array])
    }

  def update() = {
    /* Meaningless boilerplate */
    constants.renderPreamble(getCurrentContext)
    val (camera, view, viewProjection, world) = constants.renderUniforms(getCurrentContext)
    getCurrentUniforms.u_viewInverse = constants.camera
    getCurrentUniforms.u_world = constants.world
    getCurrentUniforms.u_worldInverseTranspose = M4.transpose(M4.inverse(constants.world))
    getCurrentUniforms.u_worldViewProjection = M4.multiply(constants.viewProjection, constants.world)

    syncObjectList(constants.viewProjection)

    TWGL.drawObjectList(getCurrentContext, objectList.map(_._1.get))
  }
}
