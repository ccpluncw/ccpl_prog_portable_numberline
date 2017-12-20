package ccpl.numberline.config

import ccpl.lib.Bundle
import ccpl.lib.util.addTrackedTxtField
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class ConfigPanel : JPanel() {

  private val btnGrps = mutableMapOf<String, ButtonGroup>()

  private val textKeys = listOf("num_trials", "num_prac_trials", "start_unit", "end_unit", "target_unit_low",
                                "target_unit_high", "target_unit_interval")

  private val textLabels = listOf("Number of Trials", "Number of Practice Trials", "Start Unit", "End Unit",
                                  "Target Unit Low", "Target Unit High", "Target Unit Interval")

  private val txtMap = mutableMapOf<String, JTextField>()

  init {
    this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
    this.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

    this.add(textPanel())
    this.add(estPanel())
    this.add(boundedPanel())
    this.add(sizePanel())
    this.add(biasPanel())
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

  private fun textPanel() : JPanel {
    val panel = JPanel()
    panel.layout = GridLayout(0, 2, 0, 2)

    textKeys.indices.forEach { addTrackedTxtField(textKeys[it], textLabels[it], panel, txtMap) }

    return panel
  }

  private fun estPanel() : JPanel = buttonPanel("Estimation or Production", "estimation_task",
                                                listOf("Estimation", "Production"), listOf("true", "false"))

  private fun boundedPanel() : JPanel = buttonPanel("Bounded or Unbounded", "bound_exterior",
                                                    listOf("Bounded", "Unbounded"), listOf("true", "false"))

  private fun sizePanel() : JPanel = buttonPanel("Number Line Size", "line_size", listOf("Small", "Medium", "Large"),
                                                 listOf("small", "medium", "large"))

  private fun biasPanel() : JPanel {
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

    txtMap.forEach { k, txt -> bundle.add(k, txt.text) }
    btnGrps.forEach { k, btnGrp -> bundle.add(k, btnGrp.selection.actionCommand) }

    return bundle
  }
}
