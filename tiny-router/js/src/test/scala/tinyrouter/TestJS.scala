package tinyrouter

import scala.scalajs.js.JSApp

object TestJS extends JSApp {
  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {
    println("Running JS test!")
    Test.test()
  }
}
