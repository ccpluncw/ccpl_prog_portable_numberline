package ccpl.numberline

fun main(args: Array<String>) {
  val expFile = "exp"
  val subject = ""
  val condition = ""
  val session = ""

  val bounded = false
  val estimation = false

  val targetHigh = 21
  val targetLow = 2

  val leftBound = 1
  val rightBound = 1

  val exp = UniversalNumberLine(expFile, subject, condition, session, bounded, estimation,
      targetLow, targetHigh, leftBound, rightBound)

  exp.run()
}