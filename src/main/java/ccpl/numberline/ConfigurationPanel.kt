package ccpl.numberline

import java.awt.Component
import java.awt.Dimension
import javax.swing.*

class ConfigurationPanel : JPanel() {
  init {
    this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
    this.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

    this.add(getBoundedPanel())
    this.add(getSizePanel())
    this.add(getBiasSetting())
  }

  /**
   * Overridden add method to add rigid space between each section.
   * @param p0  Component being added to JPanel
   * @return    Component added
   */
  override fun add(p0: Component?): Component {
    super.add(p0)
    return super.add(Box.createRigidArea(Dimension(0, 10)))
  }

  private fun getBoundedPanel() : JPanel = buttonPanel("Bounded or Unbounded", listOf("Bounded", "Unbounded"))

  private fun getSizePanel() : JPanel = buttonPanel("Number Line Size", listOf("Small", "Medium", "Large"))

  private fun getBiasSetting() : JPanel = buttonPanel("Estimation Largest Bias", listOf("Child", "Adult", "Other"))

  private fun borderTitlePanel(title: String) : JPanel {
    val panel = JPanel()

    panel.border = BorderFactory.createTitledBorder(title)

    return panel
  }

  private fun buttonPanel(title: String, butStrs: List<String>) : JPanel {
    val buts = butStrs.map { JRadioButton(it) }

    val btnGrp = ButtonGroup()
    buts.forEach { btnGrp.add(it) }

    val panel = borderTitlePanel(title)
    buts.forEach { panel.add(it) }

    return panel
  }
}