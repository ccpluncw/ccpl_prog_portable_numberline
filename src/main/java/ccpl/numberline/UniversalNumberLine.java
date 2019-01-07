/*
 * This file is part of the Cohen Ray Number Line.
 *
 * Latesco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Latesco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Latesco.  If not, see <http://www.gnu.org/licenses/>.
 */

package ccpl.numberline;

import static ccpl.lib.util.DatabaseFileReader.readDbFile;

import ccpl.lib.BlankPanel;
import ccpl.lib.Bundle;
import ccpl.lib.DrawExpFrame;
import ccpl.lib.Experiment;
import ccpl.lib.Fixation;
import ccpl.lib.Fraction;
import ccpl.lib.Mask;
import ccpl.lib.RandomIntGenerator;
import ccpl.lib.Specification;
import ccpl.lib.SpecificationArrayProcess;
import ccpl.lib.Unit;
import ccpl.lib.numberline.NumberLine;
import ccpl.lib.numberline.abs.AbstractHandleNumberLine;
import ccpl.numberline.config.Keys;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Number line experiment that displays a number line and asks the user for feedback. Modified by
 * Kyle Holt. November, 2010.
 *
 * @author dalecohen
 */
public class UniversalNumberLine extends Experiment implements ActionListener {

  private final boolean isEstimationTask;

  private Bundle dataBundle;

  private final int numberOfTrials;
  private final int numOfPracTrials;

  private static AbstractHandleNumberLine numLine = null;
  private static RandomIntGenerator randGen = new RandomIntGenerator();
  private BlankPanel imPanel;
  private DrawExpFrame frame;

  private long estStimTime;

  private Mask lineMask;

  private String subjAge;
  private String subjGrade;

  private String numberLineType;

  /**
   * Parameterized constructor allow the specification of an experiment file, the subject ID,
   * condition, and session number.
   *
   * @param expFile Experiment file
   * @param sub     Subject ID
   * @param cond    Condition
   * @param sess    Session number
   */
  public UniversalNumberLine(
      String expFile, String sub, String cond, String sess, Bundle dataBundle) {
    super(expFile, sub, cond, sess, dataBundle.getAsString(Keys.SAVE_DIR));

    this.dataBundle = dataBundle;

    trialType = 0;
    trialNum = 0;

    this.numberOfTrials = dataBundle.getAsInt(Keys.NUM_TRIALS);
    this.numOfPracTrials = dataBundle.getAsInt(Keys.NUM_PRAC_TRIALS);

    this.isEstimationTask = dataBundle.getAsBoolean(Keys.EST_TASK);

    final String subjAgeVal = dataBundle.getAsString(Keys.SUBJ_AGE);
    final String subjGradeVal = dataBundle.getAsString(Keys.SUBJ_GRADE);

    this.subjAge = subjAgeVal.equalsIgnoreCase("0") ? "NA" : subjAgeVal;
    this.subjGrade = subjGradeVal.equalsIgnoreCase("0") ? "NA" : subjGradeVal;
  }

