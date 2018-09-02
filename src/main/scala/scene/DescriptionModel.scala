package piecemeal.scene

import io.udash._
import piecemeal.facade.twgl.{M4, Primitives}
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

trait Description {
  def id: String
  def getSceneTree: SceneTree
  def getRenderingInfo: js.Array[(js.Dynamic, Float32Array)]
}
abstract class Work(id: String) extends Description {
  def majorVertices: js.Dynamic
  def getSceneTree: SceneTree = Leaf()
  def getRenderingInfo: js.Array[(js.Dynamic, Float32Array)]
}

case class Environment(id:String = "Environment", children: js.Array[Actuator]) extends Description {
  private val majorSt = Node()
  private val entryPoint = PlaceHolder("entry_point")
  private val endPoint = PlaceHolder("end_point")
  private val works = js.Array[Leaf]()

  for (c  <- children) c.getSceneTree match {
    case n: Node => n.setParent(majorSt)
    case p: PlaceHolder => p.setParent(majorSt)
    case _ => {}
  }
  def getSceneTree: SceneTree = majorSt

  val getRenderingInfo: js.Array[(js.Dynamic, Float32Array)] = js.Array()

  for (i <- 0 until 10) {
    val work = Leaf()
    work.updateLocalMatrix(M4.translation(js.Array(10.0, i * 0.5, 0.0)))
    works.push(work)
    getRenderingInfo.push((Primitives.createCubeVertices(0.3), work.worldMatrix))
  }
}

trait Actuator {
  def id: String
  def getSceneTree: SceneTree
  def getSteps: js.Array[Step]
  def getRenderingInfo: js.Array[(js.Dynamic, Float32Array)]
  def children: js.Array[Actuator]
}

abstract class Cylinder extends Actuator {
  def id: String
  def location: Float32Array
  def children: js.Array[Actuator]

  def rangePerCycle: Double
  def tikPerCycle: Int
  def increment: Float32Array
  def decrement: Float32Array
  def majorVertices: js.Dynamic
  def minorVertices: js.Dynamic

  private val majorSt = Node()
  private val minorSt = Node()
  majorSt.updateLocalMatrix(location)
  minorSt.setParent(majorSt)
  for (c  <- children) c.getSceneTree match {
    case n: Node => n.setParent(minorSt)
    case p: PlaceHolder => p.setParent(minorSt)
    case _ => {}
  }

  def getSceneTree: SceneTree = majorSt
  def getSteps: js.Array[Step] = {
    val countPerTik = 1.0 / tikPerCycle.toDouble
    val step = Toggle(id, minorSt, countPerTik, increment, decrement)
    val stepInit = Init(id + "_init", minorSt)
    js.Array(step, stepInit)
  }
  def getRenderingInfo: js.Array[(js.Dynamic, Float32Array)] = {
    js.Array(
      (majorVertices, majorSt.worldMatrix),
      (minorVertices, minorSt.worldMatrix))
  }
}

case class DefaultCylinder(id: String, location: Float32Array, children: js.Array[Actuator])
    extends Cylinder {
  def rangePerCycle = 1.0
  def tikPerCycle = 30

  private val rangePerTik = rangePerCycle / tikPerCycle.toDouble

  def increment = M4.translation(js.Array(0, rangePerTik, 0))
  def decrement = M4.translation(js.Array(0, -1 * rangePerTik, 0))
  def majorVertices = Primitives.createCubeVertices()
  def minorVertices = Primitives.createCubeVertices()
}

abstract class ServoMotor extends Actuator {
  def id: String
  def location: Float32Array
  def positions: js.Array[Double]
  def children: js.Array[Actuator]

  def rangePerCycle: Double
  def tikPerCycle: Int
  def increment: Float32Array
  def decrement: Float32Array
  def majorVertices: js.Dynamic
  def minorVertices: js.Dynamic

  private val countPerTik = 1.0 / tikPerCycle.toDouble
  private val shiftPositions: js.Array[Double] = positions.map((i: Double) => i / rangePerCycle.toDouble)
  private val majorSt = Node()
  private val minorSt = Node()
  majorSt.updateLocalMatrix(location)
  minorSt.setParent(majorSt)
  for (c  <- children) c.getSceneTree match {
    case n: Node => n.setParent(minorSt)
    case p: PlaceHolder => p.setParent(minorSt)
    case _ => {}
  }

