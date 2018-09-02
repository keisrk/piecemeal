package piecemeal.scene

import io.udash._
import piecemeal.facade.twgl.{Constants, TWGL, M4, Primitives}

import org.scalajs.dom.webgl

import scala.concurrent.{Future, Promise}
import scala.async.Async.{async, await}
import scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

case class Channel(){
  private[this] var value: Promise[Boolean] = Promise().success(true)
  def apply(): Future[Boolean] = {
    value = Promise[Boolean]()
    value.future
  }

  def isCompleted(): Boolean = {
    value.isCompleted
  }

  def done(): Unit = {
    if (!value.isCompleted) value.success(true)
  }

  def update(t: Boolean): Unit = {
    if (!value.isCompleted) value.success(t)
  }
}

trait SceneTree {

  def worldMatrix: Float32Array
  def initMatrix(): Unit
  def updateLocalMatrix(m: Float32Array): Unit
  def updateWorldMatrix(pWorldMatrix: Float32Array): Unit
}

case class Node() extends SceneTree {
  val children: js.Array[SceneTree] = js.Array()
  val worldMatrix = M4.identity()
  private val localMatrix = M4.identity()

  def setParent(p: Node): Unit = {
    p.children.push(this)
  }
  def initMatrix(): Unit = {M4.identity(localMatrix)}

  def updateLocalMatrix(m: Float32Array): Unit = {
    M4.multiply(this.localMatrix, m, this.localMatrix)
  }

  def updateWorldMatrix(pWorldMatrix: Float32Array): Unit = {
    M4.multiply(pWorldMatrix, this.localMatrix, this.worldMatrix)

    for (child <- this.children) {
      child.updateWorldMatrix(this.worldMatrix)
    }
  }
}

case class Leaf() extends SceneTree {
  val worldMatrix = M4.identity()
  private val localMatrix = M4.identity()
  def initMatrix(): Unit = {M4.identity(localMatrix)}

  def updateLocalMatrix(m: Float32Array): Unit = {
    M4.multiply(this.localMatrix, m, this.localMatrix)
  }
  def updateWorldMatrix(pWorldMatrix: Float32Array): Unit = {
    M4.multiply(pWorldMatrix, this.localMatrix, this.worldMatrix)
  }
}

case class PlaceHolder(id: String) extends SceneTree {
  private var child: Option[Leaf] = None
  val worldMatrix = M4.identity()
  private val localMatrix = M4.identity()
  def getId = id
  def currentChild(): Option[Leaf] = child
  def popChild(): Option[Leaf] = {val out = child; child = None; out}
  def pushChild(c: Option[Leaf]): Unit = { child = c }

  def setParent(p: Node): Unit = {
    p.children.push(this)
  }

  def initMatrix(): Unit = {M4.identity(localMatrix)}

  def updateLocalMatrix(m: Float32Array): Unit = {
    M4.multiply(this.localMatrix, m, this.localMatrix)
  }

  def updateWorldMatrix(pWorldMatrix: Float32Array): Unit = {
    M4.multiply(pWorldMatrix, this.localMatrix, this.worldMatrix)

    for (c <- child) {
      c.updateWorldMatrix(this.worldMatrix)
    }
  }
}

abstract class Command
case object On extends Command
case object Off extends Command
case class Move(i: Int) extends Command
object Move extends HasModelPropertyCreator[Move]

trait Step {
  def channel: Channel
  def isCompleted(): Boolean =  channel.isCompleted()
  def getId: String
  def exec(cmd: Command): Future[Boolean]
  def tik(): Unit
}

case class Toggle(id: String, st: SceneTree, countPerTik: Double, increment: Float32Array, decrement: Float32Array) extends Step {
  private[this] var count = 0.0
  private[this] var isON = false
  val channel = Channel()
  def getId: String = id
  def tik(): Unit = {
    if (!channel.isCompleted()){
      if (isON && count < 1.0) {
        count += countPerTik
        st.updateLocalMatrix(increment)
      } else if (isON && count >= 1.0) {
        count = 1.0
        channel.done()

      } else if (!isON && 0.0 < count){
        count -= countPerTik
        st.updateLocalMatrix(decrement)
      } else /*(!isON && 0.0 >= count)*/{
        count = 0.0
        channel.done()
      }
    }
  }
  def on(): Future[Boolean] = {
    if (!isON && channel.isCompleted()){
      isON = true
      channel()
    } else {
      // Does not accept command.
      Future { true }
    }
  }
  def off(): Future[Boolean] = {
    if (isON && channel.isCompleted()){
      isON = false
      channel()
    } else {
      // Does not accept command.
      Future { true }
    }
  }
  def exec(cmd: Command): Future[Boolean] = cmd match {
    case On => on()
    case Off => off()
    case _ => Future {true}
  }
}