  /**
   * Runs the UniversalNumberLine experiment with the specified database file.
   */
  public void run() {
    ClassLoader cl = this.getClass().getClassLoader();
    URL newLayoutPath = cl.getResource(experiment + "/infiles/base_db.txt");
    URL baseExpPath = cl.getResource(experiment + "/infiles/base_exp.txt");

    // Read DB
    assert newLayoutPath != null;
    final Bundle dbBundle = readDbFile(newLayoutPath);

    assert baseExpPath != null;
    Bundle baseExp = readDbFile(baseExpPath);

    dataBundle = dataBundle.merge(baseExp);

    // BEGIN PARSING OF DATABASE FILE
    trialType = numOfPracTrials != 0 ? 0 : 1;

    boolean isEst = dataBundle.getAsBoolean(Keys.EST_TASK);
    String bounded = dataBundle.getAsString(Keys.BOUND_EXTERIOR);

    switch (bounded) {
      case "false":
        numberLineType = "unbound";
        break;
      case "FALSE":
        numberLineType = "universal";
        break;
      default:
        numberLineType = "bound";
        break;
    }

    if (dataBundle.getAsBoolean(Keys.USE_CUST_INSTRUCTIONS)) {
      dbBundle.add(Keys.INSTRUCTIONS, dataBundle.getAsString(Keys.CUST_INSTRUCTIONS));
    } else {

      String instruction = String.format("%s_%s_instruct", isEst ? "est" : "prod", numberLineType);
      dbBundle.add(Keys.INSTRUCTIONS, dbBundle.getAsString(instruction));
    }

    instructFile = dbBundle.getAsString(Keys.INSTRUCTIONS);
    fontFile = dbBundle.getAsString(Keys.FONTS);

    restNumber = dbBundle.getAsInt(Keys.REST_NUM);

    final int baseR = dbBundle.getAsInt(Keys.BASE_RED);
    final int baseG = dbBundle.getAsInt(Keys.BASE_GREEN);
    final int baseB = dbBundle.getAsInt(Keys.BASE_BLUE);

    final int dragR = dbBundle.getAsInt(Keys.DRAG_RED);
    final int dragG = dbBundle.getAsInt(Keys.DRAG_GREEN);
    final int dragB = dbBundle.getAsInt(Keys.DRAG_BLUE);

    /* END PARSING OF DATABASE FILE */

    Color baseColor = new Color(baseR, baseG, baseB);
    Color dragColor = new Color(dragR, dragG, dragB);
    Color handleActiveColor = loadColor(Keys.HANDLE_ACTIVE, dbBundle).orElse(Color.RED);

    frame = getFrame();
    setFullScreen();

    // Set up data data file
    URL dataFile = getDataFile();

    // Prepare the database file
    DecimalFormat df = new DecimalFormat("#.####");
    StringBuilder outString = new StringBuilder();

    boolean useMouse;

    try {
      useMouse = dbBundle.getAsBoolean(Keys.USE_MOUSE);
    } catch (IndexOutOfBoundsException e) {
      System.out.println("useMouse click flag missing, defaulting to spacebar");
      useMouse = false;
    }

    if (isEstimationTask) {
      estStimTime = dataBundle.getAsInt(Keys.EST_STIM_TIME);
    }

    dataAp.writeToUrl(getDataFile(), createOutputHeader());

    // ----prepare frame---------
    Color imColor = Color.BLACK;
    final BlankPanel startPanel = new BlankPanel(imColor);
    imPanel = new BlankPanel(imColor);
    final BlankPanel leftMarginPanel = new BlankPanel(imColor);
    // BlankPanel rightMarginPanel = new BlankPanel(imColor);
    BlankPanel gridPanel = new BlankPanel(imColor);
    BlankPanel fixationPanel = new BlankPanel(imColor);
    // fixationPanel.setLayout(new GridLayout(3,1));
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

    if (dataBundle.getAsBoolean(Keys.USE_CUST_INSTRUCTIONS)) {
      try {
        instructionPanel =
            getInstructionPanel(new URL("file://" + dbBundle.getAsString(Keys.INSTRUCTIONS)));
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
    } catch (InterruptedException ignored) {
      /* IGNORE */
    }

    frame.remove(instructionPanel);
    frame.validate();

    String numberLineSize = dataBundle.getAsString(Keys.LINE_SIZE_TEMP);

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
        frame.hideCursor();
        long reactTime;

        final int leftMarginLow = dataBundle.getAsInt(Keys.LEFT_MARGIN_LOW);
        final int leftMarginHigh = dataBundle.getAsInt(Keys.LEFT_MARGIN_HIGH);
        final int leftMarginInterval = dataBundle.getAsInt(Keys.LEFT_MARGIN_INTERVAL);

        final int height = getModifier("height", numberLineSize);
        final int thickness = getModifier("thickness", numberLineSize);

        final Unit defaultStartUnit = new Unit(dataBundle.getAsString(Keys.START_UNIT));
        final Unit defaultEndUnit = new Unit(dataBundle.getAsString(Keys.END_UNIT));

        final Unit targetUnitLow = new Unit(dataBundle.getAsString(Keys.TARGET_UNIT_LOW));
        final Unit targetUnitHigh = new Unit(dataBundle.getAsString(Keys.TARGET_UNIT_HIGH));
        final Unit targetUnitInterval = new Unit(dataBundle.getAsString(Keys.TARGET_UNIT_INTERVAL));

        final boolean excludeLeft = !dataBundle.getAsBoolean(Keys.INCLUDE_LEFT_BND);
        final boolean excludeRight = !dataBundle.getAsBoolean(Keys.INCLUDE_RIGHT_BND);

        final String startUnitFormat = dataBundle.getAsString(Keys.START_UNIT_FORMAT).toUpperCase();
        final String endUnitFormat = dataBundle.getAsString(Keys.END_UNIT_FORMAT).toUpperCase();
        final String targUnitFormat = dataBundle.getAsString(Keys.TARGET_UNIT_FORMAT).toUpperCase();

        String estTargetFormat = dataBundle.getAsString(Keys.EST_TARGET_FORMAT);

        final String start = dataBundle.getAsString(Keys.START_LABEL_ON);
        final String end = dataBundle.getAsString(Keys.END_LABEL_ON);
        final String target = dataBundle.getAsString(Keys.TARGET_LABEL_ON);
        final String handle = "false";

        boolean[] showFullBaseScale = new boolean[4];
        showFullBaseScale[0] = Boolean.parseBoolean(start);
        showFullBaseScale[1] = Boolean.parseBoolean(end);
        showFullBaseScale[2] = Boolean.parseBoolean(target);
        showFullBaseScale[3] = Boolean.parseBoolean(handle);

        // location 0 in the array determines if the handle can move past the left bound
        // location 1 determines the right bound
        boolean[] keepWithinBounds = new boolean[2];
        keepWithinBounds[0] = dataBundle.getAsBoolean(Keys.BOUND_INTERIOR);
        keepWithinBounds[1] = dataBundle.getAsBoolean(Keys.BOUND_EXTERIOR);

        // Update the leftMarginPanel in each trial
        int leftMargin = getRandomLeftMargin(leftMarginLow, leftMarginHigh, leftMarginInterval);

        paneld = imPanel.getSize();
        paneld.width = leftMargin;
        leftMarginPanel.setPreferredSize(paneld);

        frame.remove(startPanel);

        // unitSize = widthMod * random(width_low, width_high);
        // number of units = high - low
        final int startUnitInt = dataBundle.getAsInt(Keys.START_UNIT);
        final int endUnitInt = dataBundle.getAsInt(Keys.END_UNIT);

        final int low = dataBundle.getAsInt(Keys.WIDTH_LOW);
        final int high = dataBundle.getAsInt(Keys.WIDTH_HIGH);

        final int units = endUnitInt - startUnitInt;

        int unitSize = widthMod * ((new Random()).nextInt(high - low) + low);

        int randWidth = units * unitSize;

        Unit randTarget;
        Unit leftBnd = new Unit(String.valueOf(startUnitInt));
        Unit rightBnd = new Unit(String.valueOf(endUnitInt));

        if (excludeLeft || excludeRight) {
          do {
            randTarget = Unit.getRandomUnit(targetUnitLow, targetUnitHigh, targetUnitInterval);
          } while ((excludeLeft && randTarget.equals(leftBnd)
              || (excludeRight && randTarget.equals(rightBnd))));
        } else {
          randTarget = Unit.getRandomUnit(targetUnitLow, targetUnitHigh, targetUnitInterval);
        }

        Unit startUnit = reduceUnit(startUnitFormat, defaultStartUnit);
        Unit endUnit = reduceUnit(endUnitFormat, defaultEndUnit);

        randTarget = reduceUnit(targUnitFormat, randTarget);

        String myFontName = getRandomFontName(fontNames);

        char leftOrRight = dataBundle.getAsString(Keys.HANDLE_START_POINT).charAt(0);

        numLine =
            new NumberLine(
                randWidth,
                height,
                thickness,
                startUnit,
                endUnit,
                randTarget,
                baseColor,
                dragColor,
                handleActiveColor,
                myFontName,
                isEstimationTask,
                leftMargin,
                keepWithinBounds,
                leftOrRight,
                showFullBaseScale,
                unitSize);

        // Load colors for the various parts of the number line.
        loadColor("left_bnd", dbBundle).ifPresent(numLine::setLeftBoundColor);
        loadColor("right_bnd", dbBundle).ifPresent(numLine::setRightBoundColor);
        loadColor("drag_active", dbBundle).ifPresent(numLine::setDragActiveColor);

        loadColor(Keys.HANDLE_INACTIVE, dbBundle).ifPresent(color -> {
          numLine.setLeftDragHandleColor(color);
          numLine.setRightDragHandleColor(color);
        });

        loadColor(Keys.HANDLE_ACTIVE, dbBundle).ifPresent(color -> {
          numLine.setLeftDragActiveColor(color);
          numLine.setRightDragActiveColor(color);
        });

        // Disable the left handle in the unbounded condition.
        if (dataBundle.getAsString(Keys.BOUND_EXTERIOR).equals("false")) {
          numLine.disableLeftHandle();
        }

        // Displays Fixation if necessary
        Fixation fixation =
            new Fixation(Color.BLACK, baseColor, thickness, numLine.getFixationLine());
        fixationPanel.removeAll();
        // fixationPanel.add(new JLabel()); //Top row of gridpanel is blank
        fixationPanel.add(fixation, BorderLayout.CENTER); // Middle row has the NumberLine
        // fixationPanel.add(new JLabel()); //Bottom row of gridPanel is blank

        if (isEstimationTask) {
          imPanel.add(fixationPanel, BorderLayout.CENTER);
          frame.setContentPane(imPanel);
          frame.validate();
          delay(500);
          imPanel.remove(fixationPanel);
        }

        // show numline and gather response
        presentTrial();

        if (!isEstimationTask) {
          frame.showCursor();

          // Idle here until user has hit the space bar
          reactTime = response.getTimedNumberLineResponse(numLine, useMouse);

          frame.remove(imPanel);
          frame.setContentPane(endPanel);
          userResp = df.format(numLine.getUserResponse());
          userRespVal = userResp;
        } else {
          if (estStimTime > 0) {
            final long startTime = new Date().getTime();

            delay((int) estStimTime);

            frame.remove(imPanel);
            frame.setContentPane(endPanel);
            frame.validate();

            reactTime = new Date().getTime() - startTime;
          } else {
            reactTime = response.getTimedNumberLineResponse(numLine, useMouse);

            frame.remove(imPanel); // remove the imPanel
            frame.setContentPane(endPanel);
            frame.validate();

            // The mask is an unstable feature within the portable number line.
            // The delay is not always a second.
            if (FeatureSwitch.USE_MASK) {
              lineMask = new Mask(thickness, Color.BLACK, new Color[] {baseColor});

              frame.setContentPane(lineMask);
              frame.validate();

              delay(1000);

              frame.remove(lineMask);
              frame.setContentPane(endPanel);
              frame.validate();
            }
          }

          frame.showCursor();
          frame.setContentPane(startPanel);
          frame.validate();
          // response.getTimedNumPadResponse(frame, "What is the target of this number line?",
          //    estTargetFormat);
          try {
            response.getTimedTextResponseJustified(
                frame, "", "What is the target of this number line?", 30, "center", false);
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
        numberLineType = numberLineType.substring(0, 1).toUpperCase() + numberLineType.substring(1);
        outString.append(subject).append("\t");
        outString.append(subjAge).append("\t");
        outString.append(subjGrade).append("\t");
        outString.append(trialType).append("\t");
        outString.append(trialNum).append("\t");
        outString.append(condition).append("\t");
        outString.append(session).append("\t");
        outString.append(numberLineSize).append("\t");
        outString.append(df.format(numLine.getUnitLength())).append("\t");
        outString.append(df.format(startUnit.toDouble())).append("\t");
        outString.append(df.format(endUnit.toDouble())).append("\t");
        outString.append(df.format(randTarget.toDouble())).append("\t");
        outString.append(numberLineType).append("\t");
        outString.append(userRespVal).append("\t");
        outString.append(isEstimationTask ? "Estimation" : "Production").append("\t");
        outString.append(reactTime).append("\t");

        if (estStimTime > 0) {
          outString.append(estStimTime).append("\t");
        }

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
    frame.dispose();
  }

  private Optional<Color> loadColor(String key, Bundle bun) {
    final int red;
    final int green;
    final int blue;

    try {
      red = bun.getAsInt(String.format("%s_red", key));
      green = bun.getAsInt(String.format("%s_green", key));
      blue = bun.getAsInt(String.format("%s_blue", key));
    } catch (Exception e) {
      return Optional.empty();
    }

    return Optional.of(new Color(red, green, blue));
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

    GridBagLayout gb = new GridBagLayout();
    j.setLayout(gb);

    SpecificationArrayProcess sap = new SpecificationArrayProcess();
    Specification[] instructions = sap.readFromUrl(u);

    String htmlHeader = "<html><body style='width:1000px'><left>";

    StringBuilder instructBuilder = new StringBuilder();
    for (Specification instruction : instructions) {
      instructBuilder.append(instruction.getAllSpecs());
    }

    String imageTagPattern = "((?<=(<img[^>]{1,1000}>)|(?=(<img[^>]{1,1000}>))))";
    String[] split = instructBuilder.toString().split(imageTagPattern);
    List<String> elems = new ArrayList<>(Arrays.asList(split));

    Pattern srcPattern = Pattern.compile("src=\".*\"");

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBackground(Color.WHITE);
    for (String entry : elems) {
      JComponent comp;

      if (entry.contains("<img")) {
        Matcher srcMatcher = srcPattern.matcher(entry);

        if (!srcMatcher.find()) {
          continue;
        }

        String dirtySrc = srcMatcher.group();
        String cleanStr = dirtySrc.replaceAll("src=", "").replaceAll("\"", "");

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL srcUrl = cl.getResource(cleanStr);

        if (srcUrl == null) {
          srcUrl = getUrl(cleanStr);
        }

        if (srcUrl == null) {
          continue;
        }

        try {
          BufferedImage image = ImageIO.read(srcUrl);
          comp = new JLabel(new ImageIcon(image));
          comp.setAlignmentX(Component.CENTER_ALIGNMENT);
        } catch (IOException e) {
          e.printStackTrace();
          comp = new JLabel();
        }
      } else {
        comp = new JLabel(htmlHeader + entry);
        comp.setForeground(Color.BLACK);
        comp.setVisible(true);
        comp.setAlignmentX(Component.CENTER_ALIGNMENT);
      }
      contentPanel.add(comp);
    }

    GridBagConstraints constraints = new GridBagConstraints();
    gb.setConstraints(contentPanel, constraints);
    j.add(contentPanel);

    JButton okButton = new JButton("OK");
    AbstractAction action =
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            okButton.doClick();
          }
        };
    okButton.addActionListener(this);
    okButton.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "DoClick");
    okButton.getActionMap().put("DoClick", action);


    constraints.gridx = 0;
    constraints.gridy = 100;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;

    gb.setConstraints(okButton, constraints);
    j.add(okButton);
    j.addAncestorListener(
        new AncestorListener() {
          @Override
          public void ancestorAdded(AncestorEvent ancestorEvent) {
            okButton.requestFocus();
          }

          @Override
          public void ancestorRemoved(AncestorEvent ancestorEvent) {
          }

          @Override
          public void ancestorMoved(AncestorEvent ancestorEvent) {
          }
        });

    return j;
  }

