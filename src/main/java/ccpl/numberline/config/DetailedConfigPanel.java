package ccpl.numberline.config;

import ccpl.lib.Bundle;
import ccpl.lib.IntFilter;
import ccpl.lib.IntTextField;
import ccpl.numberline.FeatureSwitch;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import javax.swing.text.PlainDocument;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static ccpl.lib.util.UiUtil.addTrackedTxtField;
import static ccpl.lib.util.UiUtil.screenWidth;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;

class DetailedConfigPanel extends JPanel {

  private Map<String, ButtonGroup> btnGrps = new HashMap<>();

  private List<String> textKeys = Arrays.asList("num_trials", "num_prac_trials", "start_unit", "end_unit");

  private List<String> textLabels = Arrays.asList("Number of Trials", "Number of Practice Trials", "Left Bound", "Right Bound");

  private Map<String, JTextField> txtMap = new HashMap<>();

  private JLabel largeLbl = new JLabel("Largest target value or right bound allowed: 0.0");
  private DecimalFormat intOnly = new DecimalFormat("###");
  private DecimalFormat twoSig = new DecimalFormat("###.##");

  private JFormattedTextField txtFld = new JFormattedTextField(twoSig);

  private Bundle baseBundle = new Bundle();


  public DetailedConfigPanel() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    this.add(textPanel());
    this.add(targetPanel());
    this.add(largestTarget());
    this.add(estPanel());
    this.add(boundedPanel());
    this.add(sizePanel());
    this.add(biasPanel());
    this.add(customInstruction());

