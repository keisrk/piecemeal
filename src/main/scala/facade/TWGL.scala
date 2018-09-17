package piecemeal.facade.twgl

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, ScalaJSDefined}
import scala.scalajs.js.typedarray.{Float32Array, TypedArrayStatic}

import org.scalajs.dom.html.Canvas
import org.scalajs.dom.webgl.RenderingContext

@JSGlobal("twgl")
@js.native
object TWGL extends js.Object {
  def createProgramInfo(gl: RenderingContext, shaderSources: js.Array[String]): js.Dynamic = js.native
  def createBufferInfoFromArrays(gl: RenderingContext, arrays: js.Dynamic): js.Dynamic = js.native
  def createTexture(gl: RenderingContext, options: js.Dynamic): js.Dynamic = js.native
  def drawBufferInfo(gl: RenderingContext, bufferInfo: js.Dynamic): Unit = js.native
  def drawObjectList(gl: RenderingContext, objectsToDraw: js.Array[js.Dynamic]): Unit = js.native
  def resizeCanvasToDisplaySize(canvas: Canvas): Boolean = js.native
  def resizeCanvasToDisplaySize(canvas: Canvas, multiplier: Double): Boolean = js.native
  def setBuffersAndAttributes(gl: RenderingContext, setters: js.Dynamic, buffers: js.Dynamic): Unit = js.native
  def setUniforms(setters: js.Dynamic, values: js.Dynamic): Unit = js.native
}

@JSGlobal("twgl.m4")
@js.native
object M4 extends js.Object {
  def identity():Float32Array = js.native
  def identity(dst :Float32Array): Unit = js.native
  def inverse(m:Float32Array):Float32Array = js.native
  def lookAt(eye: js.Array[Double], target: js.Array[Double], up: js.Array[Double]): Float32Array = js.native
  def multiply(a:Float32Array, b:Float32Array):Float32Array = js.native
  def multiply(a:Float32Array, b:Float32Array, c:Float32Array): Unit = js.native
  def perspective(fieldOfViewYInRadians: Double, aspect: Double, zNear: Double, zFar: Double): Float32Array = js.native
  def rotationX(radian: Double): Float32Array = js.native
  def rotationX(radian: Double, dst: Float32Array): Unit = js.native
  def rotationY(radian: Double): Float32Array = js.native
  def rotationY(radian: Double, dst: Float32Array): Unit = js.native
  def rotationZ(radian: Double): Float32Array = js.native
  def rotationZ(radian: Double, dst: Float32Array): Unit = js.native
  def translation(v: js.Array[Double]): Float32Array = js.native
  def translation(v: js.Array[Double], dst: Float32Array): Unit = js.native
  def translate(m: Float32Array, v: js.Array[Double]): Float32Array = js.native
  def translate(m: Float32Array, v: js.Array[Double], dst: Float32Array): Unit = js.native
  def transpose(m: Float32Array):Float32Array = js.native
  def scaling(v: js.Array[Double]): Float32Array = js.native
  def scaling(v: js.Array[Double], dst: Float32Array): Unit = js.native
}

@JSGlobal("twgl.primitives")
@js.native
object Primitives extends js.Object {
  def createAugmentedTypedArray(numComponents: Int, numElements: Int): js.Dynamic = js.native
  def createAugmentedTypedArray(numComponents: Int, numElements: Int, opt_type: TypedArrayStatic): js.Dynamic = js.native
  def concatVertices(arrays: js.Array[js.Dynamic]): js.Dynamic = js.native
  def createCubeVertices(): js.Dynamic = js.native
  def createCubeVertices(size: Double): js.Dynamic = js.native
  def createCylinderVertices(radius: Double, height: Double, radialSubdivisions: Int, verticalSubdivisions: Int): js.Dynamic = js.native
  def createPlaneVertices(width: Double, depth: Double): js.Dynamic = js.native
  def createSphereVertices(radius: Double, subdivisionsAxis: Int, subdivisionsHeight: Int): js.Dynamic = js.native
  def duplicateVertices(arrays: js.Dynamic): js.Dynamic = js.native
  def reorientVertices(arrays: js.Dynamic, matrix: Float32Array): js.Dynamic = js.native
}
