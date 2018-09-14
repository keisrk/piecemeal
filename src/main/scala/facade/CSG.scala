package piecemeal.facade.csg

import scala.scalajs.js

import scala.scalajs.js.annotation.{JSGlobal, ScalaJSDefined}
import scala.scalajs.js.typedarray.Float32Array

@js.native
@JSGlobal
class CSG() extends js.Object {
  def toPolygons(): js.Array[js.Dynamic] = js.native
  def union(csg: CSG): CSG = js.native
  def intersect(csg: CSG): CSG = js.native
  def subtract(csg: CSG): CSG = js.native
}

@js.native
@JSGlobal
object CSG extends js.Object {
  override def clone(): CSG = js.native
  def fromPolygons(polygons: js.Array[Polygon]): CSG = js.native
  def cube(options: js.Dynamic): CSG = js.native
  def sphere(options: js.Dynamic): CSG = js.native
  def cylinder(options: js.Dynamic): CSG = js.native

  @js.native
  class Vector(x: Double, y: Double, z: Double) extends js.Object
  @js.native
  class Vertex(pos: Vector, normal: Vector) extends js.Object
  @js.native
  class Polygon(vertices: js.Array[Vertex], shared: js.Dynamic = js.Dynamic.literal()) extends js.Object
}
