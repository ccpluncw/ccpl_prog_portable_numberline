package ccpl.numberline.config

import ccpl.lib.Bundle
import ccpl.lib.IntFilter
import ccpl.lib.util.addTrackedTxtField
import ccpl.lib.util.screenWidth
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dialog
import java.awt.Dimension
import java.awt.DisplayMode
import java.awt.GraphicsEnvironment
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.ItemEvent
import java.lang.Math.pow
import java.text.DecimalFormat
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFormattedTextField
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.NumberFormatter
import javax.swing.text.PlainDocument

class DetailedConfigPanel : JPanel() {

  private val btnGrps = mutableMapOf<String, ButtonGroup>()

  private val textKeys = listOf("num_trials", "num_prac_trials", "start_unit", "end_unit" )

  private val textLabels = listOf("Number of Trials", "Number of Practice Trials", "Left Bound", "Right Bound")

  private val txtMap = mutableMapOf<String, JTextField>()

  private val largeLbl = JLabel("Largest target value or right bound allowed: 0.0")
  private val intOnly = DecimalFormat("###")
  private val twoSig = DecimalFormat("###.##")

  private val txtFld = JFormattedTextField(twoSig)

  var baseBundle: Bundle = Bundle()
    set(value) {
      field = value
      updateLargeLbl()
    }

  init {
    this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
    this.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

    this.add(textPanel())
    this.add(targetPanel())
    this.add(largestTarget())
    this.add(estPanel())
    this.add(boundedPanel())
    this.add(sizePanel())
    this.add(biasPanel())
    this.add(customInstruction())

    txtMap.forEach { _, txtField -> txtField.document.addDocumentListener(object : DocumentListener {
      override fun changedUpdate(p0: DocumentEvent?) {
        if (txtField.text.isNotEmpty()) updateLargeLbl()
      }
      override fun insertUpdate(p0: DocumentEvent?) {
        if (txtField.text.isNotEmpty()) updateLargeLbl()
      }
      override fun removeUpdate(p0: DocumentEvent?) {
        if (txtField.text.isNotEmpty()) updateLargeLbl()
      }
    })
    }
    btnGrps.forEach { _, btnGrp -> btnGrp.elements.toList().forEach { it.addActionListener { updateLargeLbl() } } }
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

    val formatter = NumberFormatter(intOnly)
    formatter.minimum = 0
    formatter.allowsInvalid = false
    formatter.commitsOnValidEdit = true

    textKeys.indices.forEach {
      addTrackedTxtField(IntTextField(), textKeys[it], textLabels[it], panel, txtMap)
    }

    return panel
  }

  private fun targetPanel() : JPanel {
    val panel = borderTitlePanel("Target")
    panel.layout = GridLayout(0, 6, 5, 1)

    val txtKey = listOf("target_unit_low", "target_unit_high", "target_unit_interval")
    val txtLabel = listOf("From", "To", "By")

    txtKey.forEachIndexed { index, s -> addTrackedTxtField(JFormattedTextField(twoSig), s, txtLabel[index], panel, txtMap, false) }

    return panel
  }

  private fun estPanel() : JPanel {
    val wrapper = JPanel()
    wrapper.layout = BorderLayout()

    val panel = buttonPanel("Estimation or Production", "estimation_task",
        listOf("Estimation", "Production"), listOf("true", "false"))

    val stimPanel = borderTitlePanel("Estimation Stim Time")
    val stimSwitches = buttonPanel("", "stim_time_off",listOf("Unlimited", "Limited"), listOf("true", "false"))
    val stimInfoPanel = JPanel()

    stimPanel.layout = BorderLayout()
    stimPanel.add(stimSwitches, BorderLayout.NORTH)
    //stimPanel.add(stimInfoPanel, BorderLayout.SOUTH)

    val btnGrpSwitches = btnGrps["stim_time_off"]!!
    val btnsSwitches = btnGrpSwitches.elements.toList()

    btnsSwitches[0].addItemListener {
      if (it.stateChange == ItemEvent.SELECTED) {
        stimPanel.remove(stimInfoPanel)
        stimPanel.revalidate()
        (this.rootPane.parent as Dialog).pack()
      }
    }
    btnsSwitches[1].addItemListener {
      if (it.stateChange == ItemEvent.SELECTED) {
        stimPanel.add(stimInfoPanel, BorderLayout.SOUTH)
        stimPanel.revalidate()
        (this.rootPane.parent as Dialog).pack()
      }
    }

    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val gs = ge.screenDevices

    val rr = gs[0].displayMode.refreshRate
    val refreshRate = if (rr == DisplayMode.REFRESH_RATE_UNKNOWN) 60 else rr
    baseBundle.add("refresh_rate", refreshRate)

    stimInfoPanel.layout = GridBagLayout()
    val c = GridBagConstraints()
    c.gridx = 0
    c.gridy = 0
    c.gridwidth = 5
    stimInfoPanel.add(JLabel("Computer screen refresh interval: ${1000 / refreshRate} ms"), c)

    c.gridwidth = 1
    c.gridy = 1
    c.anchor = GridBagConstraints.CENTER
    stimInfoPanel.add(JLabel("Stim scalar: "), c)

    val scalarField = JTextField(4)

    (scalarField.document as PlainDocument).documentFilter = IntFilter()

    addTrackedTxtField(scalarField,"scalar_field", "", stimPanel, txtMap, false)
    c.gridx = 1
    stimInfoPanel.add(scalarField, c)

    c.gridx = 2
    stimInfoPanel.add(JLabel("x${1000 / refreshRate} ms"), c)

    val btnGrp = btnGrps["estimation_task"]!!
    val btns = btnGrp.elements.toList()

    btns[0].addItemListener {
      if (it.stateChange == ItemEvent.SELECTED) {
        wrapper.add(stimPanel, BorderLayout.SOUTH)
        wrapper.revalidate()
        (this.rootPane.parent as Dialog).pack()
      }
    }
    btns[1].addItemListener {
      if (it.stateChange == ItemEvent.SELECTED) {
        wrapper.remove(stimPanel)
        wrapper.revalidate()
        (this.rootPane.parent as Dialog).pack()
      }
    }

    wrapper.add(panel, BorderLayout.NORTH)
    wrapper.add(stimPanel, BorderLayout.SOUTH)

    return wrapper
  }

