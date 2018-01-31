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

    val isEstimation = tempBundle.getAsBoolean("estimation_task")
    val isBounded = tempBundle.getAsBoolean("bound_exterior")

    val targLow   = tempBundle.getAsString("target_unit_low").toDouble()
    val targHigh  = tempBundle.getAsString("target_unit_high").toDouble()

    val leftBound = tempBundle.getAsString("start_unit").toDouble()
    val rightBound = tempBundle.getAsString("end_unit").toDouble()

    if (isBounded) {
      if (targHigh > rightBound) {
        err.append("Target \"To\" value exceeds the right bound.\n")
      }

      if (leftBound < targLow) {
        err.append("Target \"From\" value is lower than the left bound.\n")
      }
    }

    if (isEstimation) {
      if (rightBound > panel.calculateMaxTarget()) {
        err.append("Right bound exceeds largest estimation target.\n")
      }

      if (targHigh > panel.calculateMaxTarget()) {
        err.append("Target \"To\" value exceeds maximum target.\n")
      }
    }


    if (err.isNotEmpty()) {
      JOptionPane.showMessageDialog(this, err.toString())
    }

    return err.isEmpty()
  }
}