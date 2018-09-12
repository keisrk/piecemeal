package piecemeal.facade.csg

import scala.scalajs.js

import scala.scalajs.js.annotation.{JSGlobal, ScalaJSDefined}
import scala.scalajs.js.typedarray.Float32Array


@js.native
@JSGlobal
class CSG() extends js.Object {
  def toPolygons(): js.Array[js.Dynamic] = js.native
  def union(csg: CSG): CSG = js.native
  def subtract(csg: CSG): CSG = js.native
}
@js.native
@JSGlobal
object CSG extends js.Object {
  override def clone(): CSG = js.native
  def cube(): CSG = js.native
  def sphere(): CSG = js.native
}