  private fun boundedPanel() : JPanel = buttonPanel("Bounded or Unbounded", "bound_exterior",
                                                    listOf("Bounded", "Unbounded"), listOf("true", "false"))

  private fun sizePanel() : JPanel = buttonPanel("Number Line Size", "line_size_temp", listOf("Small", "Medium", "Large"),
                                                 listOf("small", "medium", "large"))

  private fun customInstruction() : JPanel {
    val panel = buttonPanel("Custom Instructions", "use_cust_instruct", listOf("Yes", "No"),
            listOf("true", "false"))
    panel.border = BorderFactory.createEmptyBorder()

    val savePanel = JPanel()
    val saveTxtField = JTextField(20)
    addTrackedTxtField(saveTxtField,"cust_instruct", "Custom Instructions", savePanel, txtMap, false)
    saveTxtField.text = ""

    val fc = JFileChooser()
    fc.fileSelectionMode = JFileChooser.FILES_ONLY

    val saveButton = JButton("Select File")
    saveButton.addActionListener {
      val returnVal = fc.showSaveDialog(this)

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        saveTxtField.text = fc.selectedFile.canonicalPath
      }
    }

    savePanel.add(saveButton)

    val finalPanel = JPanel()
    finalPanel.layout = BorderLayout()
    finalPanel.add(panel, BorderLayout.NORTH)
    //finalPanel.add(savePanel, BorderLayout.SOUTH)

    finalPanel.border = BorderFactory.createTitledBorder("Custom instructions")

    val btnGrp = btnGrps["use_cust_instruct"]!!
    val btns = btnGrp.elements.toList()

    btns[0].addItemListener {
      if (it.stateChange == ItemEvent.SELECTED) {
        finalPanel.add(savePanel, BorderLayout.SOUTH)
        finalPanel.revalidate()
        (this.rootPane.parent as Dialog).pack()
      }
    }

    btns[1].addItemListener {
      if (it.stateChange == ItemEvent.SELECTED) {
        finalPanel.remove(savePanel)
        finalPanel.revalidate()
        (this.rootPane.parent as Dialog).pack()
      }
    }


