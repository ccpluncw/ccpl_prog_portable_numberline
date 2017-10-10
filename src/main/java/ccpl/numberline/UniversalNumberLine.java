package ccpl.numberline;

import static ccpl.numberline.DatabaseFileReaderKt.readDbFile;

import ccpl.lib.BlankPanel;
import ccpl.lib.DrawExpFrame;
import ccpl.lib.Experiment;
import ccpl.lib.Fraction;
import ccpl.lib.NumberLine;
import ccpl.lib.RandomIntGenerator;
import ccpl.lib.Response;
import ccpl.lib.Specification;
import ccpl.lib.SpecificationArrayProcess;
import ccpl.lib.Unit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URL;

import java.text.DecimalFormat;

import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Number line experiment that displays a number line and asks the user for feedback.
 * Modified by Kyle Holt.
 * November, 2010.
 * @author dalecohen
 */
public class UniversalNumberLine extends Experiment implements ActionListener {

  private boolean isEstimationTask;
  private boolean isBounded;

  private int leftBound;
  private int rightBound;

  private int targetLow;
  private int targetHigh;

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

  private static NumberLine numLine = null;
  private static RandomIntGenerator randGen = new RandomIntGenerator();
  private static BlankPanel imPanel;
  private static BlankPanel gridPanel;
  private static DrawExpFrame frame;

