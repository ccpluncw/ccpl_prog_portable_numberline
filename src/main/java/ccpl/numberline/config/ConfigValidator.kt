package ccpl.numberline.config

import ccpl.lib.Bundle
import ccpl.lib.util.screenWidth

/**
 * Create a StringBuilder containing all the errors with the configuration.
 *
 * @param bun   Configuration bundle
 *
 * @return      StringBuilder with any error messages.
 */
fun generateConfigErrors(bun: Bundle) : StringBuilder {
  val err = StringBuilder()

  // Unpack all the relevant values from the bundle.
  val numTrials = bun.getAsInt("num_trials")

  val isEstimation  = bun.getAsBoolean("estimation_task")
  val isBounded     = bun.getAsBoolean("bound_exterior")

  val targLow   = bun.getAsString("target_unit_low").toDouble()
  val targHigh  = bun.getAsString("target_unit_high").toDouble()

  val leftBound   = bun.getAsString("start_unit").toDouble()
  val rightBound  = bun.getAsString("end_unit").toDouble()

  val endUnit = bun.getAsString("end_unit").toDouble()

  val margin = bun.getAsInt("left_margin_low")
  val largestTarget = bun.getAsString("largest_target").toDouble()

  // Check for potential errors.
  if (numTrials == 0) {
    err.append("Experiment contains no trials.\n" +
        "Please set the number of trials to a minimum of 1\n")
  }

  if (leftBound > targLow) {
    err.append("Target \"From\" value is less than the left bound.\n" +
        "Please set the target \"From\" value greater than the left bound.\n")
  }

  if (leftBound > rightBound) {
    err.append("Left bound is greater than the right bound.\n" +
        "Please set the left bound to less than the right bound.\n")
  }

  if (isBounded) {
    if (targHigh >= rightBound) {
      err.append("Target \"To\" value is equal to or greater than the right bound.\n" +
          "Please set the target \"To\" value to less than the right bound.\n")
    }

    if (endUnit > screenWidth() - margin * 2) {
      err.append("End unit cannot fit on screen. Maximum end unit is $largestTarget \n" +
          "Please set the end unit to at most $largestTarget\n")
    }
  }

  if (isEstimation) {
    if (rightBound > largestTarget) {
      err.append("Right bound is greater than the largest estimation target.\n" +
          "Please set the right bound to less than or equal to the largest estimation target\n")
    }

    if (targHigh > largestTarget) {
      err.append("Target \"To\" value is greater than maximum target.\n" +
          "Please set the target \"To\" value to less than or equal to the maximum target\n")
    }

    if (targHigh > largestTarget || largestTarget > screenWidth()) {
      err.append("Largest target cannot fit on screen\n")
    }
  }


  return err
}
