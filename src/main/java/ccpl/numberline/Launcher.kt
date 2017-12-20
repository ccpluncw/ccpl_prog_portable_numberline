package ccpl.numberline

import ccpl.numberline.config.ConfigPopup
import ccpl.numberline.config.PopupCallback
import javax.swing.UIManager

fun main(args: Array<String>) {

  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

  val listener = PopupCallback()
  val popup = ConfigPopup(listener, "Configure")

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
  }
}