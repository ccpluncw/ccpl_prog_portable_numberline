package ccpl.numberline

import ccpl.numberline.config.ConfigFrame
import ccpl.numberline.config.PopupCallback
import javax.swing.UIManager
import kotlin.system.exitProcess

fun main(args: Array<String>) {

  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

  val listener = PopupCallback()
  val popup = ConfigFrame(listener, "Configure")

  while (popup.isVisible) {
    Thread.sleep(1)
  }

  val bundle = listener.bundle

  if (bundle.size != 0) {
    val expFile = "exp"
    val subject = bundle.getAsString("subject")
    val condition = bundle.getAsString("condition")
    val session = bundle.getAsString("session")

    val exp = UniversalNumberLine(expFile, subject, condition, session, bundle)

    exp.run()
    exitProcess(0)
  }
}