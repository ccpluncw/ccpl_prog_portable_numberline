package ccpl.numberline;

import ccpl.lib.BlankPanel;
import ccpl.lib.DrawExpFrame;
import ccpl.lib.Experiment;
import ccpl.lib.Fixation;
import ccpl.lib.Fraction;
import ccpl.lib.Mask;
import ccpl.lib.NumberLine;
import ccpl.lib.RandomIntGenerator;
import ccpl.lib.Response;
import ccpl.lib.Specification;
import ccpl.lib.SpecificationArrayProcess;
import ccpl.lib.Unit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URL;

import java.text.DecimalFormat;

import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Number line experiment that displays a number line and asks the user for feedback.
 * Modified by Kyle Holt.
 * November, 2010.
 * @author dalecohen
 */
public class UniversalNumberLine implements ActionListener {

  private final boolean DEBUG = false;

  private Experiment exp;
  private final String experiment;
  private final String subject;
  private final String condition;
  private final String session;
  private int trialType;
  private int trialNum;
  private int totalTrials;
  private static Mask lineMask = null;
  private Fixation fixation;

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
    Experiment.presentBlankScreen(0);
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
    experiment = expFile;
    subject = sub;
    condition = cond;
    session = sess;
    trialType = 0;
    trialNum = 0;

    this.isEstimationTask = isEstimation;
    this.isBounded = isBounded;

    this.targetLow = targetLow;
    this.targetHigh = targetHigh;

    this.leftBound = leftBound;
    this.rightBound = rightBound;

