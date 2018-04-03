package ccpl.numberline;

import static ccpl.lib.util.DatabaseFileReaderKt.readDbFile;

import ccpl.lib.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import java.text.DecimalFormat;

import java.util.Date;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Number line experiment that displays a number line and asks the user for feedback.
 * Modified by Kyle Holt.
 * November, 2010.
 *
 * @author dalecohen
 */
public class UniversalNumberLine extends Experiment implements ActionListener {

  private final boolean isEstimationTask;

  private Bundle dataBundle;

  private final int numberOfTrials;
  private final int numOfPracTrials;

  private static NumberLine numLine = null;
  private static RandomIntGenerator randGen = new RandomIntGenerator();
  private static BlankPanel imPanel;
  private static DrawExpFrame frame;

  /**
   * Parameterized constructor allow the specification of an experiment file,
   * the subject ID, condition, and session number.
   *
   * @param expFile Experiment file
   * @param sub     Subject ID
   * @param cond    Condition
   * @param sess    Session number
   */
  public UniversalNumberLine(String expFile, String sub, String cond, String sess,
                             Bundle dataBundle) {
    super(expFile, sub, cond, sess, dataBundle.getAsString("save_dir"));

    this.dataBundle = dataBundle;

    trialType = 0;
    trialNum = 0;

    this.numberOfTrials = dataBundle.getAsInt("num_trials");
    this.numOfPracTrials = dataBundle.getAsInt("num_prac_trials");

    this.isEstimationTask = dataBundle.getAsBoolean("estimation_task");
  }

