package piecemeal.facade.utils

import scala.collection.mutable.HashMap
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.typedarray.{Float32Array, Uint16Array}

import org.scalajs.dom
import org.scalajs.dom.raw
import org.scalajs.dom.webgl.RenderingContext

import piecemeal.facade.twgl._
import piecemeal.facade.csg._

class TWGLUtils {
  var posZ = -10.0
  var camera = M4.identity()
  var view = M4.identity()
  var viewProjection = M4.identity()
  var world = M4.identity()

  def tex(gl: RenderingContext): js.Dynamic = TWGL.createTexture(gl,
    js.Dynamic.literal(
      "min" -> raw.WebGLRenderingContext.NEAREST,
      "mag" -> raw.WebGLRenderingContext.NEAREST,
      "src" -> 
      js.Array(
        255, 255, 255, 255,
/*
        255, 255, 255, 255,
        192, 192, 192, 255,
        192, 192, 192, 255,
        255, 255, 255, 255,
 */
      )))

  def uniforms(gl: RenderingContext): js.Dynamic =
    js.Dynamic.literal(
      "u_lightColor" -> js.Array(0.2, 0.5, 0.8, 1),
      "u_lightWorldPos" -> js.Array(1, 8, -10),
      "u_ambient" -> js.Array(0, 0, 0, 1),
      "u_specular" -> js.Array(1, 1, 1, 1),
      "u_shininess" -> 50,
      "u_specularFactor" -> 1,
      "u_viewInverse" -> M4.identity(),
      "u_diffuse" -> tex(gl))

  def cloneUniforms(uniforms:js.Dynamic): js.Dynamic =
    js.Dynamic.literal(
      "u_lightColor" -> js.Array(0.2, 0.5, 0.8, 1),
      "u_lightWorldPos" -> js.Array(1, 8, -10),
      "u_ambient" -> js.Array(0, 0, 0, 1),
      "u_specular" -> js.Array(1, 1, 1, 1),
      "u_shininess" -> 50,
      "u_specularFactor" -> 1,
      "u_viewInverse" -> uniforms.u_viewInverse,
      "u_diffuse" -> uniforms.u_diffuse,
      "u_world" -> M4.identity(),
      "u_worldViewProjection" -> M4.identity())

  def renderPreamble(gl: RenderingContext): Unit = {
    TWGL.resizeCanvasToDisplaySize(gl.canvas)
    gl.viewport(0, 0, gl.canvas.width, gl.canvas.height)
    gl.enable(raw.WebGLRenderingContext.DEPTH_TEST)
    gl.enable(raw.WebGLRenderingContext.CULL_FACE)
    gl.clear(Math.max(
      raw.WebGLRenderingContext.COLOR_BUFFER_BIT,
      raw.WebGLRenderingContext.DEPTH_BUFFER_BIT))
  }
  def renderUniforms(gl: RenderingContext): (Float32Array, Float32Array, Float32Array, Float32Array) = {
    val fov = 60 * Math.PI / 180
    val aspect = gl.canvas.width / gl.canvas.height
    val zNear = 0.5
    val zFar = 1000
    val projection = M4.perspective(fov, aspect, zNear, zFar)
    val eye = js.Array(0.0, 4.2, posZ)
    val target = js.Array(0.0, 0.0, 0.0)
    val up = js.Array(0.0, 1.0, 0.0)

    /*val*/ camera = M4.lookAt(eye, target, up)
    /*val*/ view = M4.inverse(camera)
    /*val*/ viewProjection = M4.multiply(projection, view)
    /*val*/ //world = M4.identity()
    (camera, view, viewProjection, world)
  }