    return finalPanel
  }

  private fun biasPanel() : JPanel {
    val panel = borderTitlePanel("Estimated Largest Bias")
    panel.layout = GridLayout(2, 3)

    val childRadBtn = JRadioButton("Child")
    childRadBtn.actionCommand = "1.4"
    childRadBtn.isSelected = true

    val adultRadBtn = JRadioButton("Adult")
    adultRadBtn.actionCommand = "1.2"

    val othRadBtn   = JRadioButton("Other")
    txtFld.text = "0.0"
    txtFld.document.addDocumentListener(object : DocumentListener {
      override fun removeUpdate(p0: DocumentEvent?) {
        if (txtFld.text.isNotEmpty()) {
          othRadBtn.actionCommand = txtFld.text
          updateLargeLbl()
        }
      }

      override fun insertUpdate(p0: DocumentEvent?) {
        if (txtFld.text.isNotEmpty()) {
          othRadBtn.actionCommand = txtFld.text
          updateLargeLbl()
        }
      }

      override fun changedUpdate(p0: DocumentEvent?) {
        if (txtFld.text.isNotEmpty()) {
          othRadBtn.actionCommand = txtFld.text
          updateLargeLbl()
        }
      }
    })
    othRadBtn.addActionListener { othRadBtn.actionCommand = txtFld.text }
    othRadBtn.actionCommand = txtFld.text

    val btnGrp = ButtonGroup()
    btnGrp.add(childRadBtn)
    btnGrp.add(adultRadBtn)
    btnGrp.add(othRadBtn)

    btnGrps["bias"] = btnGrp

    panel.add(childRadBtn)
    panel.add(adultRadBtn)
    panel.add(othRadBtn)

    panel.add(JLabel("1.4"))
    panel.add(JLabel("1.2"))
    panel.add(txtFld)

    return panel
  }

  private fun largestTarget() : JPanel {
    val panel = borderTitlePanel("Largest Estimation Target or Right Bound")

    panel.add(largeLbl)

    return panel
  }

  private fun borderTitlePanel(title: String) : JPanel {
    val panel = JPanel()

    panel.border = BorderFactory.createTitledBorder(title)

    return panel
  }

  private fun buttonPanel(title: String = "", key: String, butStrs: List<String>, cmds: List<String>) : JPanel {
    val buts = butStrs.map { JRadioButton(it) }
    buts.forEachIndexed {i, it -> it.actionCommand = cmds[i]}
    buts[0].isSelected = true

    val btnGrp = ButtonGroup()
    buts.forEach { btnGrp.add(it) }
    btnGrps[key] = btnGrp

    val panel = if (title.isEmpty()) JPanel() else borderTitlePanel(title)
    buts.forEach { panel.add(it) }

    return panel
  }

  fun getBundle() : Bundle {
    val bundle = baseBundle

    txtMap.forEach { k, txt -> bundle.add(k, txt.text) }
    btnGrps.forEach { s, btnGrp -> bundle.add(s, btnGrp.selection.actionCommand) }
    bundle.add("largest_target", largeLbl.text.split(":")[1])
    bundle.add("line_size", baseBundle.getAsString("width_${bundle.getAsString("line_size_temp")}_mod"))

    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val gs = ge.screenDevices
    val rr = gs[0].displayMode.refreshRate
    val refreshRate = if (rr == DisplayMode.REFRESH_RATE_UNKNOWN) 60 else rr

    if (baseBundle.getAsBoolean("stim_time_off")) {
      baseBundle.add("est_stim_time", 0)
    } else {
      baseBundle.add("est_stim_time", baseBundle.getAsInt("scalar_field") * (1000 / refreshRate))
    }

    return bundle
  }

  fun applyDefaults(defs: Bundle) {
    val txtMatches = txtMap.filter { defs.contains(it.key) }
    val btnMatches = btnGrps.filter { defs.contains(it.key) }

    txtMatches.forEach { it -> it.value.text = defs.getAsString(it.key) }
    btnMatches.forEach { pair ->
        pair.value.elements.toList()
                           .find { it.actionCommand == defs.getAsString(pair.key) }
                          ?.isSelected = true
    }

    // Handle the case where the other bias was selected.
    val bias = defs.getAsString("bias").toDouble()
    if (bias != 1.2 && bias != 1.4) {
      val btn = btnMatches["bias"]!!.elements.toList().find { it.actionCommand == "0.0"}!!
      btn.isSelected = true
      btn.actionCommand = bias.toString()
      txtFld.text = bias.toString()
    }

    // Update the label since calculateMaxTarget() was called before setting the defaults.
    updateLargeLbl()
  }

  fun calculateMaxTarget() : Long {
    if (baseBundle.size == 0) return 0

    val bun = getBundle()

    val bounded = bun.getAsBoolean("bound_exterior")

    val bias = bun.getAsString("bias").toDouble()
    val margin = baseBundle.getAsInt("left_margin_high")
    val widthHigh = baseBundle.getAsInt("width_high")
    val scale = bun.getAsInt("line_size")
    val leftBound : Long = bun.getAsInt("start_unit").toLong()

    val maxPix = screenWidth().toDouble() - (2.0 * margin)
    val unitPix = widthHigh.toDouble() * scale

    baseBundle.add("width_interval", unitPix.toInt())

    var max = if (bounded) maxPix / unitPix
              else pow(5.0 / 6.0, (1.0 / bias)) * pow(maxPix / unitPix, 1.0 / bias)

    if (max > maxPix/unitPix) max = maxPix / unitPix

    return if (max.isNaN() || max.isInfinite()) leftBound else max.toLong() + leftBound
  }

  private fun updateLargeLbl() {
    largeLbl.text = "Largest target value or right bound allowed: ${calculateMaxTarget()}"
  }
}