  /**
   * Creates and displays a reminder message about the correct key presses.
   *
   * @param leftValue  Value of the left key press
   * @param rightValue Value of the right key press
   * @param resp       Response object that will display the notification
   */
  private void reminderMessage(String leftValue, String rightValue, Response resp) {
    // Message that will be displayed.
    String message = "Please remember: The left click indicates '" + leftValue
        + "'. The right click indicates '" + rightValue + "'";
    //JOptionPane optionPane = new JOptionPane(new JLabel(message), JLabel.CENTER);
    //JDialog d = optionPane.createDialog("Reminder");
    //d.setVisible(true);

    // Display the notification with the above message string
    resp.displayNotificationFrame(frame, message);
    // JOptionPane.showMessageDialog(frame, message, "Reminder", JOptionPane.INFORMATION_MESSAGE);
  }

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
                             boolean isBounded, boolean isEstimation, int targetLow,
                             int targetHigh, int leftBound, int rightBound) {
    super(expFile, sub, cond, sess);

    trialType = 0;
    trialNum = 0;

    this.isEstimationTask = isEstimation;
    this.isBounded = isBounded;

    this.targetLow = targetLow;
    this.targetHigh = targetHigh;

    this.leftBound = leftBound;
    this.rightBound = rightBound;
  }

  /**
   * Runs the UniversalNumberLine experiment with the specified database file.
   */
  public void run() {
    ClassLoader cl = this.getClass().getClassLoader();
    URL dbFilePath = cl.getResource(experiment + "/infiles/dbfile_ut_0_1.txt");
    URL newLayoutPath = cl.getResource(experiment + "/infiles/dbfile_new_layout.txt");

    // Read DB
    Bundle dbBundle = readDbFile(newLayoutPath);

    // Read in the database file
    dbfile = dataAP.readFromURL(dbFilePath);

    // BEGIN PARSING OF DATABASE FILE
    practiceTrialFile = dbBundle.getAsString("prac_trial");

    if (practiceTrialFile.equalsIgnoreCase("none")) {
      trialType = 1;
    } else {
      trialType = 0;
    }

    expTrialFile = dbBundle.getAsString("exp_trial");
    instructFile = dbBundle.getAsString("instructions");
    fontFile = dbBundle.getAsString("fonts");

    restNumber = dbBundle.getAsInt("rest_num");

    final int baseR = dbBundle.getAsInt("base_red");
    final int baseG = dbBundle.getAsInt("base_green");
    final int baseB = dbBundle.getAsInt("base_blue");

    final int dragR = dbfile[6].getParsedIntSpec(1);
    final int dragG = dbfile[6].getParsedIntSpec(2);
    final int dragB = dbfile[6].getParsedIntSpec(3);

    final int handleR = dbfile[7].getParsedIntSpec(1);
    final int handleG = dbfile[7].getParsedIntSpec(2);
    final int handleB = dbfile[7].getParsedIntSpec(3);
    /* END PARSING OF DATABASE FILE */

    Color baseColor = new Color(baseR, baseG, baseB);
    Color dragColor = new Color(dragR, dragG, dragB);
    Color handleActiveColor = new Color(handleR, handleG, handleB);

    String baseColorVal = "(" + baseColor.getRed() + ", "
        + baseColor.getGreen() + ", "
        + baseColor.getBlue() + ")";

    String dragColorValue = "(" + dragColor.getRed() + ", "
        + dragColor.getGreen() + ", "
        + dragColor.getBlue() + ")";

    String handleColorVal = "(" + handleActiveColor.getRed() + ", "
        + handleActiveColor.getGreen() + ", "
        + handleActiveColor.getBlue() + ")";

    frame = getFrame();
    setFullScreen();

    // Set up data data file
    URL dataFile = getDataFile();

    // Prepare the database file
    DecimalFormat df = new DecimalFormat("#.####");
    StringBuilder outString = new StringBuilder();

    boolean useMouse;

    try {
      useMouse = dbfile[15].getParsedStringSpec(1).trim().equalsIgnoreCase("on");
    } catch (IndexOutOfBoundsException e) {
      System.out.println("useMouse click flag missing, defaulting to spacebar");
      useMouse = false;
    }

    dataAP.writeToURL(getCGI(), dataFile, createOutputHeader());

    //----prepare frame---------
    Color imColor = Color.BLACK;
    final BlankPanel startPanel = new BlankPanel(imColor);
    imPanel = new BlankPanel(imColor);
    final BlankPanel leftMarginPanel = new BlankPanel(imColor);
    // BlankPanel rightMarginPanel = new BlankPanel(imColor);
    gridPanel = new BlankPanel(imColor);
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
    fonts = dataAP.readFromURL(getFontFile());
    String[] fontNames = new String[fonts.length];

    for (int i = 0; i < fonts.length; i++) {
      fontNames[i] = fonts[i].getParsedStringSpec(1);
    }

    frame.hideCursor();
    response.testTimer(startPanel, Color.white, 1000);
    frame.showCursor();

    JPanel instructionPanel = getInstructionPanel(getInstructionFile());
    frame.setContentPane(instructionPanel);
    frame.setVisible(true);

    try {
      synchronized (this) {
        wait();
      }
    } catch (InterruptedException ignored) { }

    frame.remove(instructionPanel);
    frame.validate();

    for (; trialType < 2; trialType++) {
      if (trialType == 0) {
        stims = dataAP.readFromURL(getPracticeFile());
        stims = dataAP.randomize(stims);
        prepareToStartPractice(frame);
      } else {
        frame.showCursor();

        stims = dataAP.readFromURL(getExperimentFile());
        stims = dataAP.randomize(stims);
        frame.showCursor();
        prepareToStartExperiment(frame);

        frame.hideCursor();
      }

      final int trialLength = stims.length;
      totalTrials = stims.length;
      frame.requestFocus();

      for (trialNum = 0; trialNum < trialLength; trialNum++) {
        frame.showCursor();
        long reactTime;
        long textRt = 0;

        final int leftMarginLow = stims[trialNum].getParsedIntSpec(1);
        final int leftMarginHigh = stims[trialNum].getParsedIntSpec(2);
        final int leftMarginInterval = stims[trialNum].getParsedIntSpec(3);
        final int widthLow = stims[trialNum].getParsedIntSpec(4);
        final int widthHigh = stims[trialNum].getParsedIntSpec(5);
        final int widthInterval = stims[trialNum].getParsedIntSpec(6);
        final int height = stims[trialNum].getParsedIntSpec(7);
        final int thickness = stims[trialNum].getParsedIntSpec(8);

        final Unit defaultStartUnit = new Unit(stims[trialNum].getParsedStringSpec(9));
        final Unit defaultEndUnit = new Unit(stims[trialNum].getParsedStringSpec(10));

        final Unit targetUnitLow = new Unit(String.valueOf(targetLow));
        final Unit targetUnitHigh = new Unit(String.valueOf(targetHigh));
        final Unit targetUnitInterval = new Unit(stims[trialNum].getParsedStringSpec(13));

        String startUnitFormat = stims[trialNum].getParsedStringSpec(14);
        startUnitFormat = startUnitFormat.toUpperCase();
        String endUnitFormat = stims[trialNum].getParsedStringSpec(15);
        endUnitFormat = endUnitFormat.toUpperCase();
        String targetUnitFormat = stims[trialNum].getParsedStringSpec(16);
        targetUnitFormat = targetUnitFormat.toUpperCase();

        boolean isEstimateTask = isEstimationTask;

        int estimateTime = 0;
        if (isEstimateTask) {
          estimateTime = stims[trialNum].getParsedIntSpec(18);
        }

        String estTargetFormat = stims[trialNum].getParsedStringSpec(19);

        final String start = stims[trialNum].getParsedStringSpec(20);
        final String end = stims[trialNum].getParsedStringSpec(21);
        final String target = stims[trialNum].getParsedStringSpec(22);
        final String handle = stims[trialNum].getParsedStringSpec(23);

        boolean[]  showFullBaseScale = new boolean[4];
        showFullBaseScale[0] = Boolean.parseBoolean(start);
        showFullBaseScale[1] = Boolean.parseBoolean(end);
        showFullBaseScale[2] = Boolean.parseBoolean(target);
        showFullBaseScale[3] = Boolean.parseBoolean(handle);

        // location 0 in the array determines if the handle can move past the left bound
        // location 1 determines the right bound
        boolean[] keepWithinBounds = new boolean[2];
        keepWithinBounds[0] = isBounded;
        keepWithinBounds[1] = isBounded;

        //Update the leftMarginPanel in each trial
        int leftMargin = getRandomLeftMargin(leftMarginLow, leftMarginHigh, leftMarginInterval);

        paneld = imPanel.getSize();
        paneld.width = leftMargin;
        leftMarginPanel.setPreferredSize(paneld);

        //imPanel.add(leftMarginPanel, BorderLayout.WEST);

        frame.remove(startPanel);
        //frame.hideCursor();

        randGen = new RandomIntGenerator(widthLow, widthHigh, widthInterval);
        int randWidth = randGen.drawWithInterval();

        Unit randTarget = Unit.getRandomUnit(targetUnitLow, targetUnitHigh, targetUnitInterval);


        Unit startUnit = reduceUnit(startUnitFormat, defaultStartUnit);
        Unit endUnit = reduceUnit(endUnitFormat, defaultEndUnit);

        randTarget = reduceUnit(targetUnitFormat, randTarget);

        String handleLabel = "";

        String myFontName = getRandomFontName(fontNames);

        int degrees = stims[trialNum].getParsedIntSpec(27);

        char leftOrRight = stims[trialNum].getParsedCharSpec(24);

        numLine = new NumberLine(randWidth, height, thickness, startUnit, endUnit,
            randTarget, baseColor, dragColor, handleActiveColor, myFontName,
            isEstimateTask, 180, leftMargin, keepWithinBounds, leftOrRight,
            showFullBaseScale, handleLabel);

        //show numline and gather response
        presentTrial();

        if (!isEstimateTask) {
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
          response.getTimedNumPadResponse(frame, "What is the target of this number line?",
              estTargetFormat);
          textRt = response.getTextRt();
          userResp = response.getTextValue();
          userRespVal = df.format(numLine.getUnitLength(userResp));
        }

        frame.hideCursor();
        frame.remove(startPanel);
        frame.setContentPane(endPanel);
        frame.validate();

        delay(1000);

        double numLineUnitErr;
        if (!isEstimateTask) {
          numLineUnitErr = numLine.getUnitError(true);
        } else {
          numLineUnitErr = numLine.getUnitError(true, userResp);
        }

        outString.append(experiment).append("\t");
        outString.append(subject).append("\t");
        outString.append(trialType).append("\t");
        outString.append(trialNum).append("\t");
        outString.append(condition).append("\t");
        outString.append(session).append("\t");
        outString.append(leftMargin).append("\t");
        outString.append(numLine.getBaseWidth()).append("\t");
        outString.append(height).append("\t");
        outString.append(thickness).append("\t");
        outString.append(numLine.getStartUnitString()).append("\t");
        outString.append(df.format(numLine.getStartUnit())).append("\t");
        outString.append(startUnitFormat).append("\t");
        outString.append(numLine.getEndUnitString()).append("\t");
        outString.append(df.format(numLine.getEndUnit())).append("\t");
        outString.append(endUnitFormat).append("\t");
        outString.append(numLine.getTargetUnitString()).append("\t");
        outString.append(df.format(numLine.getTargetUnit())).append("\t");
        outString.append(targetUnitFormat).append("\t");
        outString.append(handleLabel);
        outString.append("\t");
        outString.append("\t");
        outString.append(baseColorVal).append("\t");
        outString.append(dragColorValue).append("\t");
        outString.append(handleColorVal).append("\t");
        outString.append(myFontName).append("\t");
        outString.append(showFullBaseScale[0]).append("\t");
        outString.append(showFullBaseScale[1]).append("\t");
        outString.append(showFullBaseScale[2]).append("\t");
        outString.append(leftOrRight).append("\t");
        outString.append(keepWithinBounds[0]).append("\t");
        outString.append(keepWithinBounds[1]).append("\t");
        outString.append(degrees).append("\t");
        outString.append(userResp).append("\t");
        outString.append(userRespVal).append("\t");
        outString.append(df.format(numLineUnitErr)).append("\t");
        outString.append(isEstimateTask).append("\t");
        outString.append(estimateTime).append("\t");
        outString.append(reactTime).append("\t");
        outString.append(textRt).append("\t");

        String outStringTmp = outString.toString().replaceAll("true", "TRUE");
        outStringTmp = outStringTmp.replaceAll("false", "FALSE");

        dataAP.writeToURL(getCGI(), dataFile, outStringTmp);
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
    Specification[] instructions = sap.readFromURL(u);

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
   * @return String of column headers
   */
  public String createOutputHeader() {
    StringBuilder sbuf = new StringBuilder();
    sbuf.append("exp\t");
    sbuf.append("sn\t");
    sbuf.append("pract\t");
    sbuf.append("trial\t");
    sbuf.append("cond\t");
    sbuf.append("session\t");
    sbuf.append("margin\t");
    sbuf.append("width\t");
    sbuf.append("height\t");
    sbuf.append("thick\t");
    sbuf.append("sUnit\t");
    sbuf.append("sUnitValue\t");
    sbuf.append("sUnitFormat\t");
    sbuf.append("bUnit\t");
    sbuf.append("bUnitValue\t");
    sbuf.append("bUnitFormat\t");
    sbuf.append("tUnit\t");
    sbuf.append("tUnitValue\t");
    sbuf.append("tUnitFormat\t");
    sbuf.append("handleLabel\t");
    sbuf.append("unitLabel\t");
    sbuf.append("bColor\t");
    sbuf.append("dColor\t");
    sbuf.append("hColor\t");
    sbuf.append("font\t");
    sbuf.append("dispStart\t");
    sbuf.append("dispEnd\t");
    sbuf.append("dispTarget\t");
    sbuf.append("handleStart\t");
    sbuf.append("leftBound\t");
    sbuf.append("rightBound\t");
    sbuf.append("degrees\t");
    sbuf.append("userResp\t");
    sbuf.append("userRespValue\t");
    sbuf.append("unitErr\t");
    sbuf.append("estTask\t");
    sbuf.append("estStimTime\t");
    sbuf.append("numLineRT\t");
    sbuf.append("textRt\t");
    sbuf.append("feedbackType\t");

    return sbuf.toString();
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
      if (unit.getType() == Unit.UNITTYPE.FRACT) {
        fractStr = unit.getValue().split("/");
      } else if (unit.getType() == Unit.UNITTYPE.ODDS) {
        fractStr = unit.getValue().split("in");
      }
      fract = new Fraction(Integer.parseInt(fractStr[0].trim()),
          Integer.parseInt(fractStr[1].trim()));

      String reducedFractStr = Fraction.reduceFract(fract).toString();
      if (unit.getType() == Unit.UNITTYPE.FRACT) {
        reducedUnit = new Unit(reducedFractStr);
      } else if (unit.getType() == Unit.UNITTYPE.ODDS) {
        fractStr = reducedFractStr.split("/");
        reducedUnit = new Unit(fractStr[0].trim() + " in " + fractStr[1].trim());
      }
    }
    return reducedUnit;
  }
}
