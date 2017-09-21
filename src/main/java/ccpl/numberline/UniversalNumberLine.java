package ccpl.numberline;

import ccpl.lib.BlankPanel;
import ccpl.lib.CenteredFileDisplay;
import ccpl.lib.DrawExpFrame;
import ccpl.lib.Experiment;
import ccpl.lib.Fixation;
import ccpl.lib.Fraction;
import ccpl.lib.Mask;
import ccpl.lib.NumberLine;
import ccpl.lib.RTPressure;
import ccpl.lib.RandomIntGenerator;
import ccpl.lib.Response;
import ccpl.lib.Specification;
import ccpl.lib.SpecificationArrayProcess;
import ccpl.lib.Unit;
import ccpl.lib.feedback.Feedback;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;


/****************
 * @author dalecohen
 * Modified by Kyle Holt
 * November, 2010
 ****************/
public class UniversalNumberLine implements ActionListener {

    private final boolean WEB_ENABLED = true;
    private final String WEB_DIR = "newnumline";
    private final boolean debug = false;
    private final static int leftClick = 1, rightClick = 3;//the numbers that represent a left or right click for the mouse the program is using

    private Experiment exp;
    private final String experiment, subject, condition, session;
    private int trialType, trialNum, totalTrials;
    private static Mask lineMask = null;
    private Fixation fixation;
    private RTPressure rtPressure;

    /**
     * Implemented method from ActionListener.
     * Whenever an action is performed, display a blank screen and notify the
     * experiment of the action
     * @param e     ActionEvent object used to get information about the action
     */
    @Override public void actionPerformed(ActionEvent e) {
        Experiment.presentBlankScreen(0);
        synchronized(this){
                notify();
        }
    }
    private enum FEEDBACKTYPE {ANIMATION, IMAGE, FRACTAL}
    private static NumberLine numLine = null;
    private static RandomIntGenerator randGen = new RandomIntGenerator();
    private static BlankPanel imPanel, gridPanel;
    private static DrawExpFrame frame;
    
    private enum MOUSE_BUTTON{
        LEFT, RIGHT
    }

    /**
     * Entry point into the experiment
     * @param args          Command line argument
     * @throws Exception    Throws an exception when the program malfunctions.
     *                      Don't try to handle it, program should crash to 
     *                      prevent experiment data from being contaminated.
     */
    public static void main(String[] args) throws Exception {
        UniversalNumberLine numline = new UniversalNumberLine(args[1], args[2], args[3], args[4]);
        numline.run(args[0]);
    }
    
