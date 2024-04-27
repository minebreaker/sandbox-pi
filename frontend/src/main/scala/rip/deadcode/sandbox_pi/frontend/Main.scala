package rip.deadcode.sandbox_pi.frontend

import org.scalajs.dom.document
import slinky.core.facade.Hooks.*
import slinky.web.html.*
import typings.reactDom.clientMod.{Container, createRoot}

object Main {

  def main(args: Array[String]): Unit = {
    val root = createRoot(document.getElementById("app").asInstanceOf[Container])
    root.render(
      h1("hello, world")
    )
  }
}
