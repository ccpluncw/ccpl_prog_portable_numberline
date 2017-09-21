package ccpl.lib;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class Experiment implements ExpInterface {
    protected static boolean WEB_ENABLED = false;
    public final static String WEB_APP_CODEBASE = "http://arcserv20.das.uncw.edu/apps/";
    private static String WEB_DIR = "";
    //protected final static String WEB_APP_CODEBASE = "http://localhost/apps/";
    
    private final static String CGI_STR = WEB_APP_CODEBASE + "jws_lib/app_write.php";
    private final static String CGI_TRIAL_READER = WEB_APP_CODEBASE + "jws_lib/trial_file.php";
    protected static URL CGI = null;

    protected final String experiment, subject, condition, session;
    protected int totalTrials, trialNum, trialType;
    protected static Response response = new Response();
    protected static DrawExpFrame frame;
    
    public static String practiceTrialFile, expTrialFile, instructFile, fontFile;
    protected static int restNumber;

    protected static Specification[] dbfile, stims, fonts;
    protected static SpecificationArrayProcess dataAP = new SpecificationArrayProcess();
    protected static final String WORKING_DIR = System.getProperty("user.dir");

    protected String codeBase = new File(WORKING_DIR).toURI().toString();

    protected static enum FRACTAL_TYPE { MANDELBROT, JULIA }
    protected static enum TRANSITION_TYPE { FADE, BLOCK, ALL }

    protected final String REL_DATA_FILE;
    protected final String DATA_FILE_NAME;
    protected final String INFILES_PATH;

    protected static FileInTextBox instruct;

    private static BlankPanel blankPanel = new BlankPanel();
    public static DecimalFormat deciFormat = new DecimalFormat("#.####");


    public Experiment(){
        this("","","","");
    }

    public Experiment(String exp, String sub, String cond, String sess, DrawExpFrame frame) {
        subject = sub;
        condition = cond;
        session = sess;
        experiment = exp;
        this.frame = frame;
        response.setFrame(frame);
        DATA_FILE_NAME = "/p" + subject + "s" + session + ".dat";
        REL_DATA_FILE = experiment + "/data/";
        INFILES_PATH = experiment + "/infiles/";
    }
    
    public Experiment(String exp, String sub, String cond, String sess){
        subject = sub;
        condition = cond;
        session = sess;
        trialNum = 0;
        experiment = exp;
        frame = new DrawExpFrame(response);
        response.setFrame(frame);
        DATA_FILE_NAME = "/p" + subject + "s" + session + ".dat";
        REL_DATA_FILE = experiment + "/data/";
        INFILES_PATH = experiment + "/infiles/";
    }
    
    public void hide()
    {
        frame.setVisible(false);
    }

    public void readDBFile(String dbfilePath){
        dbfile = dataAP.readFromURL(getURL(INFILES_PATH + dbfilePath));
    }
    public void readLocalDBFile(String dbfilePath){
        dbfile = dataAP.readFromFile(INFILES_PATH + dbfilePath);
    }
    public URL getDBFile(String dbfilePath){
        return getURL(INFILES_PATH + dbfilePath);
    }
    
    public String getInfilesPath(){
        return INFILES_PATH;
    }
    
    public URL getInstructionFile(){
        return getURL(INFILES_PATH + instructFile);
    }

    public URL getDataFile(){
        return getURL(REL_DATA_FILE + DATA_FILE_NAME);
    }

    public URL getFontFile(){
        return getURL(INFILES_PATH + fontFile);
    }

    public URL getPracticeFile(){
        return getURL(INFILES_PATH + practiceTrialFile);
    }

    public URL getExperimentFile(){
        if(WEB_ENABLED){
            return getURL(CGI_TRIAL_READER,
                            ("?app=" + WEB_DIR + "&"
                            + "exp=" + experiment + "&"
                            + "tf=" + expTrialFile));
        }else
            return getURL(INFILES_PATH + expTrialFile);
    }

    public void setWebCodeBase(String cBase){
        if(!cBase.isEmpty()){
            codeBase = WEB_APP_CODEBASE + cBase + "/";
            WEB_DIR = cBase;
        }
    }

    public static URL getCGI(){
        return CGI;
    }
    
    
    //
    //This method will refresh the remaining trials of the experiment and
    //shuffle them around.
    //trial number is decremented to include trial which the refresh 
    //was called.
    //
    public void refreshTrials(){
        Specification[] tempSpecs = new Specification[stims.length - trialNum];
        //copy specs to temp array
        System.arraycopy(stims, trialNum, tempSpecs, 0, tempSpecs.length);
        
        //shuffle around
        tempSpecs = dataAP.randomize(tempSpecs);
        
        //copy back over
        System.arraycopy(tempSpecs, 0, stims, trialNum, tempSpecs.length);
        
        //decrement trial
        trialNum -- ;
    }
    
    public void setFullScreen(){
        if(System.getProperty("os.name").startsWith("Mac")){
          // TODO: Figure out how to handle this on non-Mac machines.
            // com.apple.eawt.Application.getApplication().requestToggleFullScreen(frame);
        }//make mac go full screen
    }
    
    
    public void setWebEnabled(boolean webEnable){
        WEB_ENABLED = webEnable;
        if(webEnable == true){
            try {
                CGI = new URL(CGI_STR);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            CGI = null;
        }
    }
    
    public static void resetMouseToCenterScreen(){
        try { 
            java.awt.Robot rob = new java.awt.Robot(); 
            rob.mouseMove(DrawExpFrame.getScreenWidth()/2, DrawExpFrame.getScreenHeight()/2); 
        } catch (AWTException ex) { 
            Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }

    public boolean isWebEnabled(){
        return WEB_ENABLED;
    }
/*** Use the following method to specify the trial structure of the experiment.
**** Pass it the JFRAME that you want to use and the Stimuli and Response objects.
***/
    public void presentStimulus (JFrame frame, Specification stim, Response resp) {
	BlankPanel endPanel = new BlankPanel (Color.black);
	BlankPanel startPanel = new BlankPanel (Color.black);
	DrawTrial trial = new DrawTrial (stim);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        if (gs.isFullScreenSupported()) {
            System.out.println("Full-screen mode is supported");
            gs.setFullScreenWindow(frame);
        } else {
             System.out.println("Full-screen mode will be simulated");
        }
             frame.setContentPane (startPanel);
			
/***	    one must set the frame to visible BEFORE one draws in the frame with 
****      a class that does not use PaintComponent.  If one does not set the 
****      frame to visible first, then a null pointer exception will result for
****      the Graphics object.
***/
             frame.setVisible(true);
			
/***      one must have the following delay (minimum of 100 ms) to       ***
****      allow the screen to be drawn and then the first trial to       ***
****			show.  If one does not have this delay, then the first trial   *** 
****			will not be shown.                                             ***
***/
            delay (500);
            trial.draw (startPanel);

            resp.getTimedKeyResponse ();
	
/***			next to clear the screen, set a new content pane and make it visible.
***/


            frame.setContentPane (endPanel);
            frame.setVisible(true);

/***			if you want a button response, set a DELAY before you clear the screen for 
**** 			how long you want the SOA and then insert the following below:
****
****			Object [] buttonLabels = { "Same", "Different" };
****			resp.getTimedTwoButtonResponse (buttonLabels);
***/

/***			Below we just check the accuracy and give feedback
***/

            resp.checkResponseAccuracy (1);
            resp.giveFeedback (endPanel);

      }
		

      public static long delay(int milliseconds) {
            if(milliseconds <= 0)
                return 0;
            long start = new Date().getTime();
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException ex) {
                Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
            }
            return ((new Date().getTime()) - start);
      }
	
      public static void changeBackground(Color bg)
      {
          frame.getContentPane().setBackground(bg);
      }
      
      public static void pauseColor(int milliseconds, Color firstColor, Color secondColor)
      {
          frame.getContentPane().setBackground(firstColor);
          delay(milliseconds);
          frame.getContentPane().setBackground(secondColor);
      }
      
      public static void pauseBeep(int milliseconds)
      {
          delay(milliseconds);
          java.awt.Toolkit.getDefaultToolkit().beep();
      }
      
        public static void rest () {
            response.displayNotificationFrame(frame, "Please take a break.  Click the OK button to resume the experiment");
//            JOptionPane.showMessageDialog(null, "Please take a break.  Click the OK button to resume the experiment", "information", JOptionPane.INFORMATION_MESSAGE);
        }

        public static void thankYou () {
            response.displayNotificationFrame(frame, "THANK YOU for participating!  Click the OK button to end the experiment");
//            JOptionPane.showMessageDialog(null, "THANK YOU for participating!  Click the OK button to end the experiment", "information", JOptionPane.INFORMATION_MESSAGE);
	}


	public void prepareToStart () {
            prepareToStartExperiment();
	}


	public void prepareToStartExperiment () {
            delay (200);
            response.displayNotificationFrame(frame, "Please Click OK to start the experiment");

//            JOptionPane.showMessageDialog(null, "Please Click OK to start the experiment", "information", JOptionPane.INFORMATION_MESSAGE);
	}
        
        public static void prepareToStartExperiment(JFrame parent){
            delay (200);
            response.displayNotificationFrame(parent, "Please Click OK to start the experiment");
        }

	public void prepareToStartPractice () {
            delay (200);
            response.displayNotificationFrame(frame, "Please Click OK to start the practice trials");
            //JOptionPane.showMessageDialog(null, "Please Click OK to start the practice trials", "information", JOptionPane.INFORMATION_MESSAGE);
	
        }
        
        public static void prepareToStartPractice(JFrame parent){
            delay (200);
            response.displayNotificationFrame(parent, "Please Click OK to start the practice trials");
        }

        public static boolean paramMatches(String param, String property){
            boolean isMatch = false;
            if((param.trim()).equalsIgnoreCase(property)){
               isMatch = true;
            }
            return isMatch;
        }
        
        public static boolean isParamOn(String param){
            boolean isOn = false;
            if(paramMatches(param, "on"))
                isOn = true;
            return isOn;
        }

        public URL getURL(String localFile){
            return getURL(codeBase, localFile);
        }

        public static URL getURL(String aCodeBase, String localFile){
            URL fileURL = null;
            try {
                fileURL = new URL(aCodeBase + localFile);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
            }
            return fileURL;
        }
        
    public void runExp(){
        setupExp();
        for (trialType = 0; trialType < 2; ++trialType) {
            if(trialType == 0 && practiceTrialFile.trim().toLowerCase().equals("none")) {
                trialType++;
            }
            if (trialType == 0) {
                //stims = dataAP.readFromURL(getURL(INFILES_PATH + practiceTrialFile));
                stims = dataAP.readFromURL(getPracticeFile());
                stims = dataAP.randomize(stims);
                frame.showCursor();
                prepareToStartPractice();
                frame.hideCursor();
            } else {
                //stims = dataAP.readFromURL(getURL(INFILES_PATH + expTrialFile));
                stims = dataAP.readFromURL(getExperimentFile());
                stims = dataAP.randomize(stims);
                frame.showCursor();
                prepareToStartExperiment();
                frame.hideCursor();
            }
            totalTrials = stims.length;
            frame.requestFocus();
            for (trialNum = 0; trialNum < totalTrials; ++trialNum) {
                readTrial();
                runTrial();
                System.out.println(trialNum);
                if(((trialNum + 1) % restNumber == 0) && trialNum != -1) {
                    Experiment.rest();
                }
            }
        }
        frame.showCursor();
    }

    public static void clearPanel(JPanel panel){
        if(panel.getComponentCount() > 0)
            panel.removeAll();
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

    public void setInstruction(String instFile){
        instructFile = instFile;
        instruct = new FileInTextBox(getURL(INFILES_PATH + instFile));
    }
    

    public static void showInstructions(){
        instruct.presentFile();
    }
    
    protected static void setButtonResponse(char sameChoiceKey, char diffChoiceKey){
        response.setSameChoice(Character.toLowerCase(sameChoiceKey));
        response.setDiffChoice(Character.toLowerCase(diffChoiceKey));
    }
    
    public class OutputBuilder {
        private StringBuilder sBuf;
        private String delim = "\t";
        
        public OutputBuilder(){
            sBuf = new StringBuilder();
        }
        
        public <U> void append(U u){
            sBuf.append(u).append(delim);
        }
        
        /**
         * Appends the final piece of information.
         * Difference between append and appendFinal is appendFinal does not
         * put append the delimiter. The delimiter causes issues sometimes when
         * importing trial data into Excel by including an empty column. 
         * Added to rectify aforementioned issue in StdDistWordMod.
         * @param <U>   Generic type
         * @param u     Generic object that will be appended to the final.
         */
        public <U> void appendFinal(U u) {
            sBuf.append(u);
        }
        
        public void clear(){
            sBuf.setLength(0);
        }
        @Override
        public String toString(){
            return sBuf.toString();
        }
    }

    public static void presentBlankScreen(int blankDelay){
        setExpFrame(blankPanel);
        delay(blankDelay);
    }

    protected static void setExpFrame(JPanel stimPanel){
        frame.setContentPane(stimPanel);
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
