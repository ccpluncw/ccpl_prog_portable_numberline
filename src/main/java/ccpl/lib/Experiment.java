package ccpl.lib;

import java.awt.AWTException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;


public class Experiment implements ExpInterface {
  private final URL CGI = null;

  protected final String experiment;
  protected final String subject;
  protected final String condition;
  protected final String session;

  protected int totalTrials;
  protected int trialNum;
  protected int trialType;

  protected final Response response;
  private DrawExpFrame frame;

  public String practiceTrialFile;
  public String expTrialFile;
  public String instructFile;
  public String fontFile;

  protected int restNumber;

  protected Specification[] dbfile;
  protected Specification[] stims;
  protected Specification[] fonts;

  protected final SpecificationArrayProcess dataAP = new SpecificationArrayProcess();
  private final String WORKING_DIR = System.getProperty("user.dir");

  private final String codeBase = new File(WORKING_DIR).toURI().toString();

  private final String REL_DATA_FILE;
  private final String DATA_FILE_NAME;
  private final String INFILES_PATH;

  private FileInTextBox instruct;

  private final BlankPanel blankPanel = new BlankPanel();

  public Experiment(String exp, String sub, String cond, String sess) {
    this.subject = sub;
    this.condition = cond;
    this.session = sess;
    this.trialNum = 0;
    this.experiment = exp;

    this.response = new Response();

    this.frame = new DrawExpFrame(response);

    this.response.setFrame(frame);

    this.DATA_FILE_NAME = "/p" + subject + "s" + session + ".dat";
    this.REL_DATA_FILE = experiment + "/data/";
    this.INFILES_PATH = "exp/infiles/";
  }

  public String getInfilesPath() {
    return INFILES_PATH;
  }

  public URL getInstructionFile() {
    return this.getClass().getClassLoader().getResource(INFILES_PATH + instructFile);
  }

  public URL getDataFile() {
    // TODO: Change this to allow a user to select where to save the file.
    try {
      return new URL("file:/home/aray/temp/cohen" + DATA_FILE_NAME);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public URL getFontFile() {
    return getClass().getClassLoader().getResource(INFILES_PATH + fontFile);
  }

  public URL getPracticeFile() {
    return this.getClass().getClassLoader().getResource(INFILES_PATH + practiceTrialFile);
  }

  public URL getExperimentFile() {
    return this.getClass().getClassLoader().getResource(INFILES_PATH + expTrialFile);
  }

  public URL getCGI() {
    return CGI;
  }


  public void setFullScreen() {
    if (System.getProperty("os.name").startsWith("Mac")) {
      // TODO: Figure out how to handle this on non-Mac machines.
      // com.apple.eawt.Application.getApplication().requestToggleFullScreen(frame);
    }//make mac go full screen
  }

  public void delay(int milliseconds) {
    if (milliseconds <= 0) {
      return;
    }

    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException ex) {
      Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void rest() {
    response.displayNotificationFrame(frame, "Please take a break.  Click the OK button to resume the experiment");
  }

  public void thankYou() {
    response.displayNotificationFrame(frame, "THANK YOU for participating!  Click the OK button to end the experiment");
  }


  public void prepareToStartExperiment(JFrame parent) {
    delay(200);
    response.displayNotificationFrame(parent, "Please Click OK to start the experiment");
  }

  public void prepareToStartPractice(JFrame parent) {
    delay(200);
    response.displayNotificationFrame(parent, "Please Click OK to start the practice trials");
  }

  public boolean paramMatches(String param, String property) {
    boolean isMatch = false;
    if ((param.trim()).equalsIgnoreCase(property)) {
      isMatch = true;
    }
    return isMatch;
  }

  public boolean isParamOn(String param) {
    boolean isOn = false;
    if (paramMatches(param, "on")) {
      isOn = true;
    }
    return isOn;
  }

  public URL getURL(String localFile) {
    return getURL(codeBase, localFile);
  }

  public URL getURL(String codeBase, String localFile) {
    URL fileURL = null;

    try {
      fileURL = new URL(codeBase + localFile);
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


  public void presentBlankScreen(int blankDelay) {
    setExpFrame();
    delay(blankDelay);
  }

  protected void setExpFrame() {
    frame.setContentPane(blankPanel);
    frame.validate();
  }

  public String getRandomFontName(String[] fontNames) {
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
}
