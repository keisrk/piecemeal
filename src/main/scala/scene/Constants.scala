package piecemeal.scene

import piecemeal.facade.twgl.{M4, Primitives}
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class MGQ(id: String, location: Float32Array, children: js.Array[Actuator])
    extends DefaultCylinder(id, location, children) {
  override def rangePerCycle = 1.0
  override def majorVertices = {
    val v = Primitives.createCubeVertices()
    Primitives.reorientVertices(v, M4.translation(js.Array(0.0, -0.5, 0.0)))
    Primitives.reorientVertices(v, M4.scaling(js.Array(1.8, 1.2, 1.0)))
    v
  }
  override def minorVertices = {
    val left = Primitives.createCylinderVertices(0.2, 1.2, 24, 12)
    Primitives.reorientVertices(left, M4.translation(js.Array(-0.5, -0.6, 0.0)))
    val right = Primitives.createCylinderVertices(0.2, 1.2, 24, 12)
    Primitives.reorientVertices(right, M4.translation(js.Array( 0.5, -0.6, 0.0)))
    val middle = Primitives.createCylinderVertices(0.2, 1.2, 24, 12)
    Primitives.reorientVertices(middle, M4.translation(js.Array( 0.0, -0.6, 0.0)))

    val plate = Primitives.createCubeVertices()
    Primitives.reorientVertices(plate, M4.translation(js.Array(0.0, 0.5, 0.0)))
    Primitives.reorientVertices(plate, M4.scaling(js.Array(1.8, 0.3, 1.0)))
    val v = Primitives.concatVertices(js.Array(left, right, middle, plate))
    v
  }
}

