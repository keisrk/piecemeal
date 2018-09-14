package piecemeal.scene.volatile
import piecemeal.scene.{Description, SceneTree, Step}
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

case class Environment(id:String = "Environment", actuator: js.Array[Actuator])

trait Actuator
case class Cylinder(id: String, productId: String, location: Float32Array,
  children: js.Array[Actuator]) extends Actuator
object Cylinder {
  def apply(id: String, productId: String, location: Float32Array, fixture: js.Dynamic,
  children: js.Array[Actuator]): Cylinder = Cylinder(id, productId, location, children)
}
case class ServoMotor(id: String, productId: String, location: Float32Array,
  children: js.Array[Actuator]) extends Actuator

abstract class ModelFactory(d: Description) {
  def getSceneTree: SceneTree
  def getSteps: js.Array[Step]
  def getRenderingInfo: js.Array[(js.Dynamic, Float32Array)]
}
