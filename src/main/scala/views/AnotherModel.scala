package piecemeal.views

import io.udash._
/** The XXX's model structure. */
case class AnotherModel(programs: Seq[Program], pieces: Seq[Piece])
object AnotherModel extends HasModelPropertyCreator[AnotherModel]
/*
case class Program(id: Int)
object Program extends HasModelPropertyCreator[Program]

case class Piece(id: String, commands: Seq[String])
object Piece extends HasModelPropertyCreator[Piece]
 */
