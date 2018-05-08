package ccpl.numberline.config

import ccpl.lib.Bundle
import ccpl.lib.util.DatabaseFileReader.readDbFile
import ccpl.lib.util.DatabaseFileReader.writeDbFile
import ccpl.lib.util.UiUtil.addTrackedTxtField
import ccpl.lib.util.UiUtil.expandGridPanel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Point
import java.awt.Toolkit
import java.io.File
import java.net.URL
import java.text.DecimalFormat
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFormattedTextField
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.WindowConstants

class ConfigFrame(private val cb: PopupCallback, title: String?) : JFrame(title) {

  private val textKeys = listOf("subject", "session")

  private val textLabels = listOf("Subject",  "Session")

  private val textFields: MutableMap<String, JTextField> = mutableMapOf()

  private val centerPanel = JPanel()

  private val homeDir: String = System.getProperty("user.home")
  private val defaultConfigLoc = "$homeDir/.port_num/defaults_config_popup"
  private val saveTxtField = JTextField(20)
  private val errorTextField = JLabel("", SwingConstants.CENTER)

  private var baseBundle = Bundle()

  private val configDialog = DetailedConfigDialog()

  init {
    val cl = ClassLoader.getSystemClassLoader()
    baseBundle = readDbFile(cl.getResource("exp/infiles/base_exp.txt"))
    configDialog.baseBundle(baseBundle)

    val defaultConfigExist = File(defaultConfigLoc).exists()

    defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

    val contentPanel = JPanel()
    contentPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    contentPanel.layout = GridBagLayout()
    val contentConstraints = GridBagConstraints()

    centerPanel.layout = GridLayout(0, 2, 0, 2)

    val fc = JFileChooser()
    fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

    val saveLabel = JLabel("Save: ")

    val saveButton = JButton("Save Directory")
    saveButton.addActionListener {
      val returnVal = fc.showSaveDialog(this)

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        if (!fc.selectedFile.canWrite()) {
          JOptionPane.showMessageDialog(this, "Unable to select directory: No write permissions" +
              "\nPlease select a different directory")
        }

        saveTxtField.text = fc.selectedFile.canonicalPath
      }
    }

    val topPanel = JPanel()
    topPanel.layout = BorderLayout()
    errorTextField.isVisible = false
    errorTextField.foreground = Color.RED
    val topCenter = JPanel()
    topPanel.add(errorTextField, BorderLayout.NORTH)
    topCenter.add(saveLabel)
    topCenter.add(saveTxtField)
    topCenter.add(saveButton)
    topPanel.add(topCenter, BorderLayout.CENTER)

    contentConstraints.gridy = 0
    contentPanel.add(topPanel, contentConstraints)

    val textField = JTextField()
    textFields["condition"] = textField
    expandGridPanel(centerPanel, "Condition", textField)

    textFields["condition"] = textField

    textKeys.indices.forEach {
      if (textKeys[it] == "session") {
        val format = JFormattedTextField(DecimalFormat("###"))
        addTrackedTxtField(format, textKeys[it], textLabels[it], centerPanel, textFields, true)
      } else {
        addTrackedTxtField(textKeys[it], textLabels[it], centerPanel, textFields, true)
      }
    }

    val okayButton = JButton("Okay")
    okayButton.addActionListener({
      if (checksPass()) {
        textFields.forEach { tf -> bunAdd(tf.key, tf.value.text) }

        cb.bundle = cb.bundle.merge(configDialog.getBundle())
        bunAdd("target_label_on", true.toString())
        bunAdd("save_dir", saveTxtField.text)

        writeDbFile(cb.bundle, URL("file://$defaultConfigLoc"))

        this.isVisible = false
        this.dispose()
      }
    })

    if (defaultConfigExist) {
      val bundle = loadDefaults()
      setTextDefaults(bundle)
      configDialog.setDefaults(bundle)
      saveTxtField.text = if (safeGrab(bundle, "save_dir") != "NULL") safeGrab(bundle, "save_dir") else ""
    }

    val configButton = JButton("Configure")
    configButton.addActionListener({
      configDialog.isVisible = true
    })

    val configButtonPanel = JPanel()
    configButtonPanel.add(configButton)

    val exitButton = JButton("Exit")
    exitButton.addActionListener { System.exit(1) }

    val bottomPanel = JPanel()
    bottomPanel.add(okayButton)
    bottomPanel.add(exitButton)

    val centerPanelWrapper = JPanel()
    centerPanelWrapper.layout = GridBagLayout()
    val c = GridBagConstraints()

    c.gridx = 0
    c.gridy = 0
    c.fill = GridBagConstraints.BOTH
    centerPanelWrapper.add(centerPanel, c)

    c.gridy = 1
    centerPanelWrapper.add(configButtonPanel, c)

    contentConstraints.gridy = 1
    contentPanel.add(centerPanelWrapper, contentConstraints)

    contentConstraints.gridy = 2
    contentPanel.add(bottomPanel, contentConstraints)

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
    val sb = StringBuilder()

    if (File("${saveTxtField.text}/p${subject}s$session.dat").exists()) {
      pass = false
      sb.append("Data file for subject and session number already exists.\n")
    } else if (saveTxtField.text.isEmpty()) {
      pass = false
      sb.append("No file specified\n")
    }

    if (sb.isNotEmpty()) {
      JOptionPane.showMessageDialog(this, sb)
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

  private fun bunAdd(key: String, value: Any) = cb.bundle.add(key, value)

  private fun loadDefaults() : Bundle = readDbFile(URL("file://$defaultConfigLoc"))

  override fun dispose() {
    configDialog.dispose()
    super.dispose()
  }
}