  def getSceneTree: SceneTree = majorSt
  def getSteps: js.Array[Step] = {
    val step = Shift(id, minorSt, countPerTik, shiftPositions, increment, decrement)
    val stepInit = Init(id + "_init", minorSt)
    js.Array(step, stepInit)
  }
  def getRenderingInfo: js.Array[(js.Dynamic, Float32Array)] = {
    js.Array(
      (majorVertices, majorSt.worldMatrix),
      (minorVertices, minorSt.worldMatrix))
  }
}

case class DefaultServoMotor(id: String, location: Float32Array, positions: js.Array[Double],
  children: js.Array[Actuator]) extends ServoMotor {
  def rangePerCycle = 3.0
  def tikPerCycle = 30
  private val rangePerTik = rangePerCycle / tikPerCycle.toDouble

  def increment = M4.translation(js.Array(0, rangePerTik, 0))
  def decrement = M4.translation(js.Array(0, -1 * rangePerTik, 0))

  def majorVertices = Primitives.createCubeVertices()
  def minorVertices = Primitives.createCubeVertices()
}

abstract class PickAndPlace(id: String, location: Float32Array) extends Actuator {
  val children: js.Array[Actuator] = js.Array()
  private val majorSt = Node()
  private val minorSt = PlaceHolder(id)
  majorSt.updateLocalMatrix(location)
  minorSt.setParent(majorSt)

  private val stepInit = Init(id + "_init", minorSt)

  def majorVertices: js.Dynamic

  def getPlaceHolder: PlaceHolder = minorSt
  def getSceneTree: SceneTree = majorSt
  def getSteps: js.Array[Step] = js.Array(stepInit)
  def getRenderingInfo: js.Array[(js.Dynamic, Float32Array)] = {
    js.Array(
      (majorVertices, majorSt.worldMatrix))
  }
}
case class DefaultPickAndPlace(id: String, location: Float32Array) extends PickAndPlace(id, location) {
  def majorVertices = Primitives.createCubeVertices(0.1)
}
case class Queue(id: String, location: Float32Array, slots: Int) extends Actuator {

  val children: js.Array[Actuator] = js.Array()
  val majorSt = Node()
  majorSt.updateLocalMatrix(location)

  val middleSt = Node()
  middleSt.setParent(majorSt)

  val jog: Jog = Jog(id ++ "_jog" , middleSt, 0.05, M4.translation(js.Array(0.015, 0.0, 0.0)))
  val init: Init = Init(id ++ "_init", middleSt)
  val macroSteps = js.Array[(Step, Command)] ()

  val getRenderingInfo: js.Array[(js.Dynamic, Float32Array)] = js.Array()

  val inputPh = PlaceHolder(id ++ "_input")
  inputPh.setParent(middleSt)

  val outputPh = PlaceHolder(id ++ "_output")
  outputPh.setParent(majorSt)

  macroSteps.push((init, On))

  var fromPh: Option[PlaceHolder] = Some(inputPh)

  for (i <- 0 until (slots)) {
    if (i == slots -1) {
      outputPh.updateLocalMatrix(M4.translation(js.Array(0.3 * (i + 2), 0.0, 0.0)))
    }
    val toPh = PlaceHolder(id ++ "_slot" ++ (i + 1).toString)
    toPh.setParent(middleSt)

    toPh.updateLocalMatrix(M4.translation(js.Array(0.3 * (i + 1), 0.0, 0.0)))

    macroSteps.push((Replace(id ++ "_slot" ++ i.toString, fromPh.get, toPh), On))
    //getRenderingInfo.push((Primitives.createSphereVertices(0.05, 12, 12), toPh.worldMatrix))
    fromPh = Some(toPh)
  }
  macroSteps.push((Replace(id ++ "_output", fromPh.get, outputPh), On))
  macroSteps.push((jog, On))

  def getSceneTree: SceneTree = majorSt
  def getSteps: js.Array[Step] = js.Array(jog, init, Macro(id, macroSteps.reverse))
}