    // Initializes an experiment instance with all the necessary information
    exp = new Experiment(expFile, sub, cond, sess);
  }

  /**
   * Runs the UniversalNumberLine experiment with the specified database file.
   */
  public void run() {
    // Create a new response object
    Response resp = exp.getResponse();

    Specification[] dbfile1;
    Specification[] stims;
    Specification[] fonts;

    SpecificationArrayProcess data1 = new SpecificationArrayProcess();

    ClassLoader cl = this.getClass().getClassLoader();
    URL dbFilePath = cl.getResource(experiment + "/infiles/dbfile_ut_0_1.txt");

    // Read in the database file
    dbfile1 = data1.readFromURL(dbFilePath);

    // BEGIN PARSING OF DATABASE FILE
    Experiment.practiceTrialFile = dbfile1[0].getParsedStringSpec(1);

    if (Experiment.practiceTrialFile.equalsIgnoreCase("none")) {
      trialType = 1;
    } else {
      trialType = 0;
    }

    Experiment.expTrialFile = dbfile1[1].getParsedStringSpec(1);
    exp.setInstruction(dbfile1[2].getParsedStringSpec(1));
    Experiment.fontFile = dbfile1[3].getParsedStringSpec(1);

    int restNumber = dbfile1[4].getParsedIntSpec(1);
    // int rightMargin = dbfile1[5].getParsedIntSpec(1);

    final int baseR = dbfile1[5].getParsedIntSpec(1);
    final int baseG = dbfile1[5].getParsedIntSpec(2);
    final int baseB = dbfile1[5].getParsedIntSpec(3);

    final int dragR = dbfile1[6].getParsedIntSpec(1);
    final int dragG = dbfile1[6].getParsedIntSpec(2);
    final int dragB = dbfile1[6].getParsedIntSpec(3);

    final int handleR = dbfile1[7].getParsedIntSpec(1);
    final int handleG = dbfile1[7].getParsedIntSpec(2);
    final int handleB = dbfile1[7].getParsedIntSpec(3);

    boolean isMask = false;

    int stimMaskTime = 0;
    int maskTime = 0;
    int fixationTime = 0;

    if (Experiment.isParamOn(dbfile1[11].getParsedStringSpec(1))) {
      isMask = true;
      stimMaskTime = dbfile1[11].getParsedIntSpec(2);
      maskTime = dbfile1[11].getParsedIntSpec(3);
      fixationTime = dbfile1[11].getParsedIntSpec(4);
    }
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
    Color[] numLineColors = {baseColor, dragColor, handleActiveColor};


    frame = exp.getFrame();
    exp.setFullScreen();

    // TODO: Fix this to handle none of this feedback
    //Feedback.setFeedback(provideImageFeedback, provideFractalFeedback, provideAniFeedback);

    // Set up data data file
    URL dataFile = exp.getDataFile();

    // Prepare the database file
    DecimalFormat df = new DecimalFormat("#.####");
    StringBuilder outString = new StringBuilder();

    boolean responseEnabled = false;
    boolean questionEnabled = dbfile1[12].getParsedStringSpec(1).trim().equalsIgnoreCase("on");

    if (questionEnabled) {
      responseEnabled = dbfile1[13].getParsedStringSpec(1).trim().equalsIgnoreCase("response");
    }

    boolean useMouse;

    try {
      useMouse = dbfile1[15].getParsedStringSpec(1).trim().equalsIgnoreCase("on");
    } catch (IndexOutOfBoundsException e) {
      System.out.println("useMouse click flag missing, defaulting to spacebar");
      useMouse = false;
    }

    data1.writeToURL(Experiment.getCGI(), dataFile,
        createOutputHeader(questionEnabled, responseEnabled));

    Specification[] questions = null;
    Specification[] practiceQuestions = null;
    int questionPointer = 0;
    int practiceQuestionPointer = 0;
    if (questionEnabled) {
      String path = exp.getInfilesPath() + dbfile1[12].getParsedStringSpec(7);
      URL url = cl.getResource(path);
      questions = data1.readFromURL(url);

      questions = data1.randomize(questions);

      if (!Experiment.practiceTrialFile.equalsIgnoreCase("none")) {
        practiceQuestions = data1.readFromURL(exp.getURL(exp.getInfilesPath()
            + dbfile1[12].getParsedStringSpec(8)));

        practiceQuestions = data1.randomize(practiceQuestions);
      }
    }

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

    Dimension paneld = imPanel.getSize();
    // paneld.width = rightMargin;
    // rightMarginPanel.setPreferredSize(paneld);

    BlankPanel endPanel = new BlankPanel(Color.BLACK);
    frame.setContentPane(startPanel);
    frame.setVisible(true);

    String userResp;
    String userRespVal;

    //  get array of usable font names
    fonts = data1.readFromURL(exp.getFontFile());
    String[] fontNames = new String[fonts.length];

    for (int i = 0; i < fonts.length; i++) {
      fontNames[i] = fonts[i].getParsedStringSpec(1);
    }

    frame.hideCursor();
    resp.testTimer(startPanel, Color.white, 1000);
    frame.showCursor();

    JPanel instructionPanel = getInstructionPanel(exp.getInstructionFile());
    frame.setContentPane(instructionPanel);
    frame.setVisible(true);
    try {
      synchronized (this) {
        wait();
      }
    } catch (InterruptedException ex) {
      //Logger.getLogger(CenteredFileDisplay.class.getName()).log(Level.SEVERE, null, ex);
    }
    frame.remove(instructionPanel);
    frame.validate();
    String leftValue = "";
    String rightValue = "";
    if (responseEnabled) {
      leftValue = dbfile1[13].getParsedStringSpec(2);
      rightValue = dbfile1[13].getParsedStringSpec(3);
    }

    String instruct2;

    try {
      instruct2 = dbfile1[2].getParsedStringSpec(2);
    } catch (Exception e) {
      e.printStackTrace();
      instruct2 = null;
    }


    getButtonMap("Left click", "Right click", leftValue, rightValue, frame);

    boolean flag = false;

    for (; trialType < 2; ++trialType) {

      if (trialType == 0) {
        flag = true;
        stims = data1.readFromURL(exp.getPracticeFile());
        stims = data1.randomize(stims);
        Experiment.prepareToStartPractice(frame);
      } else {
        frame.showCursor();

        stims = data1.readFromURL(exp.getExperimentFile());
        stims = data1.randomize(stims);
        frame.showCursor();
        Experiment.prepareToStartExperiment(frame);

        if (instruct2 != null) {
          // Get a secondary instruction panel.
          JPanel secondInstruct = getInstructionPanel(
              exp.getURL(exp.getInfilesPath() + instruct2));

          // Set the content pane and make the frame visible
          frame.setContentPane(secondInstruct);
          frame.setVisible(true);

          // Wait for panel to close
          try {
            synchronized (this) {
              wait();
            }
          } catch (InterruptedException e) {
            // Nothing
          }

          // Remove the panel
          frame.remove(secondInstruct);

          // Validate the frame
          frame.validate();
        }

        if (flag) {
          // Uncomment for old button map.
          //resp.getMouseMap(leftValue, rightValue, frame);
          // Display new button map
          getButtonMap("Left click", "Right click", leftValue, rightValue, frame);
          flag = false;
        }
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

        String myFontName = Experiment.getRandomFontName(fontNames);

        int questionDelay = -1;
        String initQuestion = "";
        double questionAffectValue = -1.0;
        BlankPanel questionPanel = new BlankPanel(imColor);
        String questionPosNeg = "";

        if (questionEnabled) {
          if (trialType == 1) {
            if (questionPointer >= questions.length) {
              questionPointer = 0;
              questions = data1.randomize(questions);
            }

            initQuestion = questions[questionPointer].getParsedStringSpec(1);
            questionPosNeg = questions[questionPointer].getParsedStringSpec(3);
            handleLabel = questions[questionPointer].getParsedStringSpec(2);
            questionAffectValue = questions[questionPointer].getParsedDoubleSpec(4);
            questionPointer++;
          } else {
            initQuestion = practiceQuestions[practiceQuestionPointer].getParsedStringSpec(1);
            questionPosNeg = practiceQuestions[practiceQuestionPointer].getParsedStringSpec(3);
            handleLabel = practiceQuestions[practiceQuestionPointer].getParsedStringSpec(2);
            questionAffectValue = practiceQuestions[practiceQuestionPointer].getParsedDoubleSpec(4);
            practiceQuestionPointer++;
          }

          final int fsize = dbfile1[12].getParsedIntSpec(5);
          final int questionWidth = dbfile1[12].getParsedIntSpec(6);

          int fontColorR = dbfile1[12].getParsedIntSpec(2);
          int fontColorG = dbfile1[12].getParsedIntSpec(3);
          int fontColorB = dbfile1[12].getParsedIntSpec(4);

          final Color fontColor = new Color(fontColorR, fontColorG, fontColorB);

          //int questionMargin = 200;
          Dimension d = frame.getSize();
          questionPanel.setSize(d);
          questionPanel.setLayout(new GridBagLayout());

          if (initQuestion.contains("#")) {
            initQuestion = initQuestion.substring(0, initQuestion.indexOf('#'))
                + randTarget.toString()
                + initQuestion.substring(initQuestion.indexOf('#') + 1);
          }

          String html1;
          String html2;
          html1 = "<html><body style='width:";
          html2 = "px'><center>";

          Font f = new Font(myFontName, Font.PLAIN, fsize);

          JLabel questionLabel = new JLabel(html1 + questionWidth + html2 + initQuestion);
          questionLabel.setFont(f);
          questionLabel.setForeground(fontColor);
          questionPanel.add(questionLabel);
        }

        String unitLabel = stims[trialNum].getParsedStringSpec(28);
        int degrees = stims[trialNum].getParsedIntSpec(27);

        char leftOrRight = stims[trialNum].getParsedCharSpec(24);

        numLine = new NumberLine(randWidth, height, thickness, startUnit, endUnit,
            randTarget, baseColor, dragColor, handleActiveColor, myFontName,
            isEstimateTask, degrees, leftMargin, keepWithinBounds, leftOrRight,
            showFullBaseScale, handleLabel, unitLabel);

        //show question if enabled and gather appropriate response
        int mouseResponse;
        long mouseRt = -1;
        String mouseResponseLabel = "";
        String mouseResponseCorrect = "";

        String temp = dbfile1[13].getParsedStringSpec(1).trim();

        if (temp.equalsIgnoreCase("response") && questionEnabled) {
          imPanel.add(questionPanel, BorderLayout.CENTER);
          frame.setContentPane(imPanel);
          frame.validate();
          frame.hideCursor();

          resp.getTimedMouseClickResponse(questionPanel);
          mouseResponse = resp.getMouseClickButton();
          mouseRt = resp.getRT();
          imPanel.remove(questionPanel);

          if (mouseResponse == 1) {
            mouseResponseLabel = leftValue;
          } else if (mouseResponse == 3) {
            mouseResponseLabel = rightValue;
          }

          if (mouseResponseLabel.trim().equalsIgnoreCase(questionPosNeg.trim())) {
            mouseResponseCorrect = "correct";
          } else if (questionPosNeg.equals("neutral")) {
            mouseResponseCorrect = "N/A";
          } else {
            mouseResponseCorrect = "incorrect";
          }
        } else if (temp.equalsIgnoreCase("time") && questionEnabled) {
          String fixedOrWord = dbfile1[13].getParsedStringSpec(2).trim();
          questionDelay = dbfile1[13].getParsedIntSpec(3);
          int wordCount = 0;
          if (fixedOrWord.equalsIgnoreCase("word")) {
            StringTokenizer tk = new StringTokenizer(initQuestion, " ");
            while (tk.hasMoreTokens()) {
              tk.nextToken();
              wordCount++;
            }

            questionDelay *= wordCount;
          }

          imPanel.add(questionPanel, BorderLayout.CENTER);
          frame.setContentPane(imPanel);
          frame.setVisible(true);
          Experiment.delay(questionDelay);
          imPanel.remove(questionPanel);
        }
        //End question

        //Displays Fixation if necessary
        fixation = new Fixation(Color.BLACK, baseColor, thickness, numLine.getFixationLine());
        fixationPanel.removeAll();
        //fixationPanel.add(new JLabel()); //Top row of gridpanel is blank
        fixationPanel.add(fixation, BorderLayout.CENTER); //Middle row has the NumberLine
        //fixationPanel.add(new JLabel()); //Bottom row of gridPanel is blank

        if (isEstimateTask && isMask) {
          imPanel.add(fixationPanel, BorderLayout.CENTER);
          frame.setContentPane(imPanel);
          frame.validate();
          Experiment.delay(fixationTime);
          imPanel.remove(fixationPanel);
        }
        //end fixation

        //show numline and gather response
        presentTrial();

        if (!isEstimateTask) {
          frame.showCursor();

          //Idle here until user has hit the space bar
          reactTime = resp.getTimedNumberLineResponse(numLine, useMouse);

          frame.remove(imPanel);
          frame.setContentPane(endPanel);
          userResp = df.format(numLine.getUnitLength());
          userRespVal = userResp;

          if (DEBUG) {
            System.out.println(userRespVal);
          }

        } else {
          if (estimateTime > 0) {
            final long startTime = new Date().getTime();

            Experiment.delay(estimateTime);

            frame.remove(imPanel);
            frame.setContentPane(endPanel);
            frame.validate();

            reactTime = new Date().getTime() - startTime;
          } else {
            reactTime = resp.getTimedNumberLineResponse(numLine, useMouse);

            frame.remove(imPanel); //remove the imPanel
            frame.setContentPane(endPanel);
            frame.validate();
          }
          if (isMask) {
            Experiment.delay(stimMaskTime);

            lineMask = new Mask(thickness, imColor, numLineColors);
            frame.setContentPane(lineMask);
            frame.validate();

            Experiment.delay(maskTime);

            frame.remove(lineMask);
          }

          frame.showCursor();
          frame.setContentPane(startPanel);
          frame.validate();
          resp.getTimedNumPadResponse(frame, "What is the target of this number line?",
              estTargetFormat);
          textRt = resp.getTextRT();
          userResp = resp.getTextValue();
          userRespVal = df.format(numLine.getUnitLength(userResp));
        }
        //frame.hideCursor();
        frame.remove(startPanel);
        frame.setContentPane(endPanel);
        frame.validate();

        Experiment.delay(1000);

        if ("incorrect".equals(mouseResponseCorrect) && trialType == 0) {
          frame.showCursor();
          reminderMessage(leftValue, rightValue, resp);
          frame.hideCursor();
        }

        //Feedback.setNextFeedback(trialType, trialNum, totalTrials);

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
        outString.append(unitLabel);
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
        if (questionEnabled) {
          outString.append(initQuestion);
          outString.append("\t");
          outString.append(questionPosNeg); //positive/negative question
          outString.append("\t");
          outString.append(questionAffectValue);
          outString.append("\t");
          if (questionDelay != -1) {
            outString.append(questionDelay);
          } else {
            outString.append(mouseResponseLabel);
            outString.append("\t");
            outString.append(mouseRt);
            outString.append("\t");
            outString.append(mouseResponseCorrect);
            outString.append("\t");
          }
        }

        String outStringTmp = outString.toString().replaceAll("true", "TRUE");
        outStringTmp = outStringTmp.replaceAll("false", "FALSE");

        data1.writeToURL(Experiment.getCGI(), dataFile, outStringTmp);
        outString.setLength(0);

        frame.remove(endPanel);
        frame.validate();
        frame.setContentPane(endPanel);
        if ((trialNum + 1) % restNumber == 0) {
          Experiment.rest();
        }
        imPanel.remove(leftMarginPanel);
        imPanel.remove(gridPanel);
        numLine = null;
        imPanel.removeAll();
      }
    }
    Experiment.thankYou();
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

    JButton okbutton = new JButton("OK");
    okbutton.addActionListener(this);


    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;

    gb.setConstraints(okbutton, c);

    j.add(okbutton);

    return j;
  }

  /**
   * Creates the OutputHeader for the data file.
   *
   * @param isQuestionEnabled Does the experiment ask questions
   * @param questionResponse  Does the experiment has question responses
   * @return String of column headers
   */
  private String createOutputHeader(boolean isQuestionEnabled, boolean questionResponse) {
    StringBuilder sbuf = new StringBuilder();
    sbuf.append("exp\t");
    sbuf.append("sn\t");
    sbuf.append("pract\t");
    sbuf.append("trial\t");
    sbuf.append("cond\t");
    sbuf.append("session\t");
    sbuf.append("margin\t");//("leftMargin\t");
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
    //sbuf.append("allBaseUnits\t");
    sbuf.append("userResp\t");
    sbuf.append("userRespValue\t");
    sbuf.append("unitErr\t");
    sbuf.append("estTask\t");
    sbuf.append("estStimTime\t");
    sbuf.append("numLineRT\t");
    sbuf.append("textRt\t");
    sbuf.append("feedbackType\t");

    if (isQuestionEnabled) {
      sbuf.append("question\t");
      sbuf.append("questionKey\t");
      sbuf.append("questionAffectValue\t");
      if (!questionResponse) {
        sbuf.append("questionDisplay");
      } else {
        sbuf.append("mouseResponse\t");
        sbuf.append("mouseRT\t");
        sbuf.append("questionCorrect\t");
      }
    }

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

  private void getButtonMap(String b1, String b2, String b1Label, String b2Label,
                            JFrame parentFrame) {
    // Create a dialog to prompt
    final JDialog dialog = new JDialog(parentFrame, true);

    JButton ok = new JButton("OK");
    ok.addActionListener(actionEvent -> {
      dialog.setVisible(false);
      dialog.dispose();
    });


    // Do not let people close out
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    String topMsg = String.format("Please use the '%s' and '%s' to indicate your response", b1, b2);

    JLabel topLabel = new JLabel(topMsg);
    topLabel.setHorizontalAlignment(SwingConstants.CENTER);
    topLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    Font font2 = new Font(topLabel.getFont().getFontName(), Font.PLAIN, 14);
    topLabel.setFont(font2);


    Font font = new Font(topLabel.getFont().getFontName(), Font.PLAIN, 32);


    String sameMsg = String.format("%s = %s", b1, b1Label.toLowerCase());
    String diffMsg = String.format("%s = %s", b2, b2Label.toLowerCase());

    JLabel sameL = getLabel(sameMsg, font);
    sameL.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

    JLabel diffL = getLabel(diffMsg, font);
    diffL.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

    JPanel topPanel = new JPanel();
    topPanel.setBackground(new Color(200, 200, 200));
    topPanel.add(topLabel);
    topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

    JPanel bodyPanel = new JPanel();
    bodyPanel.setBackground(new Color(200, 200, 200));
    bodyPanel.setLayout(new GridLayout(0, 1));
    bodyPanel.add(sameL);
    bodyPanel.add(diffL);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(new Color(200, 200, 200));
    buttonPanel.add(ok);

    dialog.setLayout(new BorderLayout());

    dialog.getContentPane().add(topPanel, BorderLayout.NORTH);
    dialog.getContentPane().add(bodyPanel, BorderLayout.CENTER);
    dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    dialog.getRootPane().setDefaultButton(ok);
    dialog.pack();

    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

    dialog.setLocation((d.width - dialog.getWidth()) / 2, (d.height - dialog.getHeight()) / 2);
    dialog.setVisible(true);

    frame.hideCursor(0, 0);
  }

  private JLabel getLabel(String message, Font font) {
    JLabel newLabel = new JLabel(message);
    newLabel.setFont(font);
    newLabel.setHorizontalAlignment(SwingConstants.CENTER);

    return newLabel;
  }
}
