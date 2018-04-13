package ccpl.numberline.config

import ccpl.lib.Bundle
import ccpl.lib.util.screenHeight
import ccpl.lib.util.screenWidth
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Dialog which appears when the experimenter clicks configure.
 *
 * This class does not handle the the actual layout, that is delegated to ConfigPanel
 *
 * @see DetailedConfigDialog
 */
class DetailedConfigDialog : JDialog() {

  private val panel = DetailedConfigPanel()

  init {
    this.layout = BorderLayout()

    val saveBtn = JButton("Save")
    saveBtn.addActionListener({
      if (isValidConfig()) {
        this.isVisible = false
      }
    })

    val botPanel = JPanel()
    botPanel.add(saveBtn)

    // Add panels to the JDialog
    this.add(panel, BorderLayout.CENTER)
    this.add(botPanel, BorderLayout.SOUTH)

    this.pack()
    this.setLocation((screenWidth() - this.width) / 2, (screenHeight() - this.height) / 2)
    this.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
  }

  fun getBundle() = panel.getBundle()

  fun setDefaults(defs: Bundle) = panel.applyDefaults(defs)

  fun baseBundle(bun: Bundle) { panel.baseBundle = bun }

  private fun isValidConfig(): Boolean {
    val err = generateConfigErrors(panel.getBundle())

    if (err.isNotEmpty()) {
      JOptionPane.showMessageDialog(this, err.toString())
      return false
    }

    return true
  }
}