package ccpl.lib;


import java.awt.AWTException;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class ExperimentV2 implements ExpInterface {

  /**
   * Added fields
   */
  private String DB_FILE_PATH;
  /**
   * End Added Fields
   */

  protected boolean WEB_ENABLED = false;
  public static final String WEB_APP_CODEBASE = "http://arcserv20.das.uncw.edu/apps/";
  private static String WEB_DIR = "";
  private static final String CGI_STR = "http://arcserv20.das.uncw.edu/apps/jws_lib/app_write.php";
  private static final String CGI_TRIAL_READER = "http://arcserv20.das.uncw.edu/apps/jws_lib/trial_file.php";
  protected static URL CGI = null;
  protected final String experiment;
  protected final String subject;
  protected final String condition;
  protected final String session;
  protected int totalTrials;
  protected int trialNum;
  protected int trialType;
  protected static ResponseV3 response = new ResponseV3();
  protected static DrawExpFrame frame;
  public String practiceTrialFile;
  public static String expTrialFile;
  public static String instructFile;
  public static String fontFile;
  protected static int restNumber;
  protected static Specification[] dbfile;
  protected static Specification[] stims;
  protected static Specification[] fonts;
  protected static SpecificationArrayProcess dataAP = new SpecificationArrayProcess();
  protected static final String WORKING_DIR = System.getProperty("user.dir");
  protected String codeBase;
  protected final String REL_DATA_FILE;
  protected final String DATA_FILE_NAME;
  protected final String INFILES_PATH;
  protected static FileInTextBox instruct;
  private static BlankPanel blankPanel = new BlankPanel();
  public static DecimalFormat deciFormat = new DecimalFormat("#.####");

  protected boolean randomizePractice;

  public ExperimentV2() {
    this("", "", "", "");
    DB_FILE_PATH = "";
  }

  public ExperimentV2(String exp, String sub, String cond, String sess) {
    this.codeBase = (new File(WORKING_DIR)).toURI().toString();
    this.subject = sub;
    this.condition = cond;
    this.session = sess;
    this.trialNum = 0;
    this.experiment = exp;
    frame = new DrawExpFrame(response);
    response.setFrame(frame);
    this.DATA_FILE_NAME = "/p" + this.subject + "s" + this.session + ".dat";
    this.REL_DATA_FILE = this.experiment + "/data/";
    this.INFILES_PATH = this.experiment + "/infiles/";
    DB_FILE_PATH = "";
  }

  public void hide() {
    frame.setVisible(false);
  }

  public void readDBFile(String dbfilePath) {
    dbfile = dataAP.readFromURL(this.getURL(this.INFILES_PATH + dbfilePath));
    randomizePractice = true;
  }

  public void readLocalDBFile(String dbfilePath) {
    dbfile = dataAP.readFromFile(this.INFILES_PATH + dbfilePath);
  }

  public URL getDBFile(String dbfilePath) {
    return this.getURL(this.INFILES_PATH + dbfilePath);
  }

  public String getInfilesPath() {
    return this.INFILES_PATH;
  }

  public URL getInstructionFile() {
    return this.getURL(this.INFILES_PATH + instructFile);
  }

  public URL getDataFile() {
    return this.getURL(this.REL_DATA_FILE + this.DATA_FILE_NAME);
  }

  public URL getFontFile() {
    return this.getURL(this.INFILES_PATH + fontFile);
  }

  public URL getPracticeFile() {
    return this.getURL(this.INFILES_PATH + practiceTrialFile);
  }

  public URL getExperimentFile() {
    return WEB_ENABLED?getURL("http://arcserv20.das.uncw.edu/apps/jws_lib/trial_file.php", "?app=" + WEB_DIR + "&" + "exp=" + this.experiment + "&" + "tf=" + expTrialFile):this.getURL(this.INFILES_PATH + expTrialFile);
  }

  public void setWebCodeBase(String cBase) {
    if(!cBase.isEmpty()) {
      this.codeBase = "http://arcserv20.das.uncw.edu/apps/" + cBase + "/";
      WEB_DIR = cBase;
    }

  }

  public static URL getCGI() {
    return CGI;
  }

  public void refreshTrials() {
    Specification[] tempSpecs = new Specification[stims.length - this.trialNum];
    System.arraycopy(stims, this.trialNum, tempSpecs, 0, tempSpecs.length);
    tempSpecs = dataAP.randomize(tempSpecs);
    System.arraycopy(tempSpecs, 0, stims, this.trialNum, tempSpecs.length);
    --this.trialNum;
  }

  public void setFullScreen() {
    if(System.getProperty("os.name").startsWith("Mac")) {
      // TODO: Figure out how to handle non-Mac machines
      //Application.getApplication().requestToggleFullScreen(frame);
    } else {
      frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
      frame.setVisible(true);
    }
  }

  public void setWebEnabled(boolean webEnable) {
    WEB_ENABLED = webEnable;
    if(webEnable) {
      try {
        CGI = new URL("http://arcserv20.das.uncw.edu/apps/jws_lib/app_write.php");
      } catch (MalformedURLException var3) {
        Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, var3);
      }
    } else {
      CGI = null;
    }

  }

  public static void resetMouseToCenterScreen() {
    try {
      Robot ex = new Robot();
      ex.mouseMove(DrawExpFrame.getScreenWidth() / 2, DrawExpFrame.getScreenHeight() / 2);
    } catch (AWTException e) {
      Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, e);
    }

  }

  public static void setMousePosition(int x, int y) {
    try {
      Robot ex = new Robot();
      ex.mouseMove(x, y);
    } catch (AWTException e) {
      Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  public boolean isWebEnabled() {
    return WEB_ENABLED;
  }

  public void presentStimulus(JFrame frame, Specification stim, Response resp) {
    BlankPanel endPanel = new BlankPanel(Color.black);
    BlankPanel startPanel = new BlankPanel(Color.black);
    DrawTrial trial = new DrawTrial(stim);
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gs = ge.getDefaultScreenDevice();
    if(gs.isFullScreenSupported()) {
      System.out.println("Full-screen mode is supported");
      gs.setFullScreenWindow(frame);
    } else {
      System.out.println("Full-screen mode will be simulated");
    }

    frame.setContentPane(startPanel);
    frame.setVisible(true);
    delay(500);
    trial.draw(startPanel);
    resp.getTimedKeyResponse();
    frame.setContentPane(endPanel);
    frame.setVisible(true);
    resp.checkResponseAccuracy(1);
    resp.giveFeedback(endPanel);
  }

  public static long delay(int milliseconds) {
    if(milliseconds <= 0) {
      return 0L;
    } else {
      long start = (new Date()).getTime();

      try {
        Thread.sleep((long)milliseconds);
      } catch (InterruptedException var4) {
        var4.printStackTrace();
        Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, var4);
      }

      return (new Date()).getTime() - start;
    }
  }

  public static void changeBackground(Color bg) {
    frame.getContentPane().setBackground(bg);
  }

  public static void pauseColor(int milliseconds, Color firstColor, Color secondColor) {
    frame.getContentPane().setBackground(firstColor);
    delay(milliseconds);
    frame.getContentPane().setBackground(secondColor);
  }

  public static void pauseBeep(int milliseconds) {
    delay(milliseconds);
    Toolkit.getDefaultToolkit().beep();
  }

  public void rest() {
    response.displayNotificationFrame(frame, "Please take a break.  Click the OK button to resume the experiment");
  }

  public static void thankYou() {
    response.displayNotificationFrame(frame, "THANK YOU for participating!  Click the OK button to end the experiment");
  }

  public void prepareToStart() {
    this.prepareToStartExperiment();
  }

  public void prepareToStartExperiment() {
    delay(200);
    response.displayNotificationFrame(frame, "Please Click OK to start the experiment");
  }

  public static void prepareToStartExperiment(JFrame parent) {
    delay(200);
    response.displayNotificationFrame(parent, "Please Click OK to start the experiment");
  }

  public void prepareToStartPractice() {
    delay(200);
    response.displayNotificationFrame(frame, "Please Click OK to start the practice trials");
  }

  public void prepareToStartPractice(JFrame parent) {
    delay(200);
    response.displayNotificationFrame(parent, "Please Click OK to start the practice trials");
  }

  public static boolean paramMatches(String param, String property) {
    boolean isMatch = false;
    if(param.trim().equalsIgnoreCase(property)) {
      isMatch = true;
    }

    return isMatch;
  }

  public static boolean isParamOn(String param) {
    boolean isOn = false;
    if(paramMatches(param, "on")) {
      isOn = true;
    }

    return isOn;
  }

  public URL getURL(String localFile) {
    return getURL(this.codeBase, localFile);
  }

  public static URL getURL(String aCodeBase, String localFile) {
    URL fileURL = null;

    try {
      fileURL = new URL(aCodeBase + localFile);
    } catch (MalformedURLException var4) {
      Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, var4);
    }

    return fileURL;
  }

  public void runExp() {
    this.setupExp();

    for(this.trialType = 0; this.trialType < 2; ++this.trialType) {
      if(this.trialType == 0 && practiceTrialFile.trim().toLowerCase().equals("none")) {
        this.trialType++;
      }

      if(this.trialType == 0) {
        stims = dataAP.readFromURL(this.getPracticeFile());

        if(randomizePractice) {
          stims = dataAP.randomize(stims);
        }

        frame.showCursor();
        prepareToStartPractice();
        frame.hideCursor();
      } else {
        stims = dataAP.readFromURL(this.getExperimentFile());
        stims = dataAP.randomize(stims);
        frame.showCursor();
        this.prepareToStartExperiment();
        frame.hideCursor();
      }

      this.totalTrials = stims.length;
      frame.requestFocus();

      for(this.trialNum = 0; this.trialNum < this.totalTrials; this.trialNum++) {
        this.readTrial();
        this.runTrial();
        System.out.format("Trial Number: %d\n", this.trialNum);
        if((this.trialNum + 1) % restNumber == 0 && this.trialNum != -1) {
          rest();
        }
      }
    }

    frame.showCursor();
  }

  public static void clearPanel(JPanel panel) {
    if(panel.getComponentCount() > 0) {
      panel.removeAll();
    }

  }

  public String createOutputHeader() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setupExp() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void runTrial() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void readTrial() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setInstruction(String instFile) {
    instructFile = instFile;
    instruct = new FileInTextBox(this.getURL(this.INFILES_PATH + instFile));
  }

  public static void showInstructions() {
    instruct.presentFile();
  }

  protected static void setButtonResponse(char sameChoiceKey, char diffChoiceKey) {
    response.setSameChoice(Character.toLowerCase(sameChoiceKey));
    response.setDiffChoice(Character.toLowerCase(diffChoiceKey));
  }

  public static void presentBlankScreen(int blankDelay) {
    setExpFrame(blankPanel);
    delay(blankDelay);
  }

  protected static void setExpFrame(JPanel stimPanel) {
    frame.setContentPane(stimPanel);
    frame.validate();
  }

  public static String getRandomFontName(String[] fontNames) {
    String myFontName;
    if(fontNames.length == 1) {
      myFontName = fontNames[0];
    } else {
      int tmp1 = (new RandomIntGenerator(0, fontNames.length - 1)).draw();
      myFontName = fontNames[tmp1];
    }

    return myFontName;
  }

  public class OutputBuilder {
    private StringBuilder sBuf = new StringBuilder();
    private String delim = "\t";

    public OutputBuilder() {
    }

    public <U> void append(U u) {
      this.sBuf.append(u).append(this.delim);
    }

    public <U> void appendFinal(U u) {
      this.sBuf.append(u);
    }

    public void clear() {
      this.sBuf.setLength(0);
    }

    public String toString() {
      return this.sBuf.toString();
    }
  }

  protected static enum TRANSITION_TYPE {
    FADE,
    BLOCK,
    ALL;

    private TRANSITION_TYPE() {
    }
  }

  protected static enum FRACTAL_TYPE {
    MANDELBROT,
    JULIA;

    private FRACTAL_TYPE() {
    }
  }

  /**
   * Added methods
   */


  public void readDBFile() {
    dbfile = dataAP.readFromURL(this.getURL(this.INFILES_PATH + DB_FILE_PATH));
  }

  public void setDbFilePath(String dbFilePath) {
    DB_FILE_PATH = dbFilePath;
  }

  protected void injectContentPanel(JPanel panel) {
    System.out.println("Injecting Panel");
    frame.setContentPane(panel);
    frame.validate();

    System.out.println("Panel inject");
    //frame.pack();
    //frame.setVisible(true);
  }

  /**
   * End added methods
   */
}
