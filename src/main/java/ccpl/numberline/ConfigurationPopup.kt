package ccpl.numberline

import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.Point
import java.awt.Toolkit
import java.io.File
import java.net.URL
import java.text.NumberFormat
import javax.swing.*
import javax.swing.text.NumberFormatter

class ConfigurationPopup(private val cb: PopupCallback, title: String?) : JFrame(title) {

  private val textKeys = listOf("subject", "session", "num_trials", "num_prac_trials",
      "start_unit", "end_unit", "target_unit_low", "target_unit_high",
      "target_unit_interval")

  private val textLabels = listOf("Subject",  "Session", "Number of Trials", "Number of Practice Trials",
      "Start Unit", "End Unit", "Target Unit Low", "Target Unit High",
      "Target Unit Interval")

  private val textFields: MutableMap<String, JTextField> = mutableMapOf()

  private val estimateProd: Array<String> = arrayOf("Estimation", "Production")
  private val trueFalse: Array<String> = arrayOf("Bounded", "Unbounded")
  private val smallMedLarge: Array<String> = arrayOf("Small", "Medium", "Large")
  private val centerPanel = JPanel()

  private val exteriorBoundDropDown: JComboBox<String> = JComboBox(trueFalse)

  private val homeDir: String = System.getProperty("user.home")
  private val defaultConfigLoc = homeDir + "/.port_num/defaults_config_popup"
  private val saveTxtField = JTextField(20)
  private val errorTextField = JLabel()

  private var baseBundle = Bundle()

  init {
    val cl = ClassLoader.getSystemClassLoader()
    baseBundle = readDbFile(cl.getResource("exp/infiles/base_exp.txt"))

    val defaultConfigExist = File(defaultConfigLoc).exists()

    defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

    val contentPanel = JPanel()
    contentPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    contentPanel.layout = BorderLayout()

    centerPanel.layout = GridLayout(0, 2, 0, 2)

    val fc = JFileChooser()
    fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

    val saveLabel = JLabel("Save: ")


    val saveButton = JButton("Save Directory")
    saveButton.addActionListener {
      val returnVal = fc.showSaveDialog(this)

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        saveTxtField.text = fc.selectedFile.canonicalPath
      }
    }

    val topPanel = JPanel()
    topPanel.layout = BorderLayout()
    errorTextField.isVisible = false
    val topCenter = JPanel()
    topPanel.add(errorTextField, BorderLayout.NORTH)
    topCenter.add(saveLabel)
    topCenter.add(saveTxtField)
    topCenter.add(saveButton)
    topPanel.add(topCenter, BorderLayout.CENTER)
    contentPanel.add(topPanel, BorderLayout.NORTH)

    val textField = JTextField()
    textFields.put("condition", textField)
    addToCenterPanel("Condition", textField)

    textFields.put("condition", textField)

    textKeys.indices.forEach { addTextField(textKeys[it], textLabels[it]) }

    val estimationDropDown: JComboBox<String> = JComboBox(estimateProd)
    val widthLow: JComboBox<String> = JComboBox(smallMedLarge)

    addToCenterPanel("Estimation or Production", estimationDropDown)
    addToCenterPanel("Bounded", exteriorBoundDropDown)
    addToCenterPanel("Number Line Size", widthLow)

    val okayButton = JButton("Okay")
    okayButton.addActionListener({
      if (checksPass()) {
        textFields.forEach { tf -> bunAdd(tf.key, tf.value.text) }

        bunAdd("estimation_task", estimateProd[0] == estimationDropDown.selectedItem)
        bunAdd("target_label_on", (!cb.bundle.getAsBoolean("estimation_task")).toString())

        bunAdd("bound_exterior", exteriorBoundDropDown.selectedItem == "Bounded")
        bunAdd("line_size", widthLow.selectedItem)

        bunAdd("save_dir", saveTxtField.text)

        writeDbFile(cb.bundle, URL("file://" + defaultConfigLoc))

        this.isVisible = false
        this.dispose()
      }
    })

