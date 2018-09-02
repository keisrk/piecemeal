package piecemeal.services
import piecemeal.facade.twgl.{Constants, M4, TWGL}
import piecemeal.scene.{SceneTree, Leaf, Node, PlaceHolder, Step, Replace, On, Off, Move, Command}
import scala.scalajs.js.typedarray.Float32Array
import scala.collection.mutable.HashMap
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.async.Async.{async, await}
import scalajs.concurrent.JSExecutionContext.Implicits.queue

class SceneContextService()(implicit ec: ExecutionContext){
  private var sceneContext: Option[SceneTree] = None
  private val worldMatrix = M4.identity()
  private val placeHolderMap: HashMap[String, PlaceHolder] = HashMap()
  private val stepMap: HashMap[String, Step] = HashMap()

  def currentContext: Option[SceneTree] = sceneContext
  def getCurrentContext: SceneTree = sceneContext
    .getOrElse(throw new RuntimeException("SceneContext is yet to be made."))

  def reset(): Unit = {
    sceneContext = None
    placeHolderMap.clear()
    stepMap.clear()
    M4.identity(worldMatrix)
  }
  def rotationY(y: Double): Unit = {
    M4.rotationY(y * Math.PI / 180.0, worldMatrix)
  }
  // Really necessary? def setup(model: DescriptionModel): Future[SceneTree] = {
  def setup(st: SceneTree): Future[SceneTree] = {
    collectPlaceHolder(st)
    if (sceneContext.isDefined) {
      Future{ sceneContext.get }
    } else {
      Future {
        val ctx = st
        sceneContext = Some(ctx)
        ctx
      }
    }
  }


  def update(): Unit = {
    stepMap.values.foreach(_.tik())
    getCurrentContext.updateWorldMatrix(worldMatrix)
  }
  def registerPlaceHolder(ph: PlaceHolder): Unit = placeHolderMap.+=((ph.getId, ph))
  def getPlaceHolder(id: String): PlaceHolder = placeHolderMap(id)
  def collectPlaceHolder(st: SceneTree): Unit = st match {
    case l: Leaf => {}
    case n: Node => for (c <- n.children) collectPlaceHolder(c)
    case p: PlaceHolder => registerPlaceHolder(p)
  }
  def registerStep(step: Step): Unit = stepMap.+=((step.getId, step))
  def getStep(id: String): Step = stepMap(id)

  def registerReplace(fromId: String, toId: String): Unit = {
    val r = Replace(fromId ++ "_" ++ toId, getPlaceHolder(fromId), getPlaceHolder(toId))
    registerStep(r)
  }
}