    txtMap.forEach((notNeeded, txtField) -> txtField.getDocument().addDocumentListener(new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent documentEvent) {
            if (!txtField.getText().isEmpty()) {
              updateLargeLbl();
            }
          }

          @Override
          public void removeUpdate(DocumentEvent documentEvent) {
            if (!txtField.getText().isEmpty()) {
              updateLargeLbl();
            }
          }

          @Override
          public void changedUpdate(DocumentEvent documentEvent) {
            if (!txtField.getText().isEmpty()) {
              updateLargeLbl();
            }
          }
        })
    );
    btnGrps.forEach((notNeeded, btnGrp) -> Collections.list(btnGrp.getElements())
        .forEach(it -> it.addActionListener(actionEvent -> updateLargeLbl())));

    JTextField txt = txtMap.get("start_unit");
    txt.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent documentEvent) {
        ButtonGroup btnGrp = btnGrps.get("bound_exterior");
        List<AbstractButton> btns = Collections.list(btnGrp.getElements());
        if (!txt.getText().isEmpty() && btns.get(1).isSelected()) {
          updateRightBound();
        }
      }

      @Override
      public void removeUpdate(DocumentEvent documentEvent) {
        if (!txt.getText().isEmpty()) {
          updateRightBound();
        }
      }

      @Override
      public void changedUpdate(DocumentEvent documentEvent) {
        if (!txt.getText().isEmpty()) {
          updateRightBound();
        }

      }
    });
  }

  /**
   * Overridden add method to add rigid space between each section.
   * @param p0  Component being added to JPanel
   * @return    Component added
   */
  @Override public Component add(Component p0) {
    super.add(p0);
    return super.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  private JPanel textPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(0, 2, 0, 2));

    NumberFormatter formatter = new NumberFormatter(intOnly);
    formatter.setMinimum(0);
    formatter.setAllowsInvalid(false);
    formatter.setCommitsOnValidEdit(true);

    for (int i = 0; i < textKeys.size(); i++) {
      addTrackedTxtField(new IntTextField(), textKeys.get(i), textLabels.get(i), panel, txtMap, true);
    }

    return panel;
  }

  private JPanel targetPanel() {
    JPanel panel = borderTitlePanel("Target");
    panel.setLayout(new GridLayout(0, 6, 5, 1));

    List<String> txtKey = Arrays.asList("target_unit_low", "target_unit_high");
    List<String> txtLabel = Arrays.asList("From", "To");

    for (int i = 0; i < txtKey.size(); i++) {
      addTrackedTxtField(new JFormattedTextField(twoSig), txtKey.get(i), txtLabel.get(i), panel, txtMap, false) ;
    }

    JFormattedTextField txt = new JFormattedTextField(twoSig);
    addTrackedTxtField(txt, "target_unit_interval", "By", panel, txtMap, false) ;
    txt.setText("1");

    return panel;
  }

  private JPanel estPanel() {
    JPanel wrapper = new JPanel();
    wrapper.setLayout(new BorderLayout());

    JPanel panel = buttonPanel("Estimation or Production", "estimation_task",
        Arrays.asList("Estimation", "Production"), Arrays.asList("true", "false"));

    wrapper.add(panel, BorderLayout.NORTH);

    JPanel stimPanel = borderTitlePanel("Estimation Stim Time");
    JPanel stimSwitches = buttonPanel("", "stim_time_off", Arrays.asList("Unlimited", "Limited"), Arrays.asList("true", "false"));
    JPanel stimInfoPanel = new JPanel();

    stimPanel.setLayout(new BorderLayout());
    stimPanel.add(stimSwitches, BorderLayout.NORTH);

    ButtonGroup btnGrpSwitches = btnGrps.get("stim_time_off");
    List<AbstractButton> btnsSwitches = Collections.list(btnGrpSwitches.getElements());

    btnsSwitches.get(0).addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
        stimPanel.remove(stimInfoPanel);
        stimPanel.revalidate();
        ((Dialog) getRootPane().getParent()).pack();
      }
    });

    btnsSwitches.get(1).addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
        stimPanel.add(stimInfoPanel, BorderLayout.SOUTH);
        stimPanel.revalidate();
        ((Dialog) this.getRootPane().getParent()).pack();
      }
    });

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();

    int rr = gs[0].getDisplayMode().getRefreshRate();
    int refreshRate = rr == DisplayMode.REFRESH_RATE_UNKNOWN ? 60 : rr;
    baseBundle.add("refresh_rate", refreshRate);

    stimInfoPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 5;
    stimInfoPanel.add(new JLabel("Computer screen refresh interval: " + 1000 / refreshRate + " ms"), c);

    c.gridwidth = 1;
    c.gridy = 1;
    c.anchor = GridBagConstraints.CENTER;
    stimInfoPanel.add(new JLabel("Stim scalar: "), c);

    JTextField scalarField = new JTextField(4);
    ((PlainDocument) scalarField.getDocument()).setDocumentFilter(new IntFilter());

    addTrackedTxtField(scalarField, "scalar_field", "", stimPanel, txtMap, false);
    c.gridx = 1;
    stimInfoPanel.add(scalarField, c);

    c.gridx = 2;
    JLabel lbl = new JLabel("x" + 1000 / refreshRate + " ms: " + (parseInt(scalarField.getText()) * (1000 / refreshRate)) + "ms.");
    stimInfoPanel.add(lbl, c);

    scalarField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent documentEvent) {
        if (!scalarField.getText().isEmpty()) {
          lbl.setText("x" + 1000 / refreshRate + " ms: " + (parseInt(scalarField.getText()) * (1000 / refreshRate)) + "ms.");
        }
      }

      @Override
      public void removeUpdate(DocumentEvent documentEvent) {
        if (!scalarField.getText().isEmpty()) {
          lbl.setText("x" + 1000 / refreshRate + " ms: " + (parseInt(scalarField.getText()) * (1000 / refreshRate)) + "ms.");
        }
      }

      @Override
      public void changedUpdate(DocumentEvent documentEvent) {
        if (!scalarField.getText().isEmpty()) {
          lbl.setText("x" + 1000 / refreshRate + " ms: " + (parseInt(scalarField.getText()) * (1000 / refreshRate)) + "ms.");
        }
      }
    });

    ButtonGroup btnGrp = btnGrps.get("estimation_task");
    List<AbstractButton> btns = Collections.list(btnGrp.getElements());

    if (FeatureSwitch.USE_MASK) {
      btns.get(0).addItemListener(it -> {
        if (it.getStateChange() == ItemEvent.SELECTED) {
          wrapper.add(stimPanel, BorderLayout.SOUTH);
          wrapper.revalidate();
          ((Dialog) this.getRootPane().getParent()).pack();
        }
      });

      btns.get(1).addItemListener(it -> {
        if (it.getStateChange() == ItemEvent.SELECTED) {
          wrapper.remove(stimPanel);
          wrapper.revalidate();
          ((Dialog) this.getRootPane().getParent()).pack();
        }
      });

      wrapper.add(stimPanel, BorderLayout.SOUTH);
    } else {
      baseBundle.add("stim_time_off", true);
    }

    return wrapper;
  }

  private JPanel boundedPanel() {
    JPanel panel = buttonPanel("Bounded or Unbounded", "bound_exterior",
        Arrays.asList("Bounded", "Unbounded", "Universal"), Arrays.asList("true", "false", "FALSE"));

    ButtonGroup btnGrp = btnGrps.get("bound_exterior");
    List<AbstractButton> btns = Collections.list(btnGrp.getElements());

    btns.get(1).addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
        updateRightBound();
      } else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
        txtMap.get("end_unit").setEnabled(true);
      }
    });

    btns.get(2).addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
        txtMap.get("end_unit").setEnabled(true);
      }
    });

    return panel;
  }

  private void updateRightBound() {
    JTextField txt     = txtMap.get("end_unit");
    JTextField leftBnd = txtMap.get("start_unit");
    txt.setText(String.valueOf(Integer.valueOf(leftBnd.getText()) + 1));
    txt.setText(String.valueOf(Integer.valueOf(leftBnd.getText()) + 1));
    txt.setEnabled(false);
  }

  private JPanel sizePanel() {
    return buttonPanel("Number Line Size", "line_size_temp", Arrays.asList("Small", "Medium", "Large"),
        Arrays.asList("small", "medium", "large"));
  }

  private JPanel customInstruction() {
    JPanel panel = buttonPanel("Custom Instructions", "use_cust_instruct",
        Arrays.asList("Yes", "No"),
        Arrays.asList("true", "false"));

    panel.setBorder(BorderFactory.createEmptyBorder());

    JPanel savePanel = new JPanel();
    JTextField saveTxtField = new JTextField(20);
    addTrackedTxtField(saveTxtField,"cust_instruct", "Custom Instructions", savePanel, txtMap, false);
    saveTxtField.setText("");

    JFileChooser fc = new JFileChooser();
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

    JButton saveButton = new JButton("Select File");
    saveButton.addActionListener(actionEvent ->  {
      int returnVal = fc.showSaveDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        try {
          saveTxtField.setText(fc.getSelectedFile().getCanonicalPath());
        } catch (IOException e) {
          Logger.getLogger(DetailedConfigPanel.class.getName()).log(Level.SEVERE, e.getLocalizedMessage());
        }
      }
    });

    savePanel.add(saveButton);

    JPanel finalPanel = new JPanel();
    finalPanel.setLayout(new BorderLayout());
    finalPanel.add(panel, BorderLayout.NORTH);
    //finalPanel.add(savePanel, BorderLayout.SOUTH)

    finalPanel.setBorder(BorderFactory.createTitledBorder("Custom instructions"));

    ButtonGroup btnGrp = btnGrps.get("use_cust_instruct");
    List<AbstractButton> btns = Collections.list(btnGrp.getElements());

    btns.get(1).setSelected(true);

    btns.get(0).addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
        finalPanel.add(savePanel, BorderLayout.SOUTH);
        finalPanel.revalidate();
        ((Dialog) this.getRootPane().getParent()).pack();
      }
    });

    btns.get(1).addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
        finalPanel.remove(savePanel);
        finalPanel.revalidate();
        ((Dialog) this.getRootPane().getParent()).pack();
      }
    });


    return finalPanel;
  }

  private JPanel biasPanel() {
    JPanel panel = borderTitlePanel("Estimated Largest Bias");
    panel.setLayout(new GridLayout(2, 3));

    JRadioButton childRadBtn = new JRadioButton("Child");
    childRadBtn.setActionCommand("1.4");
    childRadBtn.setSelected(true);

    JRadioButton adultRadBtn = new JRadioButton("Adult");
    adultRadBtn.setActionCommand("1.2");

    JRadioButton othRadBtn = new JRadioButton("Other");
    txtFld.setText("0.0");
    txtFld.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void removeUpdate(DocumentEvent p0) {
        if (!txtFld.getText().isEmpty()) {
          othRadBtn.setActionCommand(txtFld.getText());
          updateLargeLbl();
        }
      }

      @Override public void insertUpdate(DocumentEvent p0) {
        if (!txtFld.getText().isEmpty()) {
          othRadBtn.setActionCommand(txtFld.getText());
          updateLargeLbl();
        }
      }

      @Override public void changedUpdate(DocumentEvent p0) {
        if (!txtFld.getText().isEmpty()) {
          othRadBtn.setActionCommand(txtFld.getText());
          updateLargeLbl();
        }
      }
    });

    othRadBtn.addActionListener(actionEvent -> othRadBtn.setActionCommand(txtFld.getText()));
    othRadBtn.setActionCommand(txtFld.getText());

    ButtonGroup btnGrp = new ButtonGroup();
    btnGrp.add(childRadBtn);
    btnGrp.add(adultRadBtn);
    btnGrp.add(othRadBtn);

    btnGrps.put("bias", btnGrp);

    panel.add(childRadBtn);
    panel.add(adultRadBtn);
    panel.add(othRadBtn);

    panel.add(new JLabel("1.4"));
    panel.add(new JLabel("1.2"));
    panel.add(txtFld);

    return panel;
  }

  private JPanel largestTarget() {
    JPanel panel = borderTitlePanel("Largest Estimation Target or Right Bound");

    panel.add(largeLbl);

    return panel;
  }

  private JPanel borderTitlePanel(String title) {
    JPanel panel = new JPanel();

    panel.setBorder(BorderFactory.createTitledBorder(title));

    return panel;
  }

  private JPanel buttonPanel(String title, String key, List<String> butStrs, List<String> cmds) {
    List<JRadioButton> buts = butStrs.stream()
        .map(JRadioButton::new)
        .collect(Collectors.toCollection(ArrayList::new));

    for (int i = 0; i < buts.size(); i++) {
      buts.get(i).setActionCommand(cmds.get(i));
    }

    buts.get(0).setSelected(true);

    ButtonGroup btnGrp = new ButtonGroup();
    buts.forEach(btnGrp::add);
    btnGrps.put(key, btnGrp);

    JPanel panel = title.isEmpty() ? new JPanel() : borderTitlePanel(title);
    buts.forEach(panel::add);

    return panel;
  }

  Bundle getBundle() {
    Bundle bundle = baseBundle;

    txtMap.forEach((k, txt) -> bundle.add(k, txt.getText()));
    btnGrps.forEach((s, btnGrp) -> bundle.add(s, btnGrp.getSelection().getActionCommand()));
    bundle.add("largest_target", largeLbl.getText().split(":")[1]);
    bundle.add("line_size", baseBundle.getAsString("width_" + bundle.getAsString("line_size_temp") + "_mod"));

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();
    int rr = gs[0].getDisplayMode().getRefreshRate();
    int refreshRate = rr == DisplayMode.REFRESH_RATE_UNKNOWN ? 60 : rr;

    if (baseBundle.getAsBoolean("stim_time_off")) {
      baseBundle.add("est_stim_time", 0);
    } else {
      baseBundle.add("est_stim_time", baseBundle.getAsInt("scalar_field") * (1000 / refreshRate));
    }

    return bundle;
  }

  public void applyDefaults(Bundle defs) {
    Map<String, JTextField> txtMatches = txtMap.entrySet()
        .stream()
        .filter(it -> defs.contains(it.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<String, ButtonGroup> btnMatches = btnGrps.entrySet()
        .stream()
        .filter(it -> defs.contains(it.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    txtMatches.forEach((key, value) -> value.setText(defs.getAsString(key)));

    btnMatches.forEach((key, value) -> Collections.list(value.getElements())
        .stream()
        .filter(it -> it.getActionCommand().equals(defs.getAsString(key)))
        .findFirst()
        .ifPresent(it -> it.setSelected(true))
    );


    // Handle the case where the other bias was selected.
    double bias = parseDouble(defs.getAsString("bias"));
    if (bias != 1.2 && bias != 1.4) {
      JButton btn = (JButton) Collections.list(btnMatches.get("bias").getElements())
          .stream()
          .filter(it -> it.getActionCommand().equals("0.0"))
          .findFirst()
          .get();

      btn.setSelected(true);
      btn.setActionCommand(Double.toString(bias));
      txtFld.setText(Double.toString(bias));
    }

    // Update the label since calculateMaxTarget() was called before setting the defaults.
    updateLargeLbl();
  }

  public long calculateMaxTarget() {
    if (baseBundle.getSize() == 0) {
      return 0;
    }

    Bundle bun = getBundle();

    boolean bounded = bun.getAsBoolean("bound_exterior");
    boolean estimate = bun.getAsBoolean("estimation_task");

    double bias = parseDouble(bun.getAsString("bias"));
    int margin = baseBundle.getAsInt("left_margin_high");
    int widthHigh = baseBundle.getAsInt("width_high");
    int scale = bun.getAsInt("line_size");
    long leftBound = bun.getAsInt("start_unit");

    double maxPix = 1.0 * screenWidth() - (2.0 * margin);
    double unitPix = 1.0 * widthHigh * scale;

    baseBundle.add("width_interval", (int) unitPix);

    Double max = bounded || estimate ? maxPix / unitPix : pow(5.0 / 6.0, (1.0 / bias)) * pow(maxPix / unitPix, 1.0 / bias);

    if (max > maxPix/unitPix) {
      max = maxPix / unitPix;
    }

    return max.isNaN() || max.isInfinite() ? leftBound : max.longValue() + leftBound;
  }

  private void updateLargeLbl() {
    largeLbl.setText(String.format("Largest target value or right bound allowed: %s", calculateMaxTarget()));
  }

  public void setBaseBundle(Bundle value) {
    baseBundle = value;
    updateLargeLbl();
  }
}
