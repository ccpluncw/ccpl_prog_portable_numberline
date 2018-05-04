package ccpl.lib;

import javax.swing.JFrame;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Experiment implements ExpInterface {

  protected final String experiment;
  protected final String subject;
  protected final String condition;
  protected final String session;

  protected int trialNum;
  protected int trialType;

  protected final Response response;
  private final DrawExpFrame frame;

  protected String instructFile;
  protected String fontFile;

  protected int restNumber;

  protected Specification[] fonts;

  protected final SpecificationArrayProcess dataAp = new SpecificationArrayProcess();

  private final String relativeDataDirectory;
  private final String dataFileName;
  private final String infilesPath;

  private final BlankPanel blankPanel = new BlankPanel();

  protected Experiment(String exp, String sub, String cond, String sess, String saveDirectory) {
    this.subject = sub;
    this.condition = cond;
    this.session = sess;
    this.trialNum = 0;
    this.experiment = exp;

    this.response = new Response();

    this.frame = new DrawExpFrame(response);

    this.response.setFrame(frame);

    this.dataFileName = "/p" + subject + "s" + session + ".dat";
    this.relativeDataDirectory = saveDirectory;
    this.infilesPath = "exp/infiles/";
  }

  protected URL getInstructionFile() {
    return this.getClass().getClassLoader().getResource(infilesPath + instructFile);
  }

  protected URL getDataFile() {
    // TODO: Change this to allow a user to select where to save the file.
    try {
      return new URL("file://" + relativeDataDirectory + dataFileName);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    return null;
  }

  protected URL getFontFile() {
    return getClass().getClassLoader().getResource(infilesPath + fontFile);
  }

  protected void setFullScreen() {
    // Make Mac go full screen
    if (System.getProperty("os.name").startsWith("Mac")) {
      // com.apple.eawt.Application.getApplication().requestToggleFullScreen(frame);
      try {
        Class appClass = Class.forName("com.apple.eawt.Application");
        Class params[] = new Class[]{};

        Method getApp = appClass.getMethod("getApplication", params);
        Object app = getApp.invoke(appClass);
        Method toggle = app.getClass().getMethod("requestToggleFullScreen", Window.class);

        toggle.invoke(app, frame);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException
          | ClassNotFoundException e) {
        Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, e.getLocalizedMessage());
      }
    }
  }

  protected void delay(int milliseconds) {
    if (milliseconds <= 0) {
      return;
    }

    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException ex) {
      Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  protected void rest() {
    response.displayNotificationFrame(frame,
        "Please take a break.  Click the OK button to resume the experiment");
  }

  protected void thankYou() {
    response.displayNotificationFrame(frame,
        "THANK YOU for participating!  Click the OK button to end the experiment");
  }


  protected void prepareToStartExperiment(JFrame parent) {
    delay(200);
    response.displayNotificationFrame(parent, "Please Click OK to start the experiment");
  }

  protected void prepareToStartPractice(JFrame parent) {
    delay(200);
    response.displayNotificationFrame(parent, "Please Click OK to start the practice trials");
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


  protected void presentBlankScreen(int blankDelay) {
    setExpFrame();
    delay(blankDelay);
  }

  private void setExpFrame() {
    frame.setContentPane(blankPanel);
    frame.validate();
  }

  protected String getRandomFontName(String[] fontNames) {
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

  protected DrawExpFrame getFrame() {
    return frame;
  }
}