  /**
   * Runs the UniversalNumberLine experiment with the specified database file.
   */
  public void run() {
    ClassLoader cl = this.getClass().getClassLoader();
    URL newLayoutPath = cl.getResource(experiment + "/infiles/dbfile_new_layout.txt");
    URL baseExpPath = cl.getResource(experiment + "/infiles/base_exp.txt");

    // Read DB
    assert newLayoutPath != null;
    final Bundle dbBundle = readDbFile(newLayoutPath);

    assert baseExpPath != null;
    Bundle baseExp = readDbFile(baseExpPath);

    dataBundle = dataBundle.merge(baseExp);

    // BEGIN PARSING OF DATABASE FILE
    trialType = numOfPracTrials != 0 ? 0 : 1;

    if (dataBundle.getAsBoolean("use_cust_instruct")) {
      dbBundle.add("instructions", dataBundle.getAsString("cust_instruct"));
    } else {
      boolean isEst   = dataBundle.getAsBoolean("estimation_task");
      boolean isBound = dataBundle.getAsBoolean("bound_exterior");

      String instruction = String.format("%s_%s_instruct",
          isEst? "est" : "prod", isBound? "bound" : "unbound");
      dbBundle.add("instructions", dbBundle.getAsString(instruction));
    }

    instructFile = dbBundle.getAsString("instructions");
    fontFile = dbBundle.getAsString("fonts");

    restNumber = dbBundle.getAsInt("rest_num");

    final int baseR = dbBundle.getAsInt("base_red");
    final int baseG = dbBundle.getAsInt("base_green");
    final int baseB = dbBundle.getAsInt("base_blue");

    final int dragR = dbBundle.getAsInt("drag_red");
    final int dragG = dbBundle.getAsInt("drag_green");
    final int dragB = dbBundle.getAsInt("drag_blue");

    final int handleR = dbBundle.getAsInt("handle_red");
    final int handleG = dbBundle.getAsInt("handle_green");
    final int handleB = dbBundle.getAsInt("handle_blue");
    /* END PARSING OF DATABASE FILE */

    Color baseColor = new Color(baseR, baseG, baseB);
    Color dragColor = new Color(dragR, dragG, dragB);
    Color handleActiveColor = new Color(handleR, handleG, handleB);

    frame = getFrame();
    setFullScreen();

    // Set up data data file
    URL dataFile = getDataFile();

    // Prepare the database file
    DecimalFormat df = new DecimalFormat("#.####");
    StringBuilder outString = new StringBuilder();

    boolean useMouse;

    try {
      useMouse = dbBundle.getAsBoolean("use_mouse");
    } catch (IndexOutOfBoundsException e) {
      System.out.println("useMouse click flag missing, defaulting to spacebar");
      useMouse = false;
    }

    dataAp.writeToUrl(getDataFile(), createOutputHeader());

    //----prepare frame---------
    Color imColor = Color.BLACK;
    final BlankPanel startPanel = new BlankPanel(imColor);
    imPanel = new BlankPanel(imColor);
    final BlankPanel leftMarginPanel = new BlankPanel(imColor);
    // BlankPanel rightMarginPanel = new BlankPanel(imColor);
    BlankPanel gridPanel = new BlankPanel(imColor);
    BlankPanel fixationPanel = new BlankPanel(imColor);
    //fixationPanel.setLayout(new GridLayout(3,1));
    fixationPanel.setLayout(new BorderLayout());

    imPanel.setLayout(new BorderLayout());
    gridPanel.setLayout(new GridLayout(3, 1));

    Dimension paneld;

    BlankPanel endPanel = new BlankPanel(Color.BLACK);
    frame.setContentPane(startPanel);
    frame.setVisible(true);

    String userResp;
    String userRespVal;

    //  get array of usable font names
    fonts = dataAp.readFromUrl(getFontFile());
    String[] fontNames = new String[fonts.length];

    for (int i = 0; i < fonts.length; i++) {
      fontNames[i] = fonts[i].getParsedStringSpec(1);
    }

    frame.hideCursor();
    response.testTimer(startPanel, Color.white, 1000);
    frame.showCursor();
    JPanel instructionPanel = new JPanel();

    if (dataBundle.getAsBoolean("use_cust_instruct")) {
      try {
        instructionPanel = getInstructionPanel(new URL("file://" + dbBundle.getAsString("instructions")));
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    } else {
       instructionPanel = getInstructionPanel(getInstructionFile());
    }

    frame.setContentPane(instructionPanel);
    frame.setVisible(true);

    try {
      synchronized (this) {
        wait();
      }
    } catch (InterruptedException ignored) { /* IGNORE */ }

    frame.remove(instructionPanel);
    frame.validate();

    String numberLineSize = dataBundle.getAsString("line_size_temp");

    int widthMod = getModifier("width", numberLineSize);

    for (; trialType < 2; trialType++) {
      int tempTrials;
      if (trialType == 0) {
        tempTrials = numOfPracTrials;
        prepareToStartPractice(frame);
      } else {
        tempTrials = numberOfTrials;
        frame.showCursor();
        prepareToStartExperiment(frame);

        frame.hideCursor();
      }

      frame.requestFocus();

      for (trialNum = 0; trialNum < tempTrials; trialNum++) {
        frame.showCursor();
        long reactTime;

        final int leftMarginLow = dataBundle.getAsInt("left_margin_low");
        final int leftMarginHigh = dataBundle.getAsInt("left_margin_high");
        final int leftMarginInterval = dataBundle.getAsInt("left_margin_interval");

        final int widthLow  = widthMod * dataBundle.getAsInt("width_low");
        final int widthHigh = widthMod * dataBundle.getAsInt("width_high")
                                       * dataBundle.getAsInt("end_unit");
        final int widthInterval = dataBundle.getAsInt("width_interval");

        final int height    = getModifier("height", numberLineSize);
        final int thickness = getModifier("thickness", numberLineSize);

        final Unit defaultStartUnit = new Unit(dataBundle.getAsString("start_unit"));
        final Unit defaultEndUnit = new Unit(dataBundle.getAsString("end_unit"));

        final Unit targetUnitLow = new Unit(dataBundle.getAsString("target_unit_low"));
        final Unit targetUnitHigh = new Unit(dataBundle.getAsString("target_unit_high"));
        final Unit targetUnitInterval = new Unit(dataBundle.getAsString("target_unit_interval"));

        final String startUnitFormat = dataBundle.getAsString("start_unit_format").toUpperCase();
        final String endUnitFormat = dataBundle.getAsString("end_unit_format").toUpperCase();
        final String targetUnitFormat = dataBundle.getAsString("target_unit_format").toUpperCase();

        int estimateTime = 0;
        if (isEstimationTask) {
          estimateTime = dataBundle.getAsInt("est_stim_time");
        }

        String estTargetFormat = dataBundle.getAsString("est_target_format");

        final String start = dataBundle.getAsString("start_label_on");
        final String end = dataBundle.getAsString("end_label_on");
        final String target = dataBundle.getAsString("target_label_on");
        final String handle = "false";

        boolean[] showFullBaseScale = new boolean[4];
        showFullBaseScale[0] = Boolean.parseBoolean(start);
        showFullBaseScale[1] = Boolean.parseBoolean(end);
        showFullBaseScale[2] = Boolean.parseBoolean(target);
        showFullBaseScale[3] = Boolean.parseBoolean(handle);

        // location 0 in the array determines if the handle can move past the left bound
        // location 1 determines the right bound
        boolean[] keepWithinBounds = new boolean[2];
        keepWithinBounds[0] = dataBundle.getAsBoolean("bound_interior");
        keepWithinBounds[1] = dataBundle.getAsBoolean("bound_exterior");

        //Update the leftMarginPanel in each trial
        int leftMargin = getRandomLeftMargin(leftMarginLow, leftMarginHigh, leftMarginInterval);

        paneld = imPanel.getSize();
        paneld.width = leftMargin;
        leftMarginPanel.setPreferredSize(paneld);

        frame.remove(startPanel);

        // unitSize = widthMod * random(width_low, width_high);
        // number of units = high - low
        final int startUnitInt   = dataBundle.getAsInt("start_unit");
        final int endUnitInt     = dataBundle.getAsInt("end_unit");

        final int low            = dataBundle.getAsInt("width_low");
        final int high           = dataBundle.getAsInt("width_high");

        final int units = endUnitInt - startUnitInt;

        int unitSize = widthMod * ((new Random()).nextInt(high - low) + low);

        int randWidth = units * unitSize;

        Unit randTarget = Unit.getRandomUnit(targetUnitLow, targetUnitHigh, targetUnitInterval);

        Unit startUnit = reduceUnit(startUnitFormat, defaultStartUnit);
        Unit endUnit = reduceUnit(endUnitFormat, defaultEndUnit);

        randTarget = reduceUnit(targetUnitFormat, randTarget);

        String myFontName = getRandomFontName(fontNames);

        char leftOrRight = dataBundle.getAsString("handle_start_point").charAt(0);

        numLine = new NumberLine(randWidth + thickness, height, thickness, startUnit, endUnit,
            randTarget, baseColor, dragColor, handleActiveColor, myFontName,
            isEstimationTask, leftMargin, keepWithinBounds, leftOrRight,
            showFullBaseScale);

        //Displays Fixation if necessary
        Fixation fixation = new Fixation(Color.BLACK, baseColor, thickness, numLine.getFixationLine());
        fixationPanel.removeAll();
        //fixationPanel.add(new JLabel()); //Top row of gridpanel is blank
        fixationPanel.add(fixation, BorderLayout.CENTER); //Middle row has the NumberLine
        //fixationPanel.add(new JLabel()); //Bottom row of gridPanel is blank

        if (isEstimationTask) {
          imPanel.add(fixationPanel, BorderLayout.CENTER);
          frame.setContentPane(imPanel);
          frame.validate();
          delay(250);
          imPanel.remove(fixationPanel);
        }

        //show numline and gather response
        presentTrial();

        if (!isEstimationTask) {
          frame.showCursor();

          //Idle here until user has hit the space bar
          reactTime = response.getTimedNumberLineResponse(numLine, useMouse);

          frame.remove(imPanel);
          frame.setContentPane(endPanel);
          userResp = df.format(numLine.getUnitLength());
          userRespVal = userResp;
        } else {
          if (estimateTime > 0) {
            final long startTime = new Date().getTime();

            delay(estimateTime);

            frame.remove(imPanel);
            frame.setContentPane(endPanel);
            frame.validate();

            reactTime = new Date().getTime() - startTime;
          } else {
            reactTime = response.getTimedNumberLineResponse(numLine, useMouse);

            frame.remove(imPanel); //remove the imPanel
            frame.setContentPane(endPanel);
            frame.validate();
          }

          frame.showCursor();
          frame.setContentPane(startPanel);
          frame.validate();
          //response.getTimedNumPadResponse(frame, "What is the target of this number line?",
          //    estTargetFormat);
          try {
            response.getTimedTextResponseJustified(frame, "",
                "What is the target of this number line?",
                30,"center", false);
          } catch (IOException e) {
            e.printStackTrace();
          }
          userResp = response.getTextValue();
          userRespVal = df.format(numLine.getUnitLength(userResp));
        }

        frame.hideCursor();
        frame.remove(startPanel);
        frame.setContentPane(endPanel);
        frame.validate();

        delay(1000);

        // Format as decimal output.
        outString.append(subject).append("\t");
        outString.append(trialType).append("\t");
        outString.append(trialNum).append("\t");
        outString.append(condition).append("\t");
        outString.append(session).append("\t");
        // TODO: Fix this.
        outString.append(numberLineSize).append("\t");
        outString.append(df.format(numLine.getUnitLength())).append("\t");
        outString.append(df.format(numLine.getStartUnit())).append("\t");
        outString.append(df.format(numLine.getEndUnit())).append("\t");
        outString.append(df.format(numLine.getTargetUnit())).append("\t");
        outString.append(keepWithinBounds[1] ? "Bounded" : "Unbounded").append("\t");
        outString.append(userRespVal).append("\t");
        outString.append(isEstimationTask ? "Estimation" : "Production").append("\t");
        outString.append(estimateTime).append("\t");
        outString.append(reactTime).append("\t");

        String outStringTmp = outString.toString().replaceAll("true", "TRUE");
        outStringTmp = outStringTmp.replaceAll("false", "FALSE");

        dataAp.writeToUrl(dataFile, outStringTmp);
        outString.setLength(0);

        frame.remove(endPanel);
        frame.validate();
        frame.setContentPane(endPanel);
        if ((trialNum + 1) % restNumber == 0) {
          rest();
        }
        imPanel.remove(leftMarginPanel);
        imPanel.remove(gridPanel);
        numLine = null;
        imPanel.removeAll();
      }
    }

    thankYou();
    System.exit(0);
  }

  private int getModifier(String prefix, String key) {
    switch (key.toUpperCase()) {
      case "SMALL":
        return dataBundle.getAsInt(prefix + "_small_mod");
      case "MEDIUM":
        return dataBundle.getAsInt(prefix + "_medium_mod");
      default:
        return dataBundle.getAsInt(prefix + "_large_mod");
    }
  }

  /**
   * Returns an instance of JPanel formatted to display the instruction file.
   *
   * @param u URL to instruction file
   * @return Formatted instructions on a JPanel
   */
  private JPanel getInstructionPanel(URL u) {
    JPanel j = new JPanel();
    j.setBackground(Color.WHITE);
    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension d = tk.getScreenSize();
    j.setSize(d.width, d.height);
    j.setLocation(0, 0);

    SpecificationArrayProcess sap = new SpecificationArrayProcess();
    Specification[] instructions = sap.readFromUrl(u);

    String html1;
    String html2;
    html1 = "<html><body style='width:";
    html2 = "px'><left>";

    StringBuilder inst = new StringBuilder(html1 + 1000 + html2);
    for (Specification instruction : instructions) {
      inst.append(instruction.getAllSpecs());
    }

    JLabel l = new JLabel();
    l.setForeground(Color.BLACK);
    l.setText(inst.toString());
    l.setVisible(true);

    GridBagLayout gb = new GridBagLayout();
    j.setLayout(gb);

    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;

    gb.setConstraints(l, c);
    j.add(l);

    JButton okButton = new JButton("OK");
    okButton.addActionListener(this);


    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;

    gb.setConstraints(okButton, c);

    j.add(okButton);

    return j;
  }

  /**
   * Creates the OutputHeader for the data file.
   *
   * @return String of column headers
   */
  public String createOutputHeader() {
    return "sn\t"
        + "pract\t"
        + "trial\t"
        + "cond\t"
        + "session\t"
        + "numberlineSize\t"
        + "unitWidth\t"
        + "startUnit\t"
        + "endUnit\t"
        + "target\t"
        + "Bounded\t"
        + "userRespValue\t"
        + "estTask\t"
        + "estStimTime\t"
        + "numLineRT\t";
  }

  /**
   * Randomly generates a value for the left margin based on parameters.
   *
   * @param low      Low value for the range
   * @param high     High value for the range
   * @param interval Interval
   * @return Random value
   */
  private int getRandomLeftMargin(int low, int high, int interval) {
    randGen.setIntervalRange(low, high, interval);
    return randGen.drawWithInterval();
  }

  /**
   * Present the experiment trial.
   */
  private void presentTrial() {
    //gridPanel.removeAll(); //Clears out all JComponents in panel before adding any new ones
    //gridPanel.validate();
    //gridPanel.add(new JLabel()); //Top row of gridpanel is blank
    //gridPanel.add(numLine.getPanel()); //Middle row has the NumberLine
    //gridPanel.add(new JLabel()); //Bottom row of gridPanel is blank
    //gridPanel.validate();
    imPanel.add(numLine.getPanel(), BorderLayout.CENTER);
    frame.setContentPane(imPanel);
    frame.validate();
  }

  /**
   * Converts a Unit object into its reduced form.
   *
   * @param unitFormat Format for the unit
   * @param unit       Unit value
   * @return New unit object in the reduced form
   */
  private Unit reduceUnit(String unitFormat, Unit unit) {
    Unit reducedUnit = unit;
    if (unitFormat.matches("^.*[Ll][Cc][Dd]$")) {
      String[] fractStr = new String[2];
      Fraction fract;
      if (unit.getType() == Unit.UnitType.FRACT) {
        fractStr = unit.getValue().split("/");
      } else if (unit.getType() == Unit.UnitType.ODDS) {
        fractStr = unit.getValue().split("in");
      }
      fract = new Fraction(Integer.parseInt(fractStr[0].trim()),
          Integer.parseInt(fractStr[1].trim()));

      String reducedFractStr = Fraction.reduceFract(fract).toString();
      if (unit.getType() == Unit.UnitType.FRACT) {
        reducedUnit = new Unit(reducedFractStr);
      } else if (unit.getType() == Unit.UnitType.ODDS) {
        fractStr = reducedFractStr.split("/");
        reducedUnit = new Unit(fractStr[0].trim() + " in " + fractStr[1].trim());
      }
    }
    return reducedUnit;
  }

  /**
   * Implemented method from ActionListener.
   * Whenever an action is performed, display a blank screen and notify the
   * experiment of the action
   *
   * @param e ActionEvent object used to get information about the action
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    presentBlankScreen(0);
    synchronized (this) {
      notify();
    }
  }
}
