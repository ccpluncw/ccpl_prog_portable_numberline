package ccpl.numberline.config

import ccpl.lib.Bundle
import ccpl.lib.util.screenHeight
import ccpl.lib.util.screenWidth
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
      this.isVisible = false
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