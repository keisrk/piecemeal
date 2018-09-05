Design of DescriptionModel
===========================

trait Description {
  def id: String
}
trait Fixture extends Description {
  def getShape: js.Dynamic // Or js.Array[js.Dynamic]?
}

trait Actuator extends Description {
  // Vertices and its LocalMatrix must be paired.
  def getRenderingInfo: js.Array[(js.Dynamic, Float32Array)]
}
how can majorVertices be augmented with another vertices, i.e. Fixtures?
