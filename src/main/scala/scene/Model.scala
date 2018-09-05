package piecemeal.scene.model
import scala.scalajs.js

trait SceneModel
case class Cylinder(id: String, children: js.Array[SceneModel]) extends SceneModel
case class ServoMotor(id: String, children: js.Array[SceneModel]) extends SceneModel
case class Conveyor(id: String) extends SceneModel
Cylinder("01", js.Array(
  ServoMotor("01", js.Array()),
  Conveyor("02"),
))
trait RenderingModel {
  def id: String
}
trait CylinderSpec extends RenderingModel {
  def majorVertices: js.Dynamic
  def minorVertices: js.Dynamic
}
case class ProductName(id: String) extends RenderingModel {
  def maj: js.Dynamic =
  def min: js.Dynamic =
}
object Cylinder {
  def getDrawable(s: RenderingModel): js.Dynamic
}