    /**
     * Creates and displays a reminder message about the correct key presses
     * @param leftValue     Value of the left key press
     * @param rightValue    Value of the right key press
     * @param resp          Response object that will display the notification
     */
    private void reminderMessage(String leftValue, String rightValue, Response resp){
//        String message = "<html><font size='4'><center>Please Remember</font><BR>"
//                + "<font size = '3'>"
//                + "The left click indicates '" + leftValue + "'. The right click indicates '"
//                + rightValue + "'.</center></font></html>";
        
        // Message that will be displayed.
        String message = "Please remember: The left click indicates '" + leftValue + "'. The right click indicates '" + rightValue + "'";
        //JOptionPane optionPane = new JOptionPane(new JLabel(message), JLabel.CENTER);
        //JDialog d = optionPane.createDialog("Reminder");
        //d.setVisible(true);
        
        // Display the notification with the above message string
        resp.displayNotificationFrame(frame, message);
//        JOptionPane.showMessageDialog(frame, message, "Reminder", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Parameterized constructor allow the specification of an experiment file,
     * the subject ID, condition, and session number.
     * @param expFile   Experiment file
     * @param sub       Subject ID
     * @param cond      Condition
     * @param sess      Session number
     */
    public UniversalNumberLine(String expFile, String sub, String cond, String sess){
        experiment = expFile;
        subject = sub;
        condition = cond;
        session = sess;
        trialType = 0;
        trialNum = 0;

        // Initializes an experiment instance with all the necessary information
        exp = new Experiment(expFile, sub, cond, sess);
        exp.setWebEnabled(WEB_ENABLED);
        if(exp.isWebEnabled()){
            exp.setWebCodeBase(WEB_DIR);
            
            if(debug) System.out.println("-------------------");
        }
    }

    /**
     * Runs the UniversalNumberLine experiment with the specified database file
     * @param dbFilePath    Path to database file
     */
    public void run(String dbFilePath) {
      // Create a new response object
      Response resp = exp.getResponse();

      Specification[] dbfile1;
      Specification[] stims;
      Specification[] fonts;
      SpecificationArrayProcess data1 = new SpecificationArrayProcess();

      // Read in the database file
      dbfile1 = data1.readFromURL(exp.getDBFile(dbFilePath));

      /* BEGIN PARSING OF DATABASE FILE */
      Experiment.practiceTrialFile = dbfile1[0].getParsedStringSpec(1);

      if(Experiment.practiceTrialFile.equalsIgnoreCase("none")) {
          trialType = 1;
      } else {
          trialType = 0;
      }

      Experiment.expTrialFile = dbfile1[1].getParsedStringSpec(1);
      exp.setInstruction(dbfile1[2].getParsedStringSpec(1));
      Experiment.fontFile = dbfile1[3].getParsedStringSpec(1);
        
      int restNumber = dbfile1[4].getParsedIntSpec(1);
      // int rightMargin = dbfile1[5].getParsedIntSpec(1);

      int baseR = dbfile1[5].getParsedIntSpec(1);
      int baseG = dbfile1[5].getParsedIntSpec(2);
      int baseB = dbfile1[5].getParsedIntSpec(3);

      int dragR = dbfile1[6].getParsedIntSpec(1);
      int dragG = dbfile1[6].getParsedIntSpec(2);
      int dragB = dbfile1[6].getParsedIntSpec(3);

      int handleR = dbfile1[7].getParsedIntSpec(1);
      int handleG = dbfile1[7].getParsedIntSpec(2);
      int handleB = dbfile1[7].getParsedIntSpec(3);

      String isImageFeedback = dbfile1[8].getParsedStringSpec(1);

      boolean provideAniFeedback = false, provideImageFeedback = false, provideFractalFeedback = false;

      boolean isAniDetermined = false, isFractalDetermined = false, showJulia = false, showMandelbrot = false, useBlockTransition = false, useFadeTransition = false;
      int playFrequency = 1, fractalFrequency = 1;
      int fractalTransitionTime = 1000, fractalPresentTime = 1000;
      String fractalType = null, fractalTransitionType = null;

      int imageHoldTime = 0;
      isImageFeedback = isImageFeedback.toLowerCase();
      
      if(Experiment.isParamOn(isImageFeedback)){
        imageHoldTime = dbfile1[8].getParsedIntSpec(2);
        provideImageFeedback = true;
      }

      String isFractalFeedback = dbfile1[9].getParsedStringSpec(1);
      isFractalFeedback = isFractalFeedback.toLowerCase();
      String fractalOutputString = "";
      if(Experiment.isParamOn(isFractalFeedback)){
          provideFractalFeedback = true;
          String determined = dbfile1[9].getParsedStringSpec(2);
          if(Experiment.paramMatches(determined, "determined"))
              isFractalDetermined = true;
          else
              isFractalDetermined = false;

          fractalFrequency = dbfile1[9].getParsedIntSpec(3);
          fractalPresentTime = dbfile1[9].getParsedIntSpec(4);
          fractalOutputString = determined + "_" + fractalFrequency;

          fractalType = dbfile1[9].getParsedStringSpec(5);
          fractalTransitionTime = dbfile1[9].getParsedIntSpec(6);
          fractalTransitionType = dbfile1[9].getParsedStringSpec(7);
          if(Experiment.paramMatches(fractalType, "mandelbrot")){
              showMandelbrot = true;
          }else if(Experiment.paramMatches(fractalType,"julia")){
              showJulia = true;
          }else{
              showMandelbrot = true;
              showJulia = true;
          }
          if(Experiment.paramMatches(fractalTransitionType, "block")){
              useBlockTransition = true;
          }else if (Experiment.paramMatches(fractalTransitionType, "fade")){
              useFadeTransition = true;
          }else {
              useBlockTransition = true;
              useFadeTransition = true;
          }
      } else {
        fractalOutputString = isFractalFeedback;
      }


      String isAnimationFeedback = dbfile1[10].getParsedStringSpec(1);
      isAnimationFeedback = isAnimationFeedback.toLowerCase();
      String aniOutputString = "";
      
      if(Experiment.isParamOn(isAnimationFeedback)){
            provideAniFeedback = true;
            String determined = dbfile1[10].getParsedStringSpec(2);
            if(Experiment.paramMatches(determined, "determined"))
                isAniDetermined = true;
            else
                isAniDetermined = false;
            playFrequency = dbfile1[10].getParsedIntSpec(3);
            aniOutputString = determined + "_" + playFrequency;
        }else
            aniOutputString = isAnimationFeedback;

        boolean isMask = false;
        int stimMaskTime = 0, maskTime = 0, fixationTime = 0;
        if(Experiment.isParamOn(dbfile1[11].getParsedStringSpec(1))){
            isMask = true;
            stimMaskTime = dbfile1[11].getParsedIntSpec(2);
            maskTime = dbfile1[11].getParsedIntSpec(3);
            fixationTime = dbfile1[11].getParsedIntSpec(4);
        }
        
        // RT Presurre Setup
        int windowSize = -1;
        double quantileThreshold = -1;
        double errorRateThreshold = -1;
        int initDeadline = -1;
        int rtFreq = -1;
        int rtDuration = -1;
        
        if(Experiment.isParamOn(dbfile1[14].getParsedStringSpec(1))){
            windowSize = dbfile1[14].getParsedIntSpec(2);
            quantileThreshold = dbfile1[14].getParsedDoubleSpec(3);
            errorRateThreshold = dbfile1[14].getParsedDoubleSpec(4);
            initDeadline = dbfile1[14].getParsedIntSpec(5);
            rtFreq = dbfile1[14].getParsedIntSpec(6);
            rtDuration = dbfile1[14].getParsedIntSpec(7);
            rtPressure = new RTPressure(windowSize, quantileThreshold, errorRateThreshold, initDeadline);
        }
        
        /* END PARSING OF DATABASE FILE */
  
        Color baseColor = new Color(baseR, baseG, baseB);
        Color dragColor = new Color(dragR, dragG, dragB);
        Color handleActiveColor = new Color(handleR, handleG, handleB);
        Color[] numLineColors = {baseColor,dragColor,handleActiveColor};
        
        
        frame = exp.getFrame();
        exp.setFullScreen();
        
        Feedback.setFeedback(provideImageFeedback, provideFractalFeedback, provideAniFeedback);
        Feedback.initImageFeedback(UniversalNumberLine.class
                .getResourceAsStream("/resources/images.list"), imageHoldTime);
        
        Feedback.initVideoFeedback(UniversalNumberLine.class
                .getResourceAsStream("/resources/animations.list"), isAniDetermined, playFrequency);
        
        Feedback.initFractalFeedback(fractalTransitionTime, fractalPresentTime, isFractalDetermined,
                fractalFrequency, useBlockTransition, useFadeTransition, showJulia, showMandelbrot);

        /* set up data data file*/
        URL dataFile = exp.getDataFile();
        
        /* prepare the database file */
        DecimalFormat df = new DecimalFormat("#.####");
        StringBuilder outString = new StringBuilder();
        
        boolean responseEnabled = false;
        boolean questionEnabled = dbfile1[12].getParsedStringSpec(1).trim().equalsIgnoreCase("on");
        
        if(questionEnabled) {
          responseEnabled = dbfile1[13].getParsedStringSpec(1).trim().equalsIgnoreCase("response");
        }
        
        boolean useMouse;
        
        try {
          useMouse = dbfile1[15].getParsedStringSpec(1).trim().equalsIgnoreCase("on");
        } catch (IndexOutOfBoundsException e) {
          System.out.println("useMouse click flag missing, defaulting to spacebar");
          useMouse = false;
        }
        
        data1.writeToURL(Experiment.getCGI(), dataFile, UniversalNumberLine.createOutputHeader(questionEnabled, responseEnabled, rtPressure != null));
        
        Specification[] questions = null;
        Specification[] practiceQuestions = null;
        int questionPointer = 0;
        int practiceQuestionPointer = 0;
        if(questionEnabled){
            questions = data1.readFromURL(exp.getURL(exp.getInfilesPath() + dbfile1[12].getParsedStringSpec(7)));
            questions = data1.randomize(questions);
            if(!Experiment.practiceTrialFile.equalsIgnoreCase("none")){
                practiceQuestions = data1.readFromURL(exp.getURL(exp.getInfilesPath() + dbfile1[12].getParsedStringSpec(8)));
                practiceQuestions = data1.randomize(practiceQuestions);
            }
        }
        
        //----prepare frame---------
        Color imColor = Color.BLACK;
        BlankPanel startPanel = new BlankPanel(imColor);
        imPanel = new BlankPanel(imColor);
        BlankPanel leftMarginPanel = new BlankPanel(imColor);
//        BlankPanel rightMarginPanel = new BlankPanel(imColor);
        gridPanel = new BlankPanel(imColor);
        BlankPanel fixationPanel = new BlankPanel(imColor);
        //fixationPanel.setLayout(new GridLayout(3,1));
        fixationPanel.setLayout(new BorderLayout());
        
        imPanel.setLayout(new BorderLayout());
        gridPanel.setLayout(new GridLayout(3,1)); 

        Dimension paneld = imPanel.getSize();
//        paneld.width = rightMargin;
//        rightMarginPanel.setPreferredSize(paneld);

        BlankPanel endPanel = new BlankPanel(Color.BLACK);
        frame.setContentPane(startPanel);
        frame.setVisible(true);

        String userResp = "", userRespVal = "";
        //  get array of usable font names
        fonts = data1.readFromURL(exp.getFontFile());
        String[] fontNames = new String[fonts.length];
        for (int i = 0; i < fonts.length; i++) {
            fontNames[i] = fonts[i].getParsedStringSpec(1);
        }

        frame.hideCursor();
        resp.testTimer(startPanel, Color.black, Color.white, 1000);
        frame.showCursor();
        
        JPanel instructionPanel = getInstructionPanel(exp.getInstructionFile());
        frame.setContentPane(instructionPanel);
        frame.setVisible(true);
        try{
            synchronized(this){
                wait();
            }
        }
        catch(InterruptedException ex){
            Logger.getLogger(CenteredFileDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
        frame.remove(instructionPanel);
        frame.validate();
        String leftValue = "";
        String rightValue = "";
        if(responseEnabled){
            leftValue = dbfile1[13].getParsedStringSpec(2);
            rightValue = dbfile1[13].getParsedStringSpec(3);
        }
        
        /*//Experiment.showInstructions();
        if(dbFilePath.toCharArray()[10] == '0') {
            resp.getButtonMap("Please use the space bar to submit the number line and move to the next trial", frame);
        }*/
        

        //if(responseEnabled)
        //    resp.getMouseMap(leftValue, rightValue, frame);
	
      String instruct2;

      try {
        instruct2 = dbfile1[2].getParsedStringSpec(2);
      } catch (Exception e) {
        e.printStackTrace();
        instruct2 = null;
      }


        getButtonMap("Left click", "Right click", leftValue, rightValue, frame);

        boolean __flag = false;
        for (trialType = trialType; trialType < 2; ++trialType) {
            if (trialType == 0) {
                __flag = true;
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
                  JPanel secondInstruct  = getInstructionPanel(
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
                
                if(__flag){
                  // Uncomment for old button map.
                  //resp.getMouseMap(leftValue, rightValue, frame);
                  // Display new button map
                  getButtonMap("Left click", "Right click", leftValue, rightValue, frame);
                  __flag = false;
                }
                frame.hideCursor();
            }

            int trialLength = stims.length;
            totalTrials = stims.length;
            frame.requestFocus();
            
            if(trialType == 1  && rtPressure != null) {
              //rtPressure.showHeadphoneDialog();
              
              // Override the headphone flag.
              // This is done because the showHeadphoneDialog() has  been removed from this
              // iteration of the experiment, and the showHeadphoneDialog() is required before
              // rt pressure will be enabled.
              rtPressure.overrideHeadphoneFlag(true);
            }
            
            for (trialNum = 0; trialNum < trialLength; trialNum++) {
                frame.showCursor();
                long RT = 0, textRT = 0;
                int leftMarginLow = stims[trialNum].getParsedIntSpec(1);
                int leftMarginHigh = stims[trialNum].getParsedIntSpec(2);
                int leftMarginInterval = stims[trialNum].getParsedIntSpec(3);
                int widthLow = stims[trialNum].getParsedIntSpec(4);
                int widthHigh = stims[trialNum].getParsedIntSpec(5);
                int widthInterval = stims[trialNum].getParsedIntSpec(6);
                int height = stims[trialNum].getParsedIntSpec(7);
                int thickness = stims[trialNum].getParsedIntSpec(8);
                Unit startUnit = new Unit(stims[trialNum].getParsedStringSpec(9)) ;
                Unit endUnit = new Unit(stims[trialNum].getParsedStringSpec(10));
                Unit targetUnitLow = new Unit(stims[trialNum].getParsedStringSpec(11));
                Unit targetUnitHigh = new Unit(stims[trialNum].getParsedStringSpec(12));
                Unit targetUnitInterval = new Unit(stims[trialNum].getParsedStringSpec(13));

                String startUnitFormat = stims[trialNum].getParsedStringSpec(14);
                startUnitFormat = startUnitFormat.toUpperCase();
                String endUnitFormat = stims[trialNum].getParsedStringSpec(15);
                endUnitFormat = endUnitFormat.toUpperCase();
                String targetUnitFormat = stims[trialNum].getParsedStringSpec(16);
                targetUnitFormat = targetUnitFormat.toUpperCase();

                boolean isEstimateTask = Boolean.parseBoolean((stims[trialNum].getParsedStringSpec(17).trim()));

                int estimateTime = 0;
                if(isEstimateTask)
                    estimateTime = stims[trialNum].getParsedIntSpec(18);

                String estTargetFormat = stims[trialNum].getParsedStringSpec(19);

                String __start = stims[trialNum].getParsedStringSpec(20);
                String __end = stims[trialNum].getParsedStringSpec(21);
                String __target = stims[trialNum].getParsedStringSpec(22);
                String __handle = stims[trialNum].getParsedStringSpec(23);
                
                boolean showFullBaseScale[] = new boolean[4];
                showFullBaseScale[0] = Boolean.parseBoolean(__start);
                showFullBaseScale[1] = Boolean.parseBoolean(__end);
                showFullBaseScale[2] = Boolean.parseBoolean(__target);
                showFullBaseScale[3] = Boolean.parseBoolean(__handle);
                
                char leftOrRight = stims[trialNum].getParsedCharSpec(24);
                
                boolean[] keepWithinBounds = new boolean[2]; //location 0 in the array determines if the handle can move past the left bound location 1 determines the righ bound
                String __left = stims[trialNum].getParsedStringSpec(25);
                keepWithinBounds[0] = Boolean.parseBoolean(__left);
                
                String __right = stims[trialNum].getParsedStringSpec(26); 
                keepWithinBounds[1] = Boolean.parseBoolean(__right);
                
                
                //Update the leftMarginPanel in each trial
                int leftMargin = UniversalNumberLine.getRandomLeftMargin(leftMarginLow, leftMarginHigh, leftMarginInterval);
                paneld = imPanel.getSize();
                paneld.width = leftMargin;
                leftMarginPanel.setPreferredSize(paneld);

                //imPanel.add(leftMarginPanel, BorderLayout.WEST);                

                frame.remove(startPanel);
                //frame.hideCursor();

                randGen = new RandomIntGenerator(widthLow, widthHigh, widthInterval);
                int randWidth = randGen.drawWithInterval();

                Unit randTarget = Unit.getRandomUnit(targetUnitLow, targetUnitHigh, targetUnitInterval);
                

                startUnit = reduceUnit(startUnitFormat, startUnit);
                endUnit = reduceUnit(endUnitFormat, endUnit);
                randTarget = reduceUnit(targetUnitFormat, randTarget);
                String handleLabel = "";
                
                String myFontName = Experiment.getRandomFontName(fontNames);
                
                int questionDelay = -1;
                String initQuestion = "";
                double questionAffectValue = -1.0;
                BlankPanel questionPanel = new BlankPanel(imColor);
                String questionPosNeg = "";
                if(questionEnabled){
                    if(trialType == 1){
                        if(questionPointer >= questions.length) {
                            questionPointer = 0;
                            questions = data1.randomize(questions);
                        }
                        
                        initQuestion = questions[questionPointer].getParsedStringSpec(1);
                        questionPosNeg = questions[questionPointer].getParsedStringSpec(3);
                        handleLabel = questions[questionPointer].getParsedStringSpec(2);
                        questionAffectValue = questions[questionPointer].getParsedDoubleSpec(4);
                        questionPointer++;
                    }
                    else{
                        initQuestion = practiceQuestions[practiceQuestionPointer].getParsedStringSpec(1);
                        questionPosNeg = practiceQuestions[practiceQuestionPointer].getParsedStringSpec(3);
                        handleLabel = practiceQuestions[practiceQuestionPointer].getParsedStringSpec(2);
                        questionAffectValue = practiceQuestions[practiceQuestionPointer].getParsedDoubleSpec(4);
                        practiceQuestionPointer++;
                    }
                    
                    int fsize = dbfile1[12].getParsedIntSpec(5);
                    int questionWidth = dbfile1[12].getParsedIntSpec(6);
                    Color fontColor = new Color(dbfile1[12].getParsedIntSpec(2), dbfile1[12].getParsedIntSpec(3), dbfile1[12].getParsedIntSpec(4));
                    //int questionMargin = 200;
                    Font f = new Font(myFontName, Font.PLAIN, fsize);
                    Dimension d = frame.getSize();
                    questionPanel.setSize(d);
                    questionPanel.setLayout(new GridBagLayout());
                    
                    if(initQuestion.contains("#")){
                        StringBuilder sb = new StringBuilder();
                        sb.append(initQuestion.substring(0, initQuestion.indexOf('#')));
                        sb.append(randTarget.toString());
                        sb.append(initQuestion.substring(initQuestion.indexOf('#') + 1));
                        
                        initQuestion = sb.toString();
                    }
                    
                    String html1, html2;
                    html1 = "<html><body style='width:";
                    html2 = "px'><center>";
                    
                    JLabel qLabel = new JLabel(html1 + questionWidth + html2 + initQuestion);
                    qLabel.setFont(f);
                    qLabel.setForeground(fontColor);             
                    questionPanel.add(qLabel);
                }
                
                String unitLabel = stims[trialNum].getParsedStringSpec(28);
                int degrees = stims[trialNum].getParsedIntSpec(27);
                
                numLine = new NumberLine(randWidth, height, thickness, startUnit, endUnit,
                        randTarget, baseColor, dragColor, handleActiveColor, myFontName,
                        isEstimateTask, degrees, leftMargin, keepWithinBounds, leftOrRight,
                        showFullBaseScale, handleLabel, unitLabel); 
                
                //show question if enabled and gather appropriate response
                int mouseResponse = -1;
                long mouseRT = -1;
                String mouseResponseLabel = "";
                String mouseResponseCorrect = "";
                
                if(dbfile1[13].getParsedStringSpec(1).trim().equalsIgnoreCase("response") && questionEnabled){
                    imPanel.add(questionPanel, BorderLayout.CENTER);
                    frame.setContentPane(imPanel);
                    frame.validate();
                    frame.hideCursor();
 
                    resp.getTimedMouseClickResponse(questionPanel);
                    mouseResponse = resp.getMouseClickButton();
                    mouseRT = resp.getRT();
                    imPanel.remove(questionPanel);
                    
                    if(mouseResponse == 1){//left click set response label to appropriate value
                	mouseResponseLabel = leftValue;
		    } else if(mouseResponse == 3){//right click set response label to appropriate value
                        mouseResponseLabel = rightValue;
                    }

		    if(mouseResponseLabel.trim().equalsIgnoreCase(questionPosNeg.trim())) {
                        mouseResponseCorrect = "correct";
		    } else if(questionPosNeg.equals("neutral")) {
                        mouseResponseCorrect = "N/A";
		    } else {
                        mouseResponseCorrect = "incorrect";
                    }
		} else if(dbfile1[13].getParsedStringSpec(1).trim().equalsIgnoreCase("time") && questionEnabled){
                    String fixedOrWord = dbfile1[13].getParsedStringSpec(2).trim();
                    questionDelay = dbfile1[13].getParsedIntSpec(3);
                    int wordCount = 0;
                    if(fixedOrWord.equalsIgnoreCase("word")){
                        StringTokenizer tk = new StringTokenizer(initQuestion, " ");
                        while(tk.hasMoreTokens()){
                            String _temp = tk.nextToken();
                            wordCount ++;
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

                if(isEstimateTask && isMask){
                    imPanel.add(fixationPanel, BorderLayout.CENTER);
                    frame.setContentPane(imPanel);
                    frame.validate();
                    Experiment.delay(fixationTime);
                    //frame.setContentPane(startPanel);
                    //frame.validate();
                    //NumberLineExp.delay(stimMaskTime);
                    imPanel.remove(fixationPanel);
                }
                //end fixation
                
                //show numline and gather response
                UniversalNumberLine.presentTrial();
                
                if(!isEstimateTask){
                    frame.showCursor();
                    RT = resp.getTimedNumberLineResponse(numLine, useMouse); //Idle here until user has hit the space bar
                    frame.remove(imPanel); //remove the imPanel if necessary and add the feedback panel if animation is to be played
                    frame.setContentPane(endPanel);
                    userResp = df.format(numLine.getUnitLength());
                    userRespVal = userResp;
                    if(debug) {
                        System.out.println(userRespVal);
                    }
                    
                }else{
                    if(estimateTime > 0){
                        long start = new Date().getTime();
                        Experiment.delay(estimateTime);
                        frame.remove(imPanel); //remove the imPanel
                        frame.setContentPane(endPanel);
                        frame.validate();
                        RT = new Date().getTime() - start;
                    }else{
                        RT = resp.getTimedNumberLineResponse(numLine, useMouse);
                        frame.remove(imPanel); //remove the imPanel
                        frame.setContentPane(endPanel);
                        frame.validate();
                    }
                    if(isMask){
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
                    resp.getTimedNumPadResponse(frame, "What is the target of this number line?", estTargetFormat);
                    textRT = resp.getTextRT();
                    userResp = resp.getTextValue();
                    userRespVal = df.format(numLine.getUnitLength(userResp));
                }
                //frame.hideCursor();
                frame.remove(startPanel);
                frame.setContentPane(endPanel);
                frame.validate();
                
                Experiment.delay(1000);
                
                if("incorrect".equals(mouseResponseCorrect) && trialType == 0){
                    frame.showCursor();
                    reminderMessage(leftValue, rightValue, resp);
                    frame.hideCursor();
                }

                Feedback.setNextFeedback(trialType, trialNum, totalTrials);
                Feedback.loadFeedbackResource(getFeedbackResource());

                String bColorVal = "("+baseColor.getRed() + ", " + baseColor.getGreen() + ", " + baseColor.getBlue() + ")";
                String dColorVal = "("+dragColor.getRed() + ", " + dragColor.getGreen() + ", " + dragColor.getBlue() + ")";
                String hColorVal = "("+handleActiveColor.getRed() + ", " + handleActiveColor.getGreen() + ", " + handleActiveColor.getBlue() + ")";

                double numLineUnitErr = 0.0;
                if(!isEstimateTask)
                    numLineUnitErr = numLine.getUnitError(true);
                else
                    numLineUnitErr = numLine.getUnitError(true, userResp);

                // RT Pressure implementation
                String rtBeep = "N/A";
                int rtDeadline = 0;
                if (rtPressure != null && trialType == 1) { //Experiment trials only
                    rtPressure.add((int) RT, true);
                    rtPressure.checkResponseTime(rtFreq, rtDuration);
                    rtBeep = new Boolean(rtPressure.isRTBeep()).toString();
                    rtDeadline = rtPressure.getDeadline();
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
                outString.append(bColorVal).append("\t");
                outString.append(dColorVal).append("\t");
                outString.append(hColorVal).append("\t");
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
                outString.append(RT).append("\t");
                outString.append(textRT).append("\t");
                outString.append(isImageFeedback).append("\t");
                outString.append(aniOutputString).append("\t");
                outString.append(fractalOutputString).append("\t");
                outString.append(Feedback.getFeedbackType()).append("\t");
                if(questionEnabled){
                    outString.append(initQuestion);
                    outString.append("\t");
                    outString.append(questionPosNeg); //positive/negative question
                    outString.append("\t");
                    outString.append(questionAffectValue);
                    outString.append("\t");
                    if(questionDelay != -1){
                        outString.append(questionDelay);
                    }
                    else{
                        outString.append(mouseResponseLabel);
                        outString.append("\t");
                        outString.append(mouseRT);
                        outString.append("\t");
                        outString.append(mouseResponseCorrect);
                         outString.append("\t");
                    }   
                }
                
                if(rtPressure != null) {
                    outString.append("TRUE");
                    outString.append("\t");
                    outString.append(windowSize);
                    outString.append("\t");
                    outString.append(quantileThreshold);
                    outString.append("\t");
                    outString.append(errorRateThreshold);
                    outString.append("\t");
                    outString.append(rtBeep);
                    outString.append("\t");
                    outString.append(rtDeadline);
                } else {
                    outString.append("FALSE");
                    outString.append("\t");
                    outString.append("NA");
                    outString.append("\t");
                    outString.append("NA");
                    outString.append("\t");
                    outString.append("NA");
                    outString.append("\t");
                    outString.append(rtBeep);
                    outString.append("\t");
                    outString.append("NA");
                }
                
                String outStringTmp = outString.toString().replaceAll("true", "TRUE");
                outStringTmp = outStringTmp.replaceAll("false", "FALSE");

                data1.writeToURL(Experiment.getCGI(), dataFile, outStringTmp);
                outString.setLength(0); //Deallocates string builder

                frame.remove(endPanel);
                JPanel feedbackPanel = Feedback.getFeedbackPanel();
                if(feedbackPanel != null){
                    //Set the player on the experiment frame
                    frame.setContentPane(feedbackPanel);
                    frame.validate();
                    Feedback.playFeedback();  //Blocking call to ensure feedback is played through before continuing
                    frame.remove(feedbackPanel);
                    Feedback.freeFeedback();  //Frees memory used in feedback
                }
                frame.validate();
                frame.setContentPane(endPanel);
                if((trialNum + 1) % restNumber == 0) {
                    Experiment.rest();
                }
                imPanel.remove(leftMarginPanel);
                imPanel.remove(gridPanel);
                numLine = null;
                imPanel.removeAll();
            }//end inner for
        }//end outer for
        Experiment.thankYou();
        System.exit(0);
    }
    
    /**
     * Returns an instance of JPanel formatted to display the instruction file
     * @param u     URL to instruction file
     * @return      Formatted instructions on a JPanel
     */
    private JPanel getInstructionPanel(URL u){
        JPanel j = new JPanel();
        j.setBackground(Color.WHITE);       
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        j.setSize(d.width, d.height);
        j.setLocation(0,0);
        
        SpecificationArrayProcess sap = new SpecificationArrayProcess();
        Specification[] instructions = sap.readFromURL(u);
        
        String html1, html2;
        html1 = "<html><body style='width:";
        html2 = "px'><left>";
        
        String inst = html1 + 1000 + html2;
        for(int i = 0; i < instructions.length; i ++)
            inst += instructions[i].getAllSpecs();
        JLabel l = new JLabel();
        l.setForeground(Color.BLACK);
        l.setText(inst);
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
     * Returns a URL object containing the Feedback Resource's path
     * @return      URL object containing the feedback resource's path.
     */
    private static URL getFeedbackResource(){
        URL resource = null;
        String resourceName = Feedback.getCurrentResourceName();
        String feedbackType = Feedback.getFeedbackType();
        if(feedbackType.equals("ANIMATION"))
            resource = UniversalNumberLine.class.getResource("/resources/animations/"+resourceName);
        else if(feedbackType.equals("IMAGE"))
            resource = UniversalNumberLine.class.getResource("/resources/images/"+resourceName);
        return resource;
    }

    /**
     * Creates the OutputHeader for the data file
     * @param isQuestionEnabled     Does the experiment ask questions
     * @param questionResponse      Does the experiment has question responses
     * @return                      String of column headers
     */
    private static String createOutputHeader(boolean isQuestionEnabled, boolean questionResponse, boolean rtPressureEnabled){
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
        sbuf.append("textRT\t");
        sbuf.append("imgFeedback\t");
        sbuf.append("aniFeedback\t");
        sbuf.append("fractFeedback\t");
        sbuf.append("feedbackType\t");
        if(isQuestionEnabled){
            sbuf.append("question\t");
            sbuf.append("questionKey\t");
            sbuf.append("questionAffectValue\t");
            if(!questionResponse){
                sbuf.append("questionDisplay");
            }
            else{
                sbuf.append("mouseResponse\t");
                sbuf.append("mouseRT\t");
                sbuf.append("questionCorrect\t");
            }
        }
        
        
        sbuf.append("rtPressure\t");
        sbuf.append("windowSize\t");
        sbuf.append("quantileThres\t");
        sbuf.append("errorRateThres\t");
        sbuf.append("rtBeep\t");
        sbuf.append("rtDeadline");
        
        
        return sbuf.toString();
    }

    /**
     * Randomly generates a value for the left margin based on parameters
     * @param mLow          Low value for the range
     * @param mHigh         High value for the range
     * @param mInterval     Interval
     * @return              Random value
     */
    private static int getRandomLeftMargin(int mLow, int mHigh, int mInterval){
        randGen.setIntervalRange(mLow, mHigh, mInterval);
        return randGen.drawWithInterval();
    }

    private void getScreenEllispe(){
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension _d = tk.getScreenSize();
        int sHeight, sWidth;
        sHeight = _d.height;
        sWidth = _d.width;        
    }

    /**
     * Present the experiment trial
     */
    public static void presentTrial(){
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
     * Converts a Unit object into its reduced form
     * @param unitFormat    Format for the unit
     * @param unit          Unit value
     * @return              New unit object in the reduced form
     */
    private static Unit reduceUnit(String unitFormat, Unit unit){
        Unit reducedUnit = unit;
        if(unitFormat.matches("^.*[Ll][Cc][Dd]$")){
            String fractStr[] = new String[2];
            Fraction fract;
            if(unit.getType() == Unit.UNITTYPE.FRACT)
                fractStr = unit.getValue().split("/");
            else if(unit.getType() == Unit.UNITTYPE.ODDS)
                fractStr = unit.getValue().split("in");
            fract = new Fraction(Integer.parseInt(fractStr[0].trim()), Integer.parseInt(fractStr[1].trim()));

            String reducedFractStr = Fraction.reduceFract(fract).toString();
            if(unit.getType() == Unit.UNITTYPE.FRACT){
                reducedUnit = new Unit(reducedFractStr);
            }else if (unit.getType() == Unit.UNITTYPE.ODDS) {
                fractStr = reducedFractStr.split("/");
                reducedUnit = new Unit(fractStr[0].trim() + " in " + fractStr[1].trim());
            }
        }
        return reducedUnit;
    }

    private void getButtonMap(String b1, String b2, String b1Label, String b2Label, JFrame parentFrame) {
        // Create a dialog to prompt
        final JDialog dialog = new JDialog(parentFrame, true);

        JButton ok = new JButton("OK");
        ok.addActionListener(actionEvent -> {
            dialog.setVisible(false);
            dialog.dispose();
        });

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();

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
        topPanel.setBackground(new Color(200,200,200));
        topPanel.add(topLabel);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(new Color(200,200,200));
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
