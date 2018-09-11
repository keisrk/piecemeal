/*
package piecemeal.scene.model
import scala.scalajs.js

trait SceneModel
case class Fixture(id: String) extends SceneModel
case class Group(id: String, children: js.Array[SceneModel]) extends SceneModel
case class Actuator(id: String, children: js.Array[SceneModel]) extends SceneModel

Group("root", js.Array(
  Actuator("cylinder_01", js.Array(Fixture("end_01"))),
  Actuator("servo_01", js.Array(Fixture("end_02"))),
))
(
  CylinderModel("cylinder_01", loc, ProdSpec),
  ServoMortorModel("servo_01", loc, ProdSpec),
  ConveyorModel("servo_01", loc, slot, ProdSpec),
  FixtureModel("end_01", loc, majorV),
)
ProductSpec(range, incre, decre, majorV, minorV)

object CylinderModel {
  def prodname = ProductSpec(range, incre, decre, majorV, minorV)
}
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
 */
