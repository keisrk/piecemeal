package piecemeal.services
import scala.scalajs.js
import scala.scalajs.js.JSON
import piecemeal.facade.csg.CSG
import piecemeal.facade.utils.CSGUtils

class ModelDB {
  def getDescription(name: String): String = "model description"
  def setup():Unit = {
  }
}

object ModelDB {
  private val dbTxt = js.Dynamic.global.db.asInstanceOf[String]
  private val db = JSON.parse(dbTxt).asInstanceOf[js.Dictionary[js.Array[js.Dynamic]]]

  def getPolygonVertices(name: String): js.Dynamic = {
    val polygonData = db(name)

    CSGUtils.createCSGVertices(CSG.fromPolygons(
      for (polygon <- polygonData) yield {new CSG.Polygon(
        for (vertex <- polygon.polygon.asInstanceOf[js.Array[js.Dynamic]]) yield {
          val pos = vertex.pos.asInstanceOf[js.Array[Double]]
          val normal = vertex.normal.asInstanceOf[js.Array[Double]]
          new CSG.Vertex(
            new CSG.Vector(pos(0), pos(1), pos(2)),
            new CSG.Vector(normal(0), normal(1), normal(2)))
        })
      }))
  }
}
