package ccpl.numberline.config

import ccpl.lib.Bundle
import ccpl.lib.util.screenHeight
import ccpl.lib.util.screenWidth
import java.awt.BorderLayout
import javax.swing.*

class ConfigDialog : JDialog() {

  private val panel = ConfigPanel()

  init {
    this.layout = BorderLayout()
    this.add(panel, BorderLayout.CENTER)

    val saveBtn = JButton("Save")
    saveBtn.addActionListener({

      if (isValidConfig()) {
        this.isVisible = false
      }
    })

    val botPanel = JPanel()
    botPanel.add(saveBtn)
    this.add(botPanel, BorderLayout.SOUTH)

    this.pack()
    this.setLocation((screenWidth() - this.width) / 2, (screenHeight() - this.height) / 2)
    this.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
  }

  fun getBundle() = panel.getBundle()

  fun setDefaults(defs: Bundle) = panel.applyDefaults(defs)

  fun baseBundle(bun: Bundle) { panel.baseBundle = bun }

  private fun isValidConfig(): Boolean {
    val err = StringBuilder()

    val tempBundle = panel.getBundle()

    val numTrials = tempBundle.getAsInt("num_trials")

    val isEstimation  = tempBundle.getAsBoolean("estimation_task")
    val isBounded     = tempBundle.getAsBoolean("bound_exterior")

    val targLow   = tempBundle.getAsString("target_unit_low").toDouble()
    val targHigh  = tempBundle.getAsString("target_unit_high").toDouble()

    val leftBound   = tempBundle.getAsString("start_unit").toDouble()
    val rightBound  = tempBundle.getAsString("end_unit").toDouble()

    val endUnit = tempBundle.getAsString("end_unit").toDouble()

    val margin = tempBundle.getAsInt("left_margin_low")
    val largestTarget = tempBundle.getAsString("largest_target").toDouble()

    if (numTrials == 0) {
      err.append("Experiment contains no trials.\n");
    }

    if (isBounded) {
      if (targHigh >= rightBound) {
        err.append("Target \"To\" value is equal to or greater than the right bound.\n")
      }

      if (endUnit > screenWidth() - margin * 2) {
        err.append("End unit cannot fit on screen. Maximum end unit is $largestTarget \n")
      }
    }

    if (leftBound > targLow) {
      err.append("Target \"From\" value is less than the left bound.\n")
    }

    if (isEstimation) {
      if (rightBound > panel.calculateMaxTarget()) {
        err.append("Right bound is greater than the largest estimation target.\n")
      }

      if (targHigh > panel.calculateMaxTarget()) {
        err.append("Target \"To\" value is greater than maximum target.\n")
      }

      if (targHigh > largestTarget || largestTarget > screenWidth()) {
        err.append("Largest target cannot fit on screen\n")
      }
    }

    if (leftBound > rightBound) {
      err.append("Left bound is greater than the right bound.");
    }


    if (err.isNotEmpty()) {
      JOptionPane.showMessageDialog(this, err.toString())
    }

    return err.isEmpty()
  }
}