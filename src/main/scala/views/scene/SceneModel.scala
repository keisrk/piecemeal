package piecemeal.views.scene

import io.udash._
import piecemeal.views.{Program, Piece}
/** The XXX's model structure. */
case class SceneModel(programs: Seq[Program], pieces: Seq[Piece])
object SceneModel extends HasModelPropertyCreator[SceneModel]
