package ccpl.numberline.config

import ccpl.lib.Bundle
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class ConfigPanel : JPanel() {

  private val btnGrps = mutableMapOf<String, ButtonGroup>()

  init {
    this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
    this.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

    this.add(getEstPanel())
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

  private fun getEstPanel() : JPanel = buttonPanel("Estimation or Production", "estimation_task",
                                                   listOf("Estimation", "Production"), listOf("true", "false"))

  private fun getBoundedPanel() : JPanel = buttonPanel("Bounded or Unbounded", "bound_exterior",
                                                       listOf("Bounded", "Unbounded"), listOf("true", "false"))

  private fun getSizePanel() : JPanel = buttonPanel("Number Line Size", "line_size", listOf("Small", "Medium", "Large"),
                                                    listOf("small", "medium", "large"))

  private fun getBiasSetting() : JPanel {
    val panel = borderTitlePanel("Estimated Largest Bias")
    panel.layout = GridLayout(2, 3)

    val childRadBtn = JRadioButton("Child")
    childRadBtn.actionCommand = "1.4"

    val adultRadBtn = JRadioButton("Adult")
    adultRadBtn.actionCommand = "1.2"

    val txtFld = JTextField("0")
    val othRadBtn   = JRadioButton("Other")
    othRadBtn.addActionListener { othRadBtn.actionCommand = txtFld.text }

    val btnGrp = ButtonGroup()
    btnGrp.add(childRadBtn)
    btnGrp.add(adultRadBtn)
    btnGrp.add(othRadBtn)

    btnGrps.put("bias", btnGrp)

    panel.add(childRadBtn)
    panel.add(adultRadBtn)
    panel.add(othRadBtn)

    panel.add(JLabel("1.4"))
    panel.add(JLabel("1.2"))
    panel.add(txtFld)

    return panel
  }

  private fun borderTitlePanel(title: String) : JPanel {
    val panel = JPanel()

    panel.border = BorderFactory.createTitledBorder(title)

    return panel
  }

  private fun buttonPanel(title: String, key: String, butStrs: List<String>, cmds: List<String>) : JPanel {
    val buts = butStrs.map { JRadioButton(it) }
    buts.forEachIndexed {i, it -> it.actionCommand = cmds[i]}

    val btnGrp = ButtonGroup()
    buts.forEach { btnGrp.add(it) }
    btnGrps.put(key, btnGrp)

    val panel = borderTitlePanel(title)
    buts.forEach { panel.add(it) }

    return panel
  }

  fun getBundle() : Bundle {
    val bundle = Bundle()

    btnGrps.forEach { s, btnGrp -> bundle.add(s, btnGrp.selection.actionCommand) }

    return bundle
  }
}