    if (defaultConfigExist) {
      val bundle = loadDefaults()
      setTextDefaults(bundle)
      val estTask = if (safeGrab(bundle, "estimation_task") != "NULL") safeGrab(bundle, "estimation_task") else ""

      saveTxtField.text = if (safeGrab(bundle, "save_dir") != "NULL") safeGrab(bundle, "save_dir") else ""
      widthLow.selectedItem = if (safeGrab(bundle, "line_size") != "NULL") safeGrab(bundle, "line_size") else ""
      exteriorBoundDropDown.selectedItem = if (safeGrab(bundle, "bound_exterior") != "NULL") safeGrab(bundle, "bound_exterior") else ""
      estimationDropDown.selectedItem = if (estTask.toLowerCase() == "true") "Estimation" else "Production"
    }

    val exitButton = JButton("Exit")
    exitButton.addActionListener { System.exit(1) }

    val bottomPanel = JPanel()
    bottomPanel.add(okayButton)
    bottomPanel.add(exitButton)

    contentPanel.add(centerPanel, BorderLayout.CENTER)
    contentPanel.add(bottomPanel, BorderLayout.SOUTH)

    val screenSize = Toolkit.getDefaultToolkit().screenSize

    this.add(contentPanel)
    pack()
    location = Point((screenSize.width - width) / 2, (screenSize.height  - height) / 2)
    isVisible = true
  }

  private fun checksPass(): Boolean {
    errorTextField.isVisible = false
    errorTextField.text = ""


    var pass = true
    val subject = textFields["subject"]!!.text
    val session = textFields["session"]!!.text
    val sb = StringBuffer()

    if (File("${saveTxtField.text}/p${subject}s$session.dat").exists()) {
      pass = false
      sb.append("File already exists\n")
    }

    val screenWidth = Toolkit.getDefaultToolkit().screenSize.width
    val bias = 1.4
    val targetHigh = textFields["target_unit_high"]!!.text.toDouble()
    val endUnit = textFields["end_unit"]!!.text.toDouble()
    val boundedUnbounded = exteriorBoundDropDown.selectedItem
    val error = 0.2 * targetHigh * 3
    val margin = baseBundle.getAsInt("left_margin_low")
    val widthHigh = baseBundle.getAsInt("width_high")

    if (boundedUnbounded == "Bounded") {
      if (endUnit > screenWidth - margin * 2) {
        pass = false
        sb.append("End unit cannot fit on screen\n")
      }
    } else {
      val largestTarget = (Math.pow(targetHigh / endUnit, bias) + error)  * widthHigh + margin * 2

      if (targetHigh > largestTarget || largestTarget > screenWidth) {
        pass = false
        sb.append("Largest target cannot fit on screen\n")
      }
    }

    if (sb.isNotEmpty()) {
      errorTextField.isVisible = true
      errorTextField.text = sb.toString()
    }

    return pass
  }

  private fun setTextDefaults(bundle: Bundle) =
    textFields.forEach{ (key, tf) ->
      run {
        try {
          tf.text = bundle.getAsString(key)
        } catch(e: Exception) {
          tf.text = ""
        }
      }
    }

  private fun safeGrab(bundle: Bundle, key: String): String = try {
      bundle.getAsString(key)
    } catch (e: Exception) {
      "NULL"
    }

  private fun addTextField(key: String, label: String) {
    val textField = JTextField()
    textFields.put(key, textField)
    addToCenterPanel(label, textField)
  }

  private fun bunAdd(key: String, value: Any) = cb.bundle.add(key, value)

  private fun addToCenterPanel(label: String, component: JComponent) {
    (centerPanel.layout as GridLayout).rows.plus(1)
    centerPanel.add(JLabel(label + ": "))
    centerPanel.add(component)
  }

  private fun loadDefaults() : Bundle = readDbFile(URL("file://" + defaultConfigLoc))
}