  val vs =
    """
uniform mat4 u_worldViewProjection;
uniform vec3 u_lightWorldPos;
uniform mat4 u_world;
uniform mat4 u_viewInverse;
uniform mat4 u_worldInverseTranspose;

attribute vec4 position;
attribute vec3 normal;
attribute vec2 texcoord;

varying vec4 v_position;
varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_surfaceToLight;
varying vec3 v_surfaceToView;

void main() {
  v_texCoord = texcoord;
  v_position = u_worldViewProjection * position;
  v_normal = (u_worldInverseTranspose * vec4(normal, 0)).xyz;
  v_surfaceToLight = u_lightWorldPos - (u_world * position).xyz;
  v_surfaceToView = (u_viewInverse[3] - (u_world * position)).xyz;
  gl_Position = v_position;
}
"""
  val fs =
    """
precision mediump float;

varying vec4 v_position;
varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_surfaceToLight;
varying vec3 v_surfaceToView;

uniform vec4 u_lightColor;
uniform vec4 u_ambient;
uniform sampler2D u_diffuse;
uniform vec4 u_specular;
uniform float u_shininess;
uniform float u_specularFactor;

vec4 lit(float l ,float h, float m) {
  return vec4(1.0,
              max(l, 0.0),
              (l > 0.0) ? pow(max(0.0, h), m) : 0.0,
              1.0);
}

void main() {
  vec4 diffuseColor = texture2D(u_diffuse, v_texCoord);
  vec3 a_normal = normalize(v_normal);
  vec3 surfaceToLight = normalize(v_surfaceToLight);
  vec3 surfaceToView = normalize(v_surfaceToView);
  vec3 halfVector = normalize(surfaceToLight + surfaceToView);
  vec4 litR = lit(dot(a_normal, surfaceToLight),
                    dot(a_normal, halfVector), u_shininess);
  vec4 outColor = vec4((
  u_lightColor * (diffuseColor * litR.y + diffuseColor * u_ambient +
                u_specular * litR.z * u_specularFactor)).rgb,
      diffuseColor.a);
  gl_FragColor = outColor;
}
"""
}

case class Indexer(){
  val unique: js.Array[js.Dynamic] = js.Array()
  val map: HashMap[String, Int] = HashMap()
}

object CSGUtils {
  def add(idx: Indexer, obj: js.Dynamic): Int = {
    val key = JSON.stringify(obj)
    if (!(idx.map contains key)) {
      idx.map += (key -> idx.unique.length)
      idx.unique.push(obj)
    }
    idx.map(key)
  }
  def createCSGVertices(csg: CSG): js.Dynamic = {
    val indexer = Indexer()
    val triangles: js.Array[js.Array[Int]] = js.Array()
    csg.toPolygons.map((polygon) => {
      val idx: js.Array[Int] = polygon.vertices.map((vertex: js.Dynamic) => { add(indexer, vertex) })
        .asInstanceOf[js.Array[Int]]
      for (i <- 2 until idx.length) {
        triangles.push(js.Array(idx(0), idx(i - 1), idx(i)))
      }})
    val numVertices = indexer.unique.length * 3; println(numVertices)
    val numIndices = triangles.length * 3; println(numIndices)
    val positions = Primitives.createAugmentedTypedArray(3, numVertices)
    val normals   = Primitives.createAugmentedTypedArray(3, numVertices)
    val texcoords = Primitives.createAugmentedTypedArray(2, numVertices)
    val indices   = Primitives.createAugmentedTypedArray(3, numIndices, Uint16Array)
    indexer.unique.foreach((vertex) => {
      positions.push(vertex.pos.x, vertex.pos.y, vertex.pos.z)
      normals.push(vertex.normal.x, vertex.normal.y, vertex.normal.z)
    })
    triangles.foreach((v3) => {
      indices.push(v3)
    })
    js.Dynamic.literal(
      "position" -> positions,
      "normal" ->  normals,
      "texcoord" -> texcoords,
      "indices" -> indices,
    )
  }
  val txt = js.Dynamic.global.data.asInstanceOf[String]
  val data = js.JSON.parse(txt).asInstanceOf[js.Array[js.Dynamic]]
  val plg = createCSGVertices(CSG.fromPolygons(
    for (polygon <- data) yield {new CSG.Polygon(
      for (vertex <- polygon.polygon.asInstanceOf[js.Array[js.Dynamic]]) yield {
        val pos = vertex.pos.asInstanceOf[js.Array[Double]]
        val normal = vertex.normal.asInstanceOf[js.Array[Double]]
        new CSG.Vertex(
          new CSG.Vector(pos(0), pos(1), pos(2)),
          new CSG.Vector(normal(0), normal(1), normal(2)))
      })
    }))
}
