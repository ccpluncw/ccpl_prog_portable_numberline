package ccpl.numberline.config

import ccpl.lib.Bundle
import ccpl.lib.util.screenHeight
import ccpl.lib.util.screenWidth
import com.sun.media.util.JMFI18N.bundle
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.WindowConstants

class ConfigDialog : JDialog() {

  private val panel = ConfigPanel()

  init {
    this.layout = BorderLayout()
    this.add(panel, BorderLayout.CENTER)

    val saveBtn = JButton("Save")
    saveBtn.addActionListener({
      val tempBundle = panel.getBundle()
      val targHigh = tempBundle.getAsString("target_unit_high").toDouble()
      val rightBound = tempBundle.getAsString("end_unit").toDouble()

      if (targHigh <= rightBound) this.isVisible = false
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
}