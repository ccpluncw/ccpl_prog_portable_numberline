package ccpl.numberline

fun main(args: Array<String>) {

  val listener = PopupCallback()
  val popup = ConfigurationPopup(listener, "Configure")

  while (popup.isVisible) {
    Thread.sleep(1)
  }

  val bundle = listener.bundle

  if (bundle.size != 0) {
    val expFile = "exp"
    val subject = ""
    val condition = ""
    val session = ""

    val bounded = false
    val estimation = bundle.getAsBoolean("estimation_task")

    val targetHigh = 21
    val targetLow = 2

    val leftBound = 1
    val rightBound = 1

    val exp = UniversalNumberLine(expFile, subject, condition, session, bounded, estimation,
        targetLow, targetHigh, leftBound, rightBound)

    exp.run()
  }
}