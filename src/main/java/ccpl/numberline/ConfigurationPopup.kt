package ccpl.numberline

import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.Point
import java.awt.Toolkit
import javax.swing.*

class ConfigurationPopup(listener: PopupCallback, title: String?) : JFrame(title) {

  private val estimateProd: Array<String> = arrayOf("Estimation", "Production")
  private val centerPanel = JPanel()

  init {
    layout = BorderLayout()

    centerPanel.layout = GridLayout(1, 2)

    val estimationDropDown: JComboBox<String> = JComboBox(estimateProd)

    addToCenterPanel("Estimation or Production: ", estimationDropDown)

    val okayButton = JButton("Okay")
    okayButton.addActionListener({
      listener.bundle.add("estimation_task", estimateProd[0] == estimationDropDown.selectedItem)

      this.isVisible = false
      this.dispose()
    })

    val exitButton = JButton("Exit")
    exitButton.addActionListener { System.exit(1) }

    val bottomPanel = JPanel()
    bottomPanel.add(okayButton)
    bottomPanel.add(exitButton)

    this.add(centerPanel, BorderLayout.CENTER)
    this.add(bottomPanel, BorderLayout.SOUTH)

    val screenSize = Toolkit.getDefaultToolkit().screenSize

    pack()
    location = Point((screenSize.width - width) / 2, (screenSize.height  - height) / 2)
    isVisible = true
  }

  private fun addToCenterPanel(label: String, component: JComponent) {
    centerPanel.add(JLabel(label))
    centerPanel.add(component)
  }
}