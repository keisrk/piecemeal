package piecemeal

import org.scalajs.dom.Element
import org.scalajs.dom.document

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object JSLauncher extends JSApp {
  @JSExport
  def main(): Unit = {
    val appRoot = document.getElementById("application").asInstanceOf[Element]
    ApplicationContext.application.run(appRoot)
  }
}
