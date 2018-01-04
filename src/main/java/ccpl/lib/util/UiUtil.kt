package ccpl.lib.util

import java.awt.GridLayout
import java.awt.Toolkit
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

private val tk = Toolkit.getDefaultToolkit()

fun screenWidth() : Int = tk.screenSize.width

fun screenHeight() : Int = tk.screenSize.height

fun expandGridPanel(panel: JPanel, label: String, comp: JComponent) {
  (panel.layout as GridLayout).rows.plus(1)
  panel.add(JLabel("$label: "))
  panel.add(comp)
}

fun addTrackedTxtField(key: String, label: String, panel: JPanel, tracker: MutableMap<String, JTextField>, expandGrid: Boolean = true) {
  val textField = JTextField("0")
  tracker.put(key, textField)
  if (expandGrid) {
    expandGridPanel(panel, label, textField)
  } else {
    panel.add(JLabel("$label: "))
    panel.add(textField)
  }
}