  /**
   * Creates the OutputHeader for the data file.
   *
   * @return String of column headers
   */
  public String createOutputHeader() {
    String header =
        "sn\t"
            + "snAge\t"
            + "snGrade\t"
            + "pract\t"
            + "trial\t"
            + "cond\t"
            + "session\t"
            + "numLineSize\t"
            + "unitWidth\t"
            + "startUnit\t"
            + "endUnit\t"
            + "target\t"
            + "numLineType\t"
            + "userRespValue\t"
            + "numLineTask\t"
            + "numLineRT\t";

    header += (estStimTime > 0) ? "estStimTime" : "";

    return header.trim();
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
    // gridPanel.removeAll(); //Clears out all JComponents in panel before adding any new ones
    // gridPanel.validate();
    // gridPanel.add(new JLabel()); //Top row of gridpanel is blank
    // gridPanel.add(numLine.getPanel()); //Middle row has the NumberLine
    // gridPanel.add(new JLabel()); //Bottom row of gridPanel is blank
    // gridPanel.validate();
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
      fract =
          new Fraction(Integer.parseInt(fractStr[0].trim()), Integer.parseInt(fractStr[1].trim()));

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
   * Implemented method from ActionListener. Whenever an action is performed, display a blank screen
   * and notify the experiment of the action
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

  private static URL getUrl(String localFile) {
    URL fileUrl = null;
    try {
      fileUrl = new URL("file://" + localFile);
    } catch (MalformedURLException ex) {
      Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
    }
    return fileUrl;
  }
}