case class Shift(id: String, st: SceneTree, countPerTik: Double, positions: js.Array[Double], increment: Float32Array, decrement: Float32Array) extends Step {
  private[this] var count = 0.0
  private[this] var isUP = true
  private[this] var pos = 0
  val channel = Channel()
  def getId: String = id
  def tik(): Unit = {
    if (!channel.isCompleted()){
      if (isUP && count < positions(pos)) {
        count += countPerTik
        st.updateLocalMatrix(increment)
      } else if (isUP && count >= positions(pos)) {
        count = positions(pos)
        channel.done()
      } else if (!isUP && positions(pos) < count){
        count -= countPerTik
        st.updateLocalMatrix(decrement)
      } else /*(!isUP && positions(pos) >= count)*/{
        count = positions(pos)
        channel.done()
      }
    }
  }
  def move(p: Int): Future[Boolean] = {
    if (channel.isCompleted()){
      pos = p
      isUP = count < positions(pos)
      channel()
    } else {
      // Does not accept command.
      Future { true }
    }
  }
  def exec(cmd: Command): Future[Boolean] = cmd match {
    case Move(p) => move(p)
    case _ => Future {true}
  }
}
case class Replace(id: String, from: PlaceHolder, to: PlaceHolder) extends Step {
  def getId: String = id
  val channel = Channel()
  channel.done()
  def tik(): Unit = {}
  def exec(cmd: Command = On): Future[Boolean] = {
    println(from.getId ++ " -> " ++ to.getId)
    (from.currentChild, to.currentChild) match {
      case (Some(c), None) => to.pushChild(from.popChild())
      case _ => {}
    }
    Future { true }
  }
}

case class Init(id: String, st: SceneTree) extends Step {
  val channel = Channel()
  channel.done()

  def getId: String = id
  def tik(): Unit = {}
  def exec(cmd: Command = On): Future[Boolean] = Future {st.initMatrix(); true}
}

case class Jog(id: String, st: SceneTree, countPerTik: Double, increment: Float32Array) extends Step {
  private[this] var count = 0.0
  val channel = Channel()
  def tik(): Unit = {
    if (!channel.isCompleted()){
      if (count < 1.0) {
        count += countPerTik
        st.updateLocalMatrix(increment)
      } else /*(1.0 <= count)*/{
        count = 0.0
        channel.done()
      }
    }
  }
  def getId: String = id
  def exec(cmd: Command = On): Future[Boolean] = {
    if (channel.isCompleted()){
      count = 0.0
      channel()
    } else {
      // Does not accept command.
      Future { true }
    }
  }

}

case class Macro(id: String, steps: js.Array[(Step, Command)]) extends Step {
  /* You CANNOT nest Macro! */

  def getId: String = id
  private[this] var i = 0
  val channel = Channel()
  def tik(): Unit = {
    //for (s <- steps) s._1.tik()
    if (!channel.isCompleted()){
      if (i == steps.length && steps(i - 1)._1.isCompleted()) {
        i = 0
        println("done")
        channel.done()
      }
    }
  }
  def exec(cmd: Command = On): Future[Boolean] = {
    if (channel.isCompleted()){
      channel()
      async {
        i = 0
        while (i < steps.length) {
          steps(i) match {
            case (step, cmd) => await{ step.exec(cmd) }
          }
          i += 1
        }

        true
      }
    } else Future { true }
  }
}
/*
Toggle    ON/OFF (cylinder, etc.) defaults to OFF
Handle    Trig  (suction cup, etc) defaults to OFF
Shift     0 - 1 - 2 (servo moter, etc) defaults to 0
Process   Trig  (lasor marker, etc) defaults to null
Toggle.on()
Shift.move(i)
Process.trig()
Handle.trig(stuff, orig, dest, relocate) passes stuff from orig's subtree to the other.
val compose: () => Future[Boolean] = () => async{await(Toggle.on())} await(Shift._move(2))}
*/

