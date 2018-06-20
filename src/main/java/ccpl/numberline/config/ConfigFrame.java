package ccpl.numberline.config;

import static ccpl.lib.util.DatabaseFileReader.readDbFile;
import static ccpl.lib.util.DatabaseFileReader.writeDbFile;
import static ccpl.lib.util.UiUtil.addTrackedTxtField;
import static ccpl.lib.util.UiUtil.createPanelWithBorderTitle;
import static ccpl.lib.util.UiUtil.expandGridPanel;

import ccpl.lib.Bundle;
import ccpl.lib.util.DatabaseFileReader;
import ccpl.lib.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class ConfigFrame extends JFrame {

  private Logger log = Logger.getLogger(ConfigFrame.class.getName());

  private List<String> textKeys = Arrays.asList("subject", "session");

  private List<String> textLabels = Arrays.asList("Subject", "Session");

  private Map<String, JTextField> textFields = new HashMap<>();

  private JPanel centerPanel = new JPanel();

  private String homeDir = System.getProperty("user.home");
  private String defaultConfigLoc;
  private JTextField saveTxtField = new JTextField(20);
  private JLabel errorTextField = new JLabel("", SwingConstants.CENTER);

  private Bundle baseBundle;

  private DetailedConfigDialog configDialog = new DetailedConfigDialog();

  private PopupCallback cb;

  /**
   * Frame that holds configuration for subject, session, and condition.
   *
   * @param cb Callback
   * @param title Title of frame
   */
  public ConfigFrame(PopupCallback cb, String title) {
    super(title);
    this.cb = cb;
    this.defaultConfigLoc = String.format("%s/.port_num/defaults_config_popup", homeDir);
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    baseBundle = readDbFile(cl.getResource("exp/infiles/base_exp.txt"));
    configDialog.setBaseBundle(baseBundle);

    final boolean defaultConfigExist = new File(defaultConfigLoc).exists();

    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    JPanel contentPanel = new JPanel();
    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    contentPanel.setLayout(new GridBagLayout());

    centerPanel.setLayout(new GridLayout(0, 2, 0, 2));

    JFileChooser fc = new JFileChooser();
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    final JLabel saveLabel = new JLabel("Save: ");

    JButton saveButton = new JButton("Save Directory");
    saveButton.addActionListener(
        actionEvent -> {
          int returnVal = fc.showSaveDialog(this);

          if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (!fc.getSelectedFile().exists()) {
              fc.setSelectedFile(fc.getSelectedFile().getParentFile());
              JOptionPane.showMessageDialog(
                  this,
                  "Selected directory does not exist.\n"
                      + "Falling back to "
                      + fc.getSelectedFile().getAbsolutePath());
            }

            if (!fc.getSelectedFile().canWrite()) {
              JOptionPane.showMessageDialog(
                  this,
                  "Unable to select directory: No write permissions"
                      + "\nPlease select a different directory");
            }

            try {
              saveTxtField.setText(fc.getSelectedFile().getCanonicalPath());
            } catch (IOException e) {
              log.log(Level.WARNING, e.getLocalizedMessage());
            }
          }
        });

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BorderLayout());
    errorTextField.setVisible(false);
    errorTextField.setForeground(Color.RED);
    JPanel topCenter = new JPanel();
    topPanel.add(errorTextField, BorderLayout.NORTH);
    topCenter.add(saveLabel);
    topCenter.add(saveTxtField);
    topCenter.add(saveButton);
    topPanel.add(topCenter, BorderLayout.CENTER);

    GridBagConstraints contentConstraints = new GridBagConstraints();
    contentConstraints.gridy = 0;
    contentPanel.add(topPanel, contentConstraints);

    JTextField textField = new JTextField();
    textFields.put("condition", textField);
    expandGridPanel(centerPanel, "Condition", textField);

    textFields.put("condition", textField);

    for (int i = 0; i < textKeys.size(); i++) {
      if (textKeys.get(i).equals("session")) {
        JFormattedTextField format = new JFormattedTextField(new DecimalFormat("###"));
        addTrackedTxtField(
            format, textKeys.get(i), textLabels.get(i), centerPanel, textFields, true);
      } else {
        addTrackedTxtField(textKeys.get(i), textLabels.get(i), centerPanel, textFields, true);
      }
    }

    JButton okayButton = new JButton("Okay");
    okayButton.addActionListener(
        actionEvent -> {
          if (checksPass()) {
            textFields.forEach((key, value) -> bunAdd(key, value.getText()));

            cb.bundle = cb.bundle.merge(configDialog.getBundle());
            bunAdd("target_label_on", true);
            bunAdd("save_dir", saveTxtField.getText());

            try {
              writeDbFile(cb.bundle, new URL(String.format("file://%s", defaultConfigLoc)));
            } catch (MalformedURLException e) {
              log.log(Level.WARNING, e.getLocalizedMessage());
            }

            this.setVisible(false);
            this.dispose();
          }
        });

    if (defaultConfigExist) {
      Bundle bundle = null;
      try {
        bundle = loadDefaults();
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
      setTextDefaults(bundle);
      configDialog.setDefaults(bundle);
      saveTxtField.setText(
          (!Objects.equals(safeGrab(bundle, "save_dir"), "NULL"))
              ? safeGrab(bundle, "save_dir")
              : "");
    }

    JPanel ageGradeContent = new JPanel();
    ageGradeContent.setLayout(new GridLayout(1, 1));
    addTrackedTxtField("subj_age", "Subject Age", ageGradeContent, textFields, true);
    addTrackedTxtField("subj_grade", "Subject Grade", ageGradeContent, textFields, true);

    AbstractButton noButton = new JRadioButton("No");
    noButton.setSelected(true);

    Component ageGradePanel =
        UiUtil.createToggleablePanel(
            this,
            createPanelWithBorderTitle("Include age and grade?"),
            ageGradeContent,
            new JRadioButton("Yes"),
            noButton);

    JButton configButton = new JButton("Configure");
    configButton.addActionListener(actionEvent -> configDialog.setVisible(true));

    JButton loadConfigButton = new JButton("Load Configuration");
    loadConfigButton.addActionListener(
        actionEvent -> {
          JFileChooser loadFileChooser = new JFileChooser();
          int ret = loadFileChooser.showOpenDialog(this);

          if (ret == JFileChooser.CANCEL_OPTION) {
            return;
          }

          try {
            URL path = loadFileChooser.getSelectedFile().toURI().toURL();
            configDialog.setDefaults(DatabaseFileReader.readDbFile(path));
          } catch (MalformedURLException e) {
            log.log(Level.WARNING, "Unable to load configuration file", e);
          }
        });

    JPanel configButtonPanel = new JPanel();
    configButtonPanel.add(loadConfigButton);
    configButtonPanel.add(configButton);

    JButton exitButton = new JButton("Exit");
    exitButton.addActionListener(actionEvent -> System.exit(1));

    JPanel bottomPanel = new JPanel();
    bottomPanel.add(okayButton);
    bottomPanel.add(exitButton);

    JPanel centerPanelWrapper = new JPanel();
    centerPanelWrapper.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.BOTH;
    centerPanelWrapper.add(centerPanel, c);

    c.gridy++;
    centerPanelWrapper.add(ageGradePanel, c);

    c.gridy++;
    centerPanelWrapper.add(configButtonPanel, c);

    contentConstraints.gridy = 1;
    contentPanel.add(centerPanelWrapper, contentConstraints);

    contentConstraints.gridy = 2;
    contentPanel.add(bottomPanel, contentConstraints);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    this.add(contentPanel);
    pack();
    this.setLocation(
        new Point((screenSize.width - this.getWidth()) / 2, (screenSize.height - getHeight()) / 2));
    this.setVisible(true);
  }

  private boolean checksPass() {
    errorTextField.setVisible(false);
    errorTextField.setText("");

    boolean pass = true;
    String subject = textFields.get("subject").getText();
    String session = textFields.get("session").getText();
    StringBuilder sb = new StringBuilder();

    String filePath = String.format("%s/p%ss%s.dat", saveTxtField.getText(), subject, session);

    if (new File(filePath).exists()) {
      pass = false;
      sb.append("Data file for subject and session number already exists.\n");
    } else if (saveTxtField.getText().isEmpty()) {
      pass = false;
      sb.append("No file specified\n");
    }

    if (!sb.toString().isEmpty()) {
      JOptionPane.showMessageDialog(this, sb);
    }

    return pass;
  }

  private void setTextDefaults(Bundle bundle) {
    textFields.forEach(
        (key, tf) -> {
          try {
            tf.setText(bundle.getAsString(key));
          } catch (Exception e) {
            tf.setText("");
          }
        });
  }

  private String safeGrab(Bundle bundle, String key) {
    try {
      return bundle.getAsString(key);
    } catch (Exception e) {
      return "NULL";
    }
  }

  private void bunAdd(String key, Object value) {
    cb.bundle.add(key, value);
  }

  private Bundle loadDefaults() throws MalformedURLException {
    return readDbFile(new URL(String.format("file://%s", defaultConfigLoc)));
  }

  @Override
  public void dispose() {
    configDialog.dispose();
    super.dispose();
  }
}
