package ccpl.lib;

import java.awt.AWTException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;


public class Experiment implements ExpInterface {
  protected static boolean WEB_ENABLED = false;
  public final static String WEB_APP_CODEBASE = "http://arcserv20.das.uncw.edu/apps/";
  private static String WEB_DIR = "";
  //protected final static String WEB_APP_CODEBASE = "http://localhost/apps/";

  private final static String CGI_TRIAL_READER = WEB_APP_CODEBASE + "jws_lib/trial_file.php";
  protected static URL CGI = null;

  protected final String experiment, subject, condition, session;
  protected int totalTrials, trialNum, trialType;
  protected static final Response response = new Response();
  protected static DrawExpFrame frame;

  public static String practiceTrialFile, expTrialFile, instructFile, fontFile;
  protected static int restNumber;

  protected static Specification[] dbfile, stims, fonts;
  protected static final SpecificationArrayProcess dataAP = new SpecificationArrayProcess();
  protected static final String WORKING_DIR = System.getProperty("user.dir");

  protected String codeBase = new File(WORKING_DIR).toURI().toString();

  protected final String REL_DATA_FILE;
  protected final String DATA_FILE_NAME;
  protected final String INFILES_PATH;

  protected static FileInTextBox instruct;

  private static final BlankPanel blankPanel = new BlankPanel();


  public Experiment() {
    this("", "", "", "");
  }

  public Experiment(String exp, String sub, String cond, String sess, DrawExpFrame frame) {
    subject = sub;
    condition = cond;
    session = sess;
    experiment = exp;
    Experiment.frame = frame;
    response.setFrame(frame);
    DATA_FILE_NAME = "/p" + subject + "s" + session + ".dat";
    REL_DATA_FILE = experiment + "/data/";
    INFILES_PATH = "exp/infiles/";
  }

  public Experiment(String exp, String sub, String cond, String sess) {
    subject = sub;
    condition = cond;
    session = sess;
    trialNum = 0;
    experiment = exp;
    frame = new DrawExpFrame(response);
    response.setFrame(frame);
    DATA_FILE_NAME = "/p" + subject + "s" + session + ".dat";
    REL_DATA_FILE = experiment + "/data/";
    INFILES_PATH = "exp/infiles/";
  }

  public String getInfilesPath() {
    return INFILES_PATH;
  }

  public URL getInstructionFile() {
    ClassLoader cl = this.getClass().getClassLoader();
    return cl.getResource(INFILES_PATH + instructFile);
  }

  public URL getDataFile() {
    // TODO: CHANGE THIS
    //return getURL(REL_DATA_FILE + DATA_FILE_NAME);

    //return getURL("/home/aray/temp/cohen/" + DATA_FILE_NAME);

    try {
      return new URL("file:/home/aray/temp/cohen" + DATA_FILE_NAME);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public URL getFontFile() {
    ClassLoader cl = getClass().getClassLoader();
    return cl.getResource(INFILES_PATH + fontFile);
    //return getURL(INFILES_PATH + fontFile);
  }

  public URL getPracticeFile() {
    ClassLoader cl = this.getClass().getClassLoader();
    return cl.getResource(INFILES_PATH + practiceTrialFile);
  }

  public URL getExperimentFile() {
    if (WEB_ENABLED) {
      return getURL(CGI_TRIAL_READER,
          ("?app=" + WEB_DIR + "&"
              + "exp=" + experiment + "&"
              + "tf=" + expTrialFile));
    } else {
      return this.getClass().getClassLoader().getResource(INFILES_PATH + expTrialFile);
    }
  }

  public static URL getCGI() {
    return CGI;
  }


  public void setFullScreen() {
    if (System.getProperty("os.name").startsWith("Mac")) {
      // TODO: Figure out how to handle this on non-Mac machines.
      // com.apple.eawt.Application.getApplication().requestToggleFullScreen(frame);
    }//make mac go full screen
  }


  public static void resetMouseToCenterScreen() {
    try {
      java.awt.Robot rob = new java.awt.Robot();
      rob.mouseMove(DrawExpFrame.getScreenWidth() / 2, DrawExpFrame.getScreenHeight() / 2);
    } catch (AWTException ex) {
      Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
    }
  }


  public static void delay(int milliseconds) {
    if (milliseconds <= 0) {
      return;
    }

    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException ex) {
      Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static void rest() {
    response.displayNotificationFrame(frame, "Please take a break.  Click the OK button to resume the experiment");
//            JOptionPane.showMessageDialog(null, "Please take a break.  Click the OK button to resume the experiment", "information", JOptionPane.INFORMATION_MESSAGE);
  }

  public static void thankYou() {
    response.displayNotificationFrame(frame, "THANK YOU for participating!  Click the OK button to end the experiment");
//            JOptionPane.showMessageDialog(null, "THANK YOU for participating!  Click the OK button to end the experiment", "information", JOptionPane.INFORMATION_MESSAGE);
  }


  public static void prepareToStartExperiment(JFrame parent) {
    delay(200);
    response.displayNotificationFrame(parent, "Please Click OK to start the experiment");
  }

  public static void prepareToStartPractice(JFrame parent) {
    delay(200);
    response.displayNotificationFrame(parent, "Please Click OK to start the practice trials");
  }

  public static boolean paramMatches(String param, String property) {
    boolean isMatch = false;
    if ((param.trim()).equalsIgnoreCase(property)) {
      isMatch = true;
    }
    return isMatch;
  }

  public static boolean isParamOn(String param) {
    boolean isOn = false;
    if (paramMatches(param, "on")) {
      isOn = true;
    }
    return isOn;
  }

  public URL getURL(String localFile) {
    return getURL(codeBase, localFile);
  }

  public static URL getURL(String aCodeBase, String localFile) {
    URL fileURL = null;
    try {
      fileURL = new URL(aCodeBase + localFile);
    } catch (MalformedURLException ex) {
      Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
    }
    return fileURL;
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
    instruct = new FileInTextBox(getURL(INFILES_PATH + instFile));
  }


  public static void presentBlankScreen(int blankDelay) {
    setExpFrame();
    delay(blankDelay);
  }

  protected static void setExpFrame() {
    frame.setContentPane(Experiment.blankPanel);
    frame.validate();
  }

  public static String getRandomFontName(String[] fontNames) {
    String myFontName;
    int tmp1;
    if (fontNames.length == 1) {
      myFontName = fontNames[0];
    } else {
      tmp1 = new RandomIntGenerator(0, fontNames.length - 1).draw();
      myFontName = fontNames[tmp1];
    }
    return (myFontName);
  }

  public DrawExpFrame getFrame() {
    return frame;
  }

  public Response getResponse() {
    return response;
  }
}
