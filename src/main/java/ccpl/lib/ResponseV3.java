package ccpl.lib;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

/*****	The RESPONSE class is very important.  It specifies methods for collecting
 ******	and checking responses.  It implements KEYLISTENER so that keyboard responses
 ******	can be collected.  The class must be added to the panel as the keyListener
 ******	(i.e., addKeyListener (ResponseV3)
 ****** Before any timing routine is run, testTimer () must be run once
 *****/
public class ResponseV3 extends Response implements KeyListener, ActionListener, ChangeListener {

    private static final int leftClick = 0;
    private static final int rightClick = 3;//mouse button codes for left and right click
    public static final int sameProbe = 0;
    public static final int diffProbe = 1;
    private static int maxRespTime = -1;
    private AudioClip mediaPlayer;
    private boolean hapticFeedback = false;
    private int tone, ms;
    private static boolean logSwitch = false;
    private static PrintWriter pw;
    private static FileWriter fw;
    
    KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    
    
    public AbstractAction returnAction(){
        return new ResponseAction();
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    /**
     * this is used to enable logging
     * log statements will only trigger if this method is called
     * if there is an error opening/creating the log file the experiment will close.
     * @param filename 
     *  the log file name
     */
    public void setLogFile(String filename){
        try{
        fw = new FileWriter(filename, true);
        pw = new PrintWriter(fw);
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "ERROR CONFIGURING LOG. FATAL");
            System.exit(0);
        }
        logSwitch = true;
    }
    
    /**
     * closes the log
     */
    public void closeLog(){
        if(logSwitch){
            pw.flush();
            pw.close();
        }
    }
    
    public AbstractAction returnMouseAction(){
        return new MouseAction();
    }
    
    private class MouseAction extends AbstractAction{
        @Override
        public void actionPerformed(ActionEvent e){
            if(mouseFrame != null){
                if(mouseEnabled)
                    disableMouse(mouseFrame);
                else
                    enableMouse(mouseFrame);
            }
        }
    }
    
    private class ResponseAction extends AbstractAction{
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            char responseKey;
            
            if(command != null)
                responseKey = command.charAt(0);
            else
                responseKey = '~';
            
            if(isMultipleButton){
                userChoice = responseKey;
                responseList.add(userChoice);
                currentRespSize ++;
                
                if(hapticFeedback){
                    try {
                        //mediaPlayer.run();//check this
                        AudioClip.beep(tone, ms);
                    } catch (Exception ex) {
                        Logger.getLogger(ResponseV3.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                //resetUserChoice();
            
            }
        
            else{
                userChoice = responseKey;
            }
            keyboardInputDone = true;
        }
        
    }
    public ResponseV3() {
        numPadResponse = null;
    }

    public ResponseV3(char a, char b) {
        this();
        sameChoice = a;
        diffChoice = b;
    }
    
    public void setFrame(JFrame f){
        mouseFrame = f;
    }
    
    public void enableHapticFeedback(int x, int y){
        //mediaPlayer = new AudioClip(hapticFeedbackFile);
        tone = x;
        ms = y;
        hapticFeedback = true;
    }

    public boolean isFocusTraversable() {
        return true;
    }
 
    /**
     * When a button is hit this is triggered
     * saves the button that was hit
     * and deals with it depending on the response mode
     * @param evt 
     */
    public void keyPressed(KeyEvent evt) {
        if(logSwitch){
            pw.print("KEY PRESSED -> ");
            pw.println(evt.getKeyChar());
            pw.print("KEY CODE -> ");
            pw.println(evt.getKeyCode());
            pw.print("KEY MODIFIER -> ");
            pw.println(evt.getModifiersEx());
            pw.flush();
        }
        
        try{
        keyCode = evt.getKeyCode();
        int sameCode = KeyStroke.getKeyStroke(sameChoice).getKeyCode();
        int diffCode = KeyStroke.getKeyStroke(diffChoice).getKeyCode();

            if(evt.isControlDown() && (evt.getKeyChar() + "").toLowerCase().charAt(0) == 'q'){
                System.exit(0);
            }
            else if(isMultipleButton){
                    userChoice = (evt.getKeyChar() + "").toLowerCase().charAt(0);
                    responseList.add(userChoice);
                    currentRespSize ++;
                    if(hapticFeedback){
                        try {
                            AudioClip.beep(tone, ms);
                        } catch (Exception ex) {
                            Logger.getLogger(ResponseV3.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }//end haptic
                    resetUserChoice();
            }//end multipleButton

            else{
                 userChoice = (evt.getKeyChar() + "").toLowerCase().charAt(0);
            }//catchall else
            keyboardInputDone = true;
        }catch(Exception e){
            if(logSwitch){
                pw.print("ERROR IN KEY LISTENER ##10001 -> ");
                pw.println(e.toString());
                pw.flush();
            }
        }
            
    }
    public void keyReleased(KeyEvent evt) {//do nothing
    }
    public void keyTyped(KeyEvent evt) {//do nothing
    }

    /**
     * YOU HAVE TO CALL THIS TO MAKE THE RESPONSE CLASS WORK
     * Calibrates timing to ensure accuracy
     * 
     * @param inputPanel
     *  panel this method will draw in (its just going to indicate the timer is being calibrated)
     * @param backColor
     *  background color of the panel
     * @param textColor
     *  text color for the panel
     * @param reps
     *  how many reps the timing calibrator will iterate through (normally 1000)
     * @return 
     */
    public synchronized long testTimer(BlankPanel inputPanel, Color backColor, Color textColor, int reps) {
        long timeA, timeB;
        int i;
        long devisor, overRun;
        long[] testLoop = new long[reps];
        long tmpTot = 0;

        // reset the user choice so that the polling call below will work.
        Font myFont = new Font("SansSerif", Font.BOLD, 24);
        Dimension d1 = inputPanel.getSize();
        int dotAreaHeight = d1.height;
        int dotAreaWidth = d1.width;

        JLabel timerLabel = new JLabel("Please wait while the timer is calibrated.");
        inputPanel.setLayout(null);
        timerLabel.setForeground(textColor);
        timerLabel.setFont(myFont);
        timerLabel.setSize(timerLabel.getPreferredSize());
        timerLabel.setLocation(dotAreaWidth / 2 - (timerLabel.getWidth() / 2), dotAreaHeight / 2 - (timerLabel.getHeight() / 2));
        inputPanel.add(timerLabel);
        inputPanel.revalidate();
        inputPanel.repaint();

        for (i = 0; i < reps; i++) {
            resetUserChoice();
            overRun = 0;
            timeA = new Date().getTime();
            timeB = new Date().getTime();
            while (getUserChoice() == '~') {
                timeB = new Date().getTime();
                if (timeA != timeB) {
                    setUserChoice('a');
                    devisor = timeB - timeA;
                    testLoop[i] = overRun / devisor;
                } else {
                    overRun++;
                }
            }
        }

        for (i = 0; i < reps; i++) {
            tmpTot += testLoop[i];
        }

        loopsPerMS = tmpTot / reps;
        
        inputPanel.remove(timerLabel);
        inputPanel.validate();

        return loopsPerMS;
    }

    public synchronized long getTimedKeyResponse(PrintWriter log) { //remove
        log.append("ResponseV3 Method Started\n"); //remove
        long timeA, timeB, tmpTime;
        long overRun;
        boolean print = true;

        if (loopsPerMS == 0) {
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        /** reset the user choice so that the polling call below will work.
         **/
        resetUserChoice();
        overRun = 0;
        timeA = new Date().getTime();
        timeB = new Date().getTime();
        tmpTime = timeA;

        /**	The following polls a variable to make the computer wait for the keyboard response
         **/
        log.append("ResponseV3 Loop Starting\n"); //remove
        while (getUserChoice() == '~') {
            timeB = new Date().getTime();
            if (print) {
                log.append("Loop Waiting...\n"); //remove
                print = false;
            }
            if (tmpTime != timeB) {
                overRun = 0;
                tmpTime = timeB;
            } else {
                overRun++;
            }
        }
        log.append("ResponseV3 Loop Finished\n"); //remove
        addedMS = overRun / loopsPerMS;
        RT = (timeB - timeA) + (overRun / loopsPerMS);

        return RT;
    }
    
    public synchronized ArrayList<Character> getTimedMultipleButtonResponse(long maxTime){
         ArrayList<Character> c = getTimedMultipleButtonResponse(maxTime, -1);
         return c;
    }
    
    public synchronized ArrayList<Character> getTimedMultipleButtonResponse(long maxTime, int trialResetTime){
        ArrayList<Character> c = getTimedMultipleButtonResponse(maxTime, trialResetTime, false);
         return c;
    }
    
    public synchronized ArrayList<Character> getTimedMultipleButtonResponse(long maxTime, int trialResetTime, boolean flag){
        long timeA, timeB, tmpTime, overRun;
        long totalTime = 0;
        responseList = new ArrayList<Character>();
        isMultipleButton = true;
        respListSize = -1;
        
        
        if(loopsPerMS ==0){
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        resetUserChoice();//sets user choice to '~'
        
        if(trialResetTime < 0){
        //System.out.println("calling poll for response");
            RT = pollForKeyResponse();
        //System.out.println("poll for response returned");
        }
        //else if(flag){
        //    RT = pollForKeyResponseRobot(trialResetTime);
        //}
        else{
            RT = pollForKeyResponse(trialResetTime);
        }
        
        
        if(trialResetTime > 0 && RT >= trialResetTime){
            responseList.add(0, '*');
            return responseList;
        }
        
        overRun = 0;
        timeA = new Date().getTime();
        timeB = new Date().getTime();
        tmpTime = timeA;
        
        //System.out.println("entering timed while");
        if(maxTime != 0){

            while(totalTime < maxTime){
                timeB = new Date().getTime();
                if(tmpTime != timeB){
                    overRun = 0;
                    tmpTime = timeB;
                    totalTime = (timeB- timeA) + (overRun/loopsPerMS);
                }
                else{
                    overRun++;
                }
            }
        }
        //System.out.println("exiting timed while");
        //System.out.println("RT: " + RT);
        //System.out.println("RESPONSE: " + responseList.toString());
        
        isMultipleButton = false;//resets so normal key response will work
        return responseList;
    }//timedMutipleButtonResponse
    
    public synchronized ArrayList<Character> getFixedMultipleButtonResponse(int numOfKeysHit){
        long timeA, timeB, tmpTime, overRun;
        long totalTime = 0;
        responseList = new ArrayList<Character>();
        respListSize = numOfKeysHit;
        isMultipleButton = true;
        currentRespSize = 0;
        if(loopsPerMS ==0){
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        resetUserChoice();
        overRun = 0;
        timeA = new Date().getTime();
        timeB = new Date().getTime();
        tmpTime = timeA;
        
        while(getRespSize() < numOfKeysHit){
            System.out.print("");
            timeB = new Date().getTime();
            if(tmpTime != timeB){
                overRun = 0;
                tmpTime = timeB;
                totalTime = (timeB- timeA) + (overRun/loopsPerMS);
            }
            else{
                overRun++;
            }
        }
        
        addedMS = overRun / loopsPerMS;
        RT = (timeB - timeA) + (overRun / loopsPerMS);
        isMultipleButton = false;//resets so normal key response will work
        return responseList;
    }
    
    public int getRespSize(){
        return responseList.size();
    }
    
    public long getAutoKeyResponse(Object o){
        long timeA, timeB, tmpTime;
        long overRun;
        if (loopsPerMS == 0) {
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        /** reset the user choice so that the polling call below will work.
         **/
        resetUserChoice();
        overRun = 0;
        timeA = new Date().getTime();
        timeB = new Date().getTime();
        tmpTime = timeA;
        
        synchronized(o){
            o.notify();
        }
        while (!keyboardInputDone) {
            timeB = new Date().getTime();
            if (tmpTime != timeB) {
                overRun = 0;
                tmpTime = timeB;
            } else {
                overRun++;
            }
        }
        
        addedMS = overRun / loopsPerMS;
        RT = (timeB - timeA) + (overRun / loopsPerMS);
        
        return RT;
    }
    public synchronized long getAutoKeyResponse(PrintWriter pw) {
        long timeA, timeB, tmpTime;
        long overRun;

        if (loopsPerMS == 0) {
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        /** reset the user choice so that the polling call below will work.
         **/
        resetUserChoice();
        overRun = 0;
        timeA = new Date().getTime();
        timeB = new Date().getTime();
        tmpTime = timeA;

        /**	The following polls a variable to make the computer wait for the keyboard response
         **/
        int pressed = 1;
        RandomIntGenerator gen = new RandomIntGenerator();
        gen.setIntervalRange(200, 2000, 1);
        int random = gen.drawWithInterval();
        double n;
        
        pw.println("entering while loop to poll response");
        pw.flush();
        
        while (getUserChoice() == '~') {
            n = (timeB -timeA)/(double) random;
        
            if (n > pressed){
                try {
                    Robot r = new Robot();
                    r.keyPress(KeyEvent.VK_D);
                    r.keyRelease(KeyEvent.VK_D);
                    pressed ++;
                } catch (AWTException ex) {pw.println("there was a error with the robot");
                pw.flush();}
                
            
            }
            timeB = new Date().getTime();
            if (tmpTime != timeB) {
                overRun = 0;
                tmpTime = timeB;
            } else {
                overRun++;
            }
        }
        pw.println("while loop finished");
        pw.flush();
        addedMS = overRun / loopsPerMS;
        RT = (timeB - timeA) + (overRun / loopsPerMS);
        
        if(RT - random > 200){
            pw.println("********WARNING*********");
            pw.println("random time: " + random);
            pw.println("response time: " + RT);
            pw.println("pressed: " + (pressed-1));
            pw.flush();
        }
        else{
            pw.println("pressed: " + (pressed - 1));
            pw.flush();
        }

        return RT;
    }
    private volatile boolean keyboardInputDone;
    private long pollForKeyResponse(){
        long rt = 0; 
        long timeA, timeB, tmpTime;
        long overRun;
       
       
         overRun = 0;
         timeA = new Date().getTime();
         timeB = new Date().getTime();
         tmpTime = timeA;
         
         while (!keyboardInputDone) {
             timeB = new Date().getTime();
             if (tmpTime != timeB) {
                 overRun = 0;
                 tmpTime = timeB;
             } else {
                 overRun++;
             }
         }
         
        addedMS = overRun / loopsPerMS;
        rt = (timeB - timeA) + (overRun / loopsPerMS);
        
    
       return rt;
    }
    
    private long pollForKeyResponse(char c){
        long rt = 0; 
        long timeA, timeB, tmpTime;
        long overRun;
       
       
         overRun = 0;
         timeA = new Date().getTime();
         timeB = new Date().getTime();
         tmpTime = timeA;
         while (getUserChoice() != c) {
             timeB = new Date().getTime();
             if (tmpTime != timeB) {
                 overRun = 0;
                 tmpTime = timeB;
             } else {
                 overRun++;
             }
         }
         
        addedMS = overRun / loopsPerMS;
        rt = (timeB - timeA) + (overRun / loopsPerMS);
        
    
       return rt;
    }
    
    private long pollForKeyResponseRobot(int pTime){
        long rt = 0; 
        long timeA, timeB, tmpTime;
        long overRun;
        Robot arr;
        
       
         overRun = 0;
         timeA = new Date().getTime();
         timeB = new Date().getTime();
         tmpTime = timeA;
         
         while (!keyboardInputDone) {
             timeB = new Date().getTime();
             if (tmpTime != timeB) {
                 overRun = 0;
                 tmpTime = timeB;
             } else {
                 overRun++;
             }
             
             if((timeB - timeA) + (overRun / loopsPerMS) > pTime){
                 try {
                     System.out.println("GO ROBO");
                     arr = new Robot();
                     arr.keyPress(KeyEvent.VK_D);
                     arr.keyRelease(KeyEvent.VK_D);
                 } catch (AWTException ex) {
                     System.err.println("FATAL ERROR");
                 }
             }
         }
         
        addedMS = overRun / loopsPerMS;
        rt = (timeB - timeA) + (overRun / loopsPerMS);
        
    
       return rt;
    }
    
    private long pollForKeyResponse(int pTime){
        long rt = 0; 
        long timeA, timeB, tmpTime;
        long overRun;
       
       
         overRun = 0;
         timeA = new Date().getTime();
         timeB = new Date().getTime();
         tmpTime = timeA;
         
         while (((timeB - timeA) + (overRun / loopsPerMS)) < pTime && !keyboardInputDone) {
             timeB = new Date().getTime();
             if (tmpTime != timeB) {
                 overRun = 0;
                 tmpTime = timeB;
             } else {
                 overRun++;
             }
         }
         
        addedMS = overRun / loopsPerMS;
        rt = (timeB - timeA) + (overRun / loopsPerMS);
        
    
       return rt;
    }
    
    private long pollForKeyResponse(DrawExpFrame frame, int probeTime){
        long timeA, timeB, tmpTime;
        long overRun;
        boolean probeRemoved = false;
        long rt = 0;
        
        overRun = 0;
        timeA = new Date().getTime();
        timeB = new Date().getTime();
        tmpTime = timeA;

        /**	The following polls a variable to make the computer wait for the keyboard response
         **/
        while (getUserChoice() == '~') {
            timeB = new Date().getTime();
            if (tmpTime != timeB) {
                overRun = 0;
                tmpTime = timeB;
            } else {
                overRun++;
            }

            if (!probeRemoved && ((timeB - timeA) + (overRun / loopsPerMS)) >= probeTime) {
                frame.setContentPane(new BlankPanel(Color.BLACK));
                frame.validate();
                probeRemoved = true;
            }
        }

        addedMS = overRun / loopsPerMS;
        rt = (timeB - timeA) + (overRun / loopsPerMS);
        
        return rt;
    }
    
    private long pollForResponse(){
        long rt = 0;
        long timeA, timeB, tmpTime;
        long overRun = 0;
        
        
        timeA = new Date().getTime();
        timeB = new Date().getTime();
        tmpTime = timeA;
        
        
        while (!getInputDone()) {
            timeB = new Date().getTime();
            if (tmpTime != timeB) {
                overRun = 0;
                tmpTime = timeB;
            } else {
                overRun++;
            }
        }
        
        addedMS = overRun / loopsPerMS;
        rt = (timeB - timeA) + (overRun / loopsPerMS);
        return rt;
    }
    
    public synchronized long getTimedKeyResponse() {
        long timeA, timeB, tmpTime;
        long overRun;

        if (loopsPerMS == 0) {
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        /** reset the user choice so that the polling call below will work.
         **/
        resetUserChoice();
        
        RT = pollForKeyResponse();
        return RT;
    }

    public long getTimedKeyResponse(DrawExpFrame frame, int probeTime) {

        if (loopsPerMS == 0) {
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        /** reset the user choice so that the polling call below will work.
         **/
        resetUserChoice();

        RT = pollForKeyResponse(frame, probeTime);

        return RT;
    }

    public synchronized long getTimedTwoButtonResponse(String labelA, String labelB) {
        long timeA, timeB;
        int choice = 999;

        Object[] buttonLabels = {labelA, labelB};
        timeA = new Date().getTime();

        choice = JOptionPane.showOptionDialog(null, "Please Choose an Option",
                "ResponseV3", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttonLabels, buttonLabels[0]);

        timeB = new Date().getTime();

        RT = timeB - timeA;
        if (choice == 0) {
            userChoice = getSameChoice();
        }
        if (choice == 1) {
            userChoice = getDiffChoice();
        }
        if (choice == JOptionPane.CLOSED_OPTION) {
            resetUserChoice();
            JOptionPane.showMessageDialog(null, "You must choose an option.  Do NOT use the Close Box", "alert", JOptionPane.ERROR_MESSAGE);
        }

        return RT;
    }

    public synchronized long getTimedTwoButtonResponse(String labelA, String labelB, String question) {
        long timeA, timeB;
        int choice = 999;

        Object[] buttonLabels = {labelA, labelB};
        timeA = new Date().getTime();

        choice = JOptionPane.showOptionDialog(null, question,
                "ResponseV3", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttonLabels, buttonLabels[0]);

        timeB = new Date().getTime();

        RT = timeB - timeA;
        if (choice == 0) {
            userChoice = getSameChoice();
        }
        if (choice == 1) {
            userChoice = getDiffChoice();
        }
        if (choice == JOptionPane.CLOSED_OPTION) {
            resetUserChoice();
            JOptionPane.showMessageDialog(null, "You must choose an option.  Do NOT use the Close Box", "alert", JOptionPane.ERROR_MESSAGE);
        }

        return RT;
    }
    

    public synchronized void getTimedSliderNoThumbResponse(JFrame parent, int min, int max, int initial,
            String lowLabel, String highLabel, String responseLabel){
        
        getTimedSliderNoThumbResponse(parent, min, max, initial, lowLabel, highLabel, responseLabel, 500, 250, new Font(Font.SERIF, Font.PLAIN, 12), Color.LIGHT_GRAY);
        
    }    
    public synchronized void getTimedSliderNoThumbResponseLabeled(JFrame parent, final int min, final int max, final int initial,
            String lowLabel, String highLabel, String responseLabel, int windowWidth, int windowHeight, Font f, final Color thumbColor, int tick) {
        inputDone = false; 
        //final SliderNoThumb slider;
        
        slider = new SliderNoThumb(min, max, initial, thumbColor);
        
        slider.setBackground(Color.lightGray);
        slider.setPaintLabels(true);
        Hashtable labelTable = new Hashtable();
        slider.setMajorTickSpacing(tick);
        slider.setPaintTicks(false);
        slider.setPaintLabels(true);
        JLabel j1 = new JLabel("<html>" + lowLabel);
        JLabel j2 = new JLabel("<html>" + highLabel);
        Font f2 = new Font(f.getFontName(), Font.PLAIN, 12);
        j1.setFont(f2);
        j2.setFont(f2);
        labelTable.put(min, j1);
        labelTable.put(max, j2);
        
        slider.setLabelTable(labelTable);
        
        
       
        
        sliderOkButton = new JButton("OK");
        sliderOkButton.addActionListener(this);
        
        //slider.addMouseListener(new MouseAdapter(){
           // public void mouseClicked(MouseEvent e){
               // Point point = slider.getLocation();
                //the start position of the slider bar is off from the start position of the panel by about 35 pixels
               // int start = point.x+35;
              //  if(e.getX()>= start-10 && e.getX()<= start+10){
               // slider.hideThumb = false;
              //  System.out.println(slider.getValue());
               // }
          //  }
      //  }
      //  );
        slider.addChangeListener(this);
        
                 
       
    
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();

        
        createResponseFrame(parent, slider, sliderOkButton, responseLabel, false, -1, d.width/2, d.height/2, windowWidth, windowHeight, f);
    }
    public synchronized void getTimedSliderNoThumbResponse(JFrame parent, int min, int max, int initial,
            String lowLabel, String highLabel, String responseLabel, int windowWidth, int windowHeight, Font f, Color thumbColor) {
        inputDone = false; 

        slider = new SliderNoThumb(min, max, initial, thumbColor);
        
        slider.setBackground(Color.lightGray);
        slider.setPaintLabels(true);
        Hashtable labelTable = new Hashtable();
        JLabel j1 = new JLabel("<html>" + lowLabel);
        JLabel j2 = new JLabel("<html>" + highLabel);
        Font f2 = new Font(f.getFontName(), Font.PLAIN, 12);
        j1.setFont(f2);
        j2.setFont(f2);
        labelTable.put(min, j1);
        labelTable.put(max, j2);
        
        slider.setLabelTable(labelTable);

        sliderOkButton = new JButton("OK");
        sliderOkButton.addActionListener(this);
        slider.addChangeListener(this);
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();

        
        createResponseFrame(parent, slider, sliderOkButton, responseLabel, false, -1, d.width/2, d.height/2, windowWidth, windowHeight, f);
    }
    public synchronized void getTimedRadioButtonResponseLabeled(JFrame parent, int min, int max, String lowLabel,
            String highLabel, String responseLabel, int minWindow,int windowHeight, Font f){
        inputDone = false;
        JPanel panel = new JPanel();
        JPanel margin_panel = new JPanel();
        margin_panel.setBackground(Color.lightGray);
        JRadioButton button;
        int numRadioButtons = max-(min-1);
        int lowLabelWidth;
        int highLabelWidth;
        int panel_width;
        JLabel lLbl1 = new JLabel(""+min);
        JLabel lLbl2 = new JLabel(lowLabel);
        JLabel hLbl1 = new JLabel(""+max);
        JLabel hLbl2 = new JLabel(highLabel);
        panel.setLayout(new GridLayout(0,max));
        panel.setBackground(Color.lightGray);
        FontMetrics fm = panel.getFontMetrics(f);
        lowLabelWidth = fm.stringWidth(lowLabel);
        highLabelWidth = fm.stringWidth(highLabel);
        if (lowLabelWidth<highLabelWidth){
            panel_width = highLabelWidth*numRadioButtons;
        }
        else{
            panel_width = lowLabelWidth*numRadioButtons;
        }
        
        buttonGroup = new ButtonGroup();
        for(int i= min ; i<max+1; i++){
            button = new JRadioButton();
            buttonGroup.add(button);
            button.setActionCommand(""+i);
            //button.addActionListener(this);
            button.setHorizontalAlignment(AbstractButton.CENTER);
            panel.add(button);
        }
        lLbl1.setHorizontalAlignment(JLabel.CENTER);
        panel.add(lLbl1);
        for(int i=min+1;i<max;i++){
            JLabel l = new JLabel("");
            panel.add(l);
        }
        hLbl1.setHorizontalAlignment(JLabel.CENTER);
        panel.add(hLbl1);
        lLbl2.setHorizontalAlignment(JLabel.CENTER);
        panel.add(lLbl2);
        for(int i=min+1;i<max;i++){
            JLabel l = new JLabel("");
            panel.add(l);
        }
        hLbl2.setHorizontalAlignment(JLabel.CENTER);
        panel.add(hLbl2);
        radioOkButton = new JButton("ok");
        radioOkButton.addActionListener(this);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int padding = panel_width/2;
        margin_panel.add(panel);
        if(panel_width + padding >d.width){
            System.out.println("too many radio buttons, perhaps try no labeling");
        }
        createResponseFrame(parent, margin_panel, radioOkButton, responseLabel, false, -1, d.width/2, d.height/2, panel_width+padding, windowHeight, f);
    }
    public synchronized void getTimedRadioButtonResponse(JFrame parent, int min, int max, String responseLabel, int minWindow,int windowHeight, Font f){
        inputDone = false;
        JPanel panel = new JPanel();
        JRadioButton button;
        JPanel margin_panel = new JPanel();
        margin_panel.setBackground(Color.lightGray);
        int numRadioButtons = max-(min-1);
        int lowLabelWidth;
        int highLabelWidth;
        int panel_width;
        int radButton_height = 30;
        panel.setLayout(new GridLayout(0,max));
        panel.setBackground(Color.lightGray);
        panel_width = radButton_height*numRadioButtons;
        if (panel_width >minWindow){
            minWindow = panel_width;
        }
        
        buttonGroup = new ButtonGroup();
        for(int i= min ; i<max+1; i++){
            button = new JRadioButton();
            buttonGroup.add(button);
            button.setActionCommand(""+i);
            //button.addActionListener(this);
            button.setHorizontalAlignment(AbstractButton.CENTER);
            panel.add(button);
        }//end of for loop
        
        radioOkButton = new JButton("ok");
        radioOkButton.addActionListener(this);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        if (minWindow>d.width){
            System.out.println("too many radio buttons");
        }
        
        panel.setVisible(true);
        double var = panel.getPreferredSize().getHeight();
        //System.out.println("height is "+var);
        
        margin_panel.add(panel);
        createResponseFrame(parent, margin_panel, radioOkButton, responseLabel, false, -1, d.width/2, d.height/2,minWindow ,windowHeight, f);
    }
    //not working yet
    /*private class Custom_Radio_Panel extends JPanel{
        
        public void paintComponent(Graphics2D g2){
           super.paintComponent(g2);
           g2.setColor(Color.RED);
           g2.draw(new Line2D.Double(0,this.getPreferredSize().getHeight()/2, this.getPreferredSize().getWidth(), this.getPreferredSize().getHeight()/2));
        }
    }
    */
    private class MyUI extends BasicSliderUI{
        MyUI(JSlider j){
            super(j);
        }
        
        //This methods ensures that the thumb is in the correct location when user clicks on track
        @Override
        protected void scrollDueToClickInTrack(int direction) {

            int value = slider.getValue(); 

            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                value = this.valueForXPosition(slider.getMousePosition().x);
            } else if (slider.getOrientation() == JSlider.VERTICAL) {
                value = this.valueForYPosition(slider.getMousePosition().y);
            }
            slider.setValue(value);
        }
        
        @Override
        public void paintFocus(Graphics g){
            //do nothing so there is no rectangle border when selected
        }
    }

    public synchronized void getTimedSliderResponse(JFrame parent, int min, int max, int initial,
            String lowLabel, String highLabel, String responseLabel) {
        inputDone = false;

        slider = new JSlider(min, max, initial);
        slider.setBackground(Color.lightGray);
        slider.setPaintLabels(true);
        slider.setUI(new MyUI(slider));
        Hashtable labelTable = new Hashtable();
        labelTable.put(min, new JLabel(lowLabel));
        labelTable.put(max, new JLabel(highLabel));
        slider.setLabelTable(labelTable);

        sliderOkButton = new JButton("OK");
        sliderOkButton.addActionListener(this);
        slider.addChangeListener(this);
       

        createResponseFrame(parent, slider, sliderOkButton, responseLabel, true);
    }

    public synchronized void getTimeLimitTextResponse(JFrame parent, String info, int maxTime, int posX, int posY, int width, int height, Font f){
        inputDone = false;

        textInput = new JTextField(10);
        textInput.setText(null);

        textOkButton = new JButton("OK");
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();

        createResponseFrame(parent, textInput, textOkButton, info, false, maxTime, posX, posY, width, height, f);
    }
   
    public synchronized void getTimedTextResponse(JFrame parent, String info) {
        inputDone = false;

        textInput = new JTextField(10);
        textInput.setText(null);

        textOkButton = new JButton("OK");


        createResponseFrame(parent, textInput, textOkButton, info, false);

    }
    
    public synchronized void displayNotificationFrame(JFrame parent, String info){
        inputDone = false;
        
        textOkButton = new JButton("OK");
        
        createNotificationFrame(parent, info, textOkButton, 500, 250);
    }
    
    public synchronized void displayNotificationFrame(JFrame parent, String info, int width, int height){
        inputDone = false;
        
        textOkButton = new JButton("OK");
        
        createNotificationFrame(parent, info, textOkButton, width, height);
    }
    
    

    public void getTimedNumPadResponse(JFrame parent, String info, String targetFieldFormat) {

        if (loopsPerMS == 0) {
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        createNumPadResponseFrame(parent, info, targetFieldFormat);

        inputDone = false;
        textRT = pollForResponse();

        textValue = numPadResponse.getResponse();
    }

    public void getFixedTimeNumPadResponse(int responseTimeLimit){
        long timeA, timeB, tmpTime;
        long overRun = 0;
        numPadResponse.setActiveField("0");
        if (loopsPerMS == 0) {
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        //Show numpad dialog
        respFrame.setVisible(true);
        timeA = new Date().getTime();
        timeB = new Date().getTime();
        tmpTime = timeA;

        inputDone = false;
        while (!getInputDone()) {
            timeB = new Date().getTime();
            if (tmpTime != timeB) {
                overRun = 0;
                tmpTime = timeB;
            } else {
                overRun++;
            }
        }

        addedMS = overRun / loopsPerMS;
        textRT = (timeB - timeA) + (overRun / loopsPerMS);
        RT = (timeB - timeA) + (overRun / loopsPerMS);

        textValue = numPadResponse.getResponse();
        
        
        startTime = new Date().getTime();
            long timeSpent = 0;
            while(!getInputDone() && timeSpent < responseTimeLimit){
                timeSpent = (new Date().getTime()) - startTime;

                if(timeSpent >= responseTimeLimit){
                    inputDone = true;
                    textValue = textInput.getText().trim();
                    textRT = responseTimeLimit;
                    respFrame.dispose();
                }
            }
    }
    public void getTimedNumPadResponse() {
        numPadResponse.setActiveField("0");
        if (loopsPerMS == 0) {
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        //Show numpad dialog
        respFrame.setVisible(true);

        inputDone = false;
  

        textRT = pollForResponse();
        RT = textRT;

        textValue = numPadResponse.getResponse();
    }
public synchronized void getTimedTextResponseJustified(JFrame parent, String info, int columns, String just) {
        
        inputDone = false;

        textInput = new JTextField(columns);
        textInput.setText(null);
        
       
       
        if (just.equals("center")){
            textInput.setHorizontalAlignment(JTextField.CENTER);
        }
        textOkButton = new JButton("OK");

        createTextResponseFrame(parent, textInput, textOkButton, info, false);

    }
    public synchronized void getTimedTextResponse(JFrame parent, String info, int columns) {
        inputDone = false;

        textInput = new JTextField(columns);
        textInput.setText(null);

        textOkButton = new JButton("OK");

        createTextResponseFrame(parent, textInput, textOkButton, info, false);

    }
    
    public synchronized void getTimedRadioButtonResponse(JFrame parent, String info, String[] options){
        int count = options.length;
        buttons = new JCheckBox[count];

        JPanel buttonPanel = new JPanel(new GridLayout(count, 1));
        buttonPanel.setBackground(Color.LIGHT_GRAY);
        
        for(int i = 0; i < buttons.length; i++){
            buttons[i] = new JCheckBox("<HTML>" + options[i] + "</HTML>");
            buttonPanel.add(buttons[i]);
        }
        
        inputDone = false;
        rbOkButton = new JButton("OK");
        
        createResponseFrame(parent, buttonPanel, rbOkButton, info, false);
        
    }
    

    public long getTimedNumberLineResponse(NumberLine lPanel) {
        //Only the spacebar will trigger a timer stop, anything else is ignored
        char submitKey = ' ';

        if (loopsPerMS == 0) {
            JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        /** reset the user choice so that the polling call below will work.
         **/
        resetUserChoice();

        //Idle here until user has dragged the handle
        while (!lPanel.isHandleDragged()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                Logger.getLogger(ResponseV3.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        resetUserChoice(); //reset choice because space bar could already be pressed at this point
        //User has moved handle so respond to the space bar

        RT = pollForKeyResponse(submitKey);

        return RT;
    }

    public void actionPerformed(ActionEvent evt) {
        if(respFrame != null){
            
        JButton button = (JButton) evt.getSource();
        

        if (button.equals(sliderOkButton)) {
            sliderValue = slider.getValue();
            
            if (isSliderMoved) {
                endTime = new Date().getTime();
                sliderRT = endTime - startTime;
                respFrame.setVisible(false);
                respFrame.dispose();
                respFrame = null;
                inputDone = true;
            }
        }
        if (button.equals(textOkButton)) {
            endTime = new Date().getTime();
            textRT = endTime - startTime;
            if (textInput != null && !"".equals(textInput.getText())) {
                textValue = (textInput.getText()).trim();
                respFrame.dispose();
                inputDone = true;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  }
            else if (numPadResponse != null) {
                textValue = numPadResponse.getResponse();
                if (numPadResponse.validateResponse(textValue)) {
                    if (legacy) {
                        respFrame.setVisible(false);
                        respFrame.dispose();
                        respFrame = null;
                    } else {
                        respFrame.setVisible(false);
                    }

                    inputDone = true;
                }
            }
            else if(textInput == null){
                respFrame.setVisible(false);
                respFrame.dispose();
                inputDone = true;
            }
        }
        
        if(button.equals(rbOkButton)){
            endTime = new Date().getTime();
            boxId = new ArrayList<>();
            
            int temp = boxId.size();
            
            for(int i = 0; i < buttons.length; i ++){

                if(buttons[i].isSelected()){
                    boxId.add(i);   
                }
            }
            
            if(temp != boxId.size()){
                inputDone = true;
                respFrame.setVisible(false);
                respFrame.dispose();
                respFrame = null;
            }
            
            else
                JOptionPane.showMessageDialog(respFrame.getContentPane(), "Please select one or more options");
        }
        if (button.equals(dotOkButton)) {
            respFrame.dispose();
            inputDone = true;
        }
        if (button.equals(radioOkButton)){
            endTime = new Date().getTime();
            radioRT = endTime-startTime;
            radioValue = buttonGroup.getSelection().getActionCommand();
            respFrame.setVisible(false);
            respFrame.dispose();
            respFrame = null;
            inputDone = true;
        }
        }
    }

    public void stateChanged(ChangeEvent changeEvent) {
        Object source = changeEvent.getSource();
        if (source instanceof JSlider) {
            JSlider jslider = (JSlider) source;
            if (!jslider.getValueIsAdjusting()) {
                isSliderMoved = true;
            }
        }
    }
    public synchronized void createResponseFrame(JFrame parent, JComponent inputComponent, JButton okButton, String label, boolean isFrameModal){
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        createResponseFrame(parent, inputComponent, okButton, label, isFrameModal, 0, d.width/2, d.height/2, 500, 250, new Font(Font.SERIF, Font.PLAIN, 12));
    }
    public synchronized void createResponseFrame(JFrame parent, JComponent inputComponent, JButton okButton, String label, boolean isFrameModal, int responseTimeLimit, int locationX, int locationY,
            int windowWidth, int windowHeight, Font f) {

        respFrame = new JDialog(parent, isFrameModal);
        respFrame.setTitle("ResponseV3");
        respFrame.addKeyListener(this);
        respFrame.setResizable(false);
        respFrame.setUndecorated(true);

        // dont let people close out
        respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        closedProperly = true;
        respFrame.addWindowListener(new WindowAdapter() {

            public synchronized void windowClosed(WindowEvent e) {
            }

            public synchronized void windowClosing(WindowEvent e) {
                //inputDone = true;
                //closedProperly = false;
            }
        });

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        respFrame.setSize(windowWidth, windowHeight);
        locationX -= windowWidth/2;
        locationY -= windowHeight/2;
        respFrame.setLocation(locationX, locationY);
//    respFrame.setSize(screenWidth,200);
//    respFrame.setLocation (0,(d.height - 200)/2);

        BlankPanel inputPanel = new BlankPanel(Color.lightGray);
        BlankPanel buttonPanel = new BlankPanel(Color.lightGray);
        BlankPanel blackPanelA = new BlankPanel(Color.lightGray);
        BlankPanel blackPanelB = new BlankPanel(Color.lightGray);

        /*** this set of code allows one to get multiple line headers on the slider ***/
        if(!("".equals(label))){
            JLabel rLabel = new JLabel(label);
            rLabel.setFont(f);
            blackPanelA.add(rLabel);
        }

        Insets cInset = new Insets(10, 10, 10, 10);
        GridBagLayout gridBag = new GridBagLayout();
        inputPanel.setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = cInset;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        
        if(!("".equals(label))){
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            gridBag.setConstraints(blackPanelA, c);
            inputPanel.add(blackPanelA);
        }

        c.gridx = 0;
        if(!("".equals(label)))
            c.gridy = 1;
        else
            c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        gridBag.setConstraints(inputComponent, c);
        inputPanel.add(inputComponent);
        

        buttonPanel.add(okButton);
        okButton.addActionListener(this);

        respFrame.getContentPane().setLayout(new BorderLayout());
        respFrame.getContentPane().add(inputPanel, BorderLayout.CENTER);
        respFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        //respFrame.pack();

        // because this is a modal dialog, the show () command will not return until the frame has been
        // removed from sight
        respFrame.getRootPane().setDefaultButton(okButton);
        respFrame.setVisible(true);

        if (inputComponent instanceof JSlider) {
            isSliderMoved = false;
        }
        if(responseTimeLimit == -1){//do not constrain responseTime
            startTime = new Date().getTime();
            while (!getInputDone()) {
                Thread.yield();
            }
            if (!closedProperly) {
                okButton.doClick();
                JOptionPane.showMessageDialog(null, "You must choose an option.  Do NOT use the Close Box", "alert", JOptionPane.ERROR_MESSAGE);
            }
        }
        else{
            startTime = new Date().getTime();
            long timeSpent = 0;
            while(!getInputDone() && timeSpent < responseTimeLimit){
                timeSpent = (new Date().getTime()) - startTime;

                if(timeSpent >= responseTimeLimit){
                    inputDone = true;
                    textValue = textInput.getText().trim();
                    textRT = responseTimeLimit;
                    respFrame.dispose();
                }
            }
            if (!closedProperly) {
                okButton.doClick();
                JOptionPane.showMessageDialog(null, "You must choose an option.  Do NOT use the Close Box", "alert", JOptionPane.ERROR_MESSAGE);
            }
        }
     }

    public synchronized void createTextResponseFrame(JFrame parent, JComponent inputComponent, JButton okButton, String label, boolean isFrameModal) {

        respFrame = new JDialog(parent, isFrameModal);
        respFrame.setTitle("ResponseV3");
        respFrame.addKeyListener(this);

        // dont let people close out
        respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        closedProperly = true;
        respFrame.addWindowListener(new WindowAdapter() {

            public synchronized void windowClosed(WindowEvent e) {
            }

            public synchronized void windowClosing(WindowEvent e) {
                //inputDone = true;
                //closedProperly = false;
            }
        });

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        respFrame.setSize(500, 250);
        respFrame.setLocation((d.width - 500) / 2, (d.height - 250) / 2);
//    respFrame.setSize(screenWidth,200);
//    respFrame.setLocation (0,(d.height - 200)/2);

        BlankPanel inputPanel = new BlankPanel(Color.lightGray);
        BlankPanel buttonPanel = new BlankPanel(Color.lightGray);
//    BlankPanel blackPanelA [] = new BlankPanel [5];
        BlankPanel blackPanelA = new BlankPanel(Color.lightGray);
        BlankPanel blackPanelB = new BlankPanel(Color.lightGray);
        BlankPanel blackPanelC = new BlankPanel(Color.lightGray);
        BlankPanel responsePanel = new BlankPanel(Color.lightGray);

//    JLabel responseLabel = new JLabel (label);

        /*** this set of code allows one to get multiple line headers on the slider ***/
        JLabel rLabel = new JLabel(label);

        Font labelFont = rLabel.getFont();
        FontMetrics fm = rLabel.getFontMetrics(labelFont);
        int fontWidth = fm.stringWidth(label);



        int i = 0;
        int totalTokens;
        String[] outString = new String[5];
        String tmp = "";
        StringTokenizer t = new StringTokenizer(label, " ");


        do {
            tmp = t.nextToken();
            totalTokens = t.countTokens();
            if (totalTokens > 0) {
                do {
                    tmp += " " + t.nextToken();
                    fontWidth = fm.stringWidth(tmp);
                    totalTokens = t.countTokens();
                } while (fontWidth < 200 && totalTokens > 0);
            }
            outString[i] = tmp + "\n";
            i++;
        } while (totalTokens > 0 && i < 5);

        int j;
        for (j = 0; j < i; j++) {
            JLabel responseLabel = new JLabel(outString[j]);
            blackPanelA.add(responseLabel);
        }

        Insets cInset = new Insets(10, 10, 10, 10);
        GridBagLayout gridBag = new GridBagLayout();
        inputPanel.setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = cInset;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.VERTICAL;


        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 5;
        c.gridheight = 1;
        gridBag.setConstraints(blackPanelB, c);
        inputPanel.add(blackPanelB);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 5;
        c.gridheight = 5;
        gridBag.setConstraints(blackPanelA, c);
        inputPanel.add(blackPanelA);

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 5;
        c.gridheight = 1;
        gridBag.setConstraints(blackPanelC, c);
        inputPanel.add(blackPanelC);

        responsePanel.setLayout(gridBag);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.SOUTH;
        c.fill = GridBagConstraints.VERTICAL;
        gridBag.setConstraints(inputComponent, c);
        responsePanel.add(inputComponent);



        buttonPanel.add(okButton);
        okButton.addActionListener(this);

        responsePanel.add(inputComponent);


        respFrame.getContentPane().setLayout(new BorderLayout());
        respFrame.getContentPane().add(responsePanel, "East");
        respFrame.getContentPane().add(inputPanel, "Center");
        respFrame.getContentPane().add(buttonPanel, "South");

        startTime = new Date().getTime();

        // because this is a modal dialog, the show () command will not return until the frame has been
        // removed from sight

        respFrame.getRootPane().setDefaultButton(okButton);
        respFrame.setVisible(true);


        while (!getInputDone()) {
            Thread.yield();
        }

        if (!closedProperly) {
            okButton.doClick();
            JOptionPane.showMessageDialog(null, "You must choose an option.  Do NOT use the Close Box", "alert", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void createNotificationFrame(JFrame parent, String label, JButton okButton, int width, int height){
        respFrame = new JDialog(parent, false);
        respFrame.setTitle("Notification");
        //respFrame.addKeyListener(this);

        // dont let people close out
        respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        closedProperly = true;
//        respFrame.addWindowListener(new WindowAdapter() {
//
//            public synchronized void windowClosed(WindowEvent e) {
//            }
//
//            public synchronized void windowClosing(WindowEvent e) {
//                //inputDone = true;
//                //closedProperly = false;
//            }
//        });

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        respFrame.setSize(width, height);
        respFrame.setLocation((d.width - width) / 2, (d.height - height) / 2);
//    respFrame.setSize(screenWidth,200);
//    respFrame.setLocation (0,(d.height - 200)/2);
        
        Color tempC = new Color(200,200,200);
        
        BlankPanel inputPanel = new BlankPanel(tempC);
        BlankPanel buttonPanel = new BlankPanel(tempC);
//    BlankPanel blackPanelA [] = new BlankPanel [5];
        BlankPanel blackPanelA = new BlankPanel(tempC);
        BlankPanel blackPanelB = new BlankPanel(tempC);
        BlankPanel blackPanelC = new BlankPanel(tempC);
//        BlankPanel responsePanel = new BlankPanel(Color.lightGray);

//    JLabel responseLabel = new JLabel (label);

        /*** this set of code allows one to get multiple line headers on the slider ***/
        JLabel rLabel = new JLabel(label);

        Font labelFont = rLabel.getFont();
        FontMetrics fm = rLabel.getFontMetrics(labelFont);
        int fontWidth = fm.stringWidth(label);



        int i = 0;
        int totalTokens;
        String[] outString = new String[5];
        String tmp = "";
        StringTokenizer t = new StringTokenizer(label, " ");


        do {
            tmp = t.nextToken();
            totalTokens = t.countTokens();
            if (totalTokens > 0) {
                do {
                    tmp += " " + t.nextToken();
                    fontWidth = fm.stringWidth(tmp);
                    totalTokens = t.countTokens();
                } while (fontWidth < 200 && totalTokens > 0);
            }
            outString[i] = tmp + "\n";
            i++;
        } while (totalTokens > 0 && i < 5);

        int j;
        for (j = 0; j < i; j++) {
            JLabel responseLabel = new JLabel(outString[j]);
            blackPanelA.add(responseLabel);
        }

        Insets cInset = new Insets(10, 10, 10, 10);
        GridBagLayout gridBag = new GridBagLayout();
        inputPanel.setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = cInset;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.VERTICAL;


        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 5;
        c.gridheight = 1;
        gridBag.setConstraints(blackPanelB, c);
        inputPanel.add(blackPanelB);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 5;
        c.gridheight = 5;
        gridBag.setConstraints(blackPanelA, c);
        inputPanel.add(blackPanelA);

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 5;
        c.gridheight = 1;
        gridBag.setConstraints(blackPanelC, c);
        inputPanel.add(blackPanelC);

//        responsePanel.setLayout(gridBag);
//        c.weightx = 1.0;
//        c.weighty = 1.0;
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.anchor = GridBagConstraints.SOUTH;
//        c.fill = GridBagConstraints.VERTICAL;
//        gridBag.setConstraints(inputComponent, c);
//        responsePanel.add(inputComponent);



        buttonPanel.add(okButton);
        okButton.addActionListener(this);

//        responsePanel.add(inputComponent);


        respFrame.getContentPane().setLayout(new BorderLayout());
//        respFrame.getContentPane().add(responsePanel, "East");
        respFrame.getContentPane().add(inputPanel, "Center");
        respFrame.getContentPane().add(buttonPanel, "South");

        startTime = new Date().getTime();

        // because this is a modal dialog, the show () command will not return until the frame has been
        // removed from sight

        respFrame.getRootPane().setDefaultButton(okButton);
        respFrame.setVisible(true);


        while (!getInputDone()) {
            Thread.yield();
        }

        if (!closedProperly) {
            okButton.doClick();
            JOptionPane.showMessageDialog(null, "You must choose an option.  Do NOT use the Close Box", "alert", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createNumPadResponseFrame(JFrame parent, String label, String targetFieldFormat) {
        respFrame = new JDialog(parent);
        respFrame.setTitle("ResponseV3");

        //Color frameColor = respFrame.getContentPane().getBackground();
        respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        numPadResponse = new NumPadResponse(targetFieldFormat.trim(), respFrame);

        textOkButton = new JButton("OK");
        JPanel inputPanel = new JPanel(new BorderLayout(3, 5));
        JPanel labelPanel = new JPanel(new FlowLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        labelPanel.add(new JLabel(label));
        buttonPanel.add(textOkButton);

        inputPanel.add(labelPanel, BorderLayout.NORTH);
        inputPanel.add(numPadResponse.getPanel(), BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);
        respFrame.getContentPane().add(inputPanel);

        Dimension respFrameDim = respFrame.getPreferredSize();

        final int respFrameWidth = respFrameDim.width + 150;
        final int respFrameHeight = respFrameDim.height + 75;
        respFrame.setSize(respFrameWidth, respFrameHeight);
        respFrame.setLocation((parent.getWidth() - respFrameWidth) / 2, (parent.getHeight() - respFrameHeight) / 2);
        respFrame.setResizable(false);

        textOkButton.addActionListener(this);
        respFrame.getRootPane().setDefaultButton(textOkButton);
        respFrame.addKeyListener(this);
        respFrame.setVisible(true);

    }

    public void createNumPadResponseFrameNew(JFrame parent, String label, String targetFieldFormat) {
        //Not using old version of numpad
        legacy = false;

        respFrame = new JDialog(parent);
        respFrame.setTitle("ResponseV3");

        //Color frameColor = respFrame.getContentPane().getBackground();
        respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        numPadResponse = new NumPadResponse(targetFieldFormat.trim(), respFrame);

        textOkButton = new JButton("OK");
        JPanel inputPanel = new JPanel(new BorderLayout(3, 5));
        JPanel labelPanel = new JPanel(new FlowLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        labelPanel.add(new JLabel(label));
        buttonPanel.add(textOkButton);

        inputPanel.add(labelPanel, BorderLayout.NORTH);
        inputPanel.add(numPadResponse.getPanel(), BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);
        respFrame.getContentPane().add(inputPanel);

        Dimension respFrameDim = respFrame.getPreferredSize();

        final int respFrameWidth = respFrameDim.width + 150;
        final int respFrameHeight = respFrameDim.height + 75;
        respFrame.setSize(respFrameWidth, respFrameHeight);
        respFrame.setLocation((parent.getWidth() - respFrameWidth) / 2, (parent.getHeight() - respFrameHeight) / 2);
        respFrame.setResizable(false);

        textOkButton.addActionListener(this);
        respFrame.getRootPane().setDefaultButton(textOkButton);
        respFrame.addKeyListener(this);

    }

    public synchronized void getPercentResponse(int low, int high, int dotSize, Color backColor,
            Color onDotColor, Color offDotColor, int largeChange, int mediumChange, int smallChange, int startOffDots) {

        JFrame nullFrame = new JFrame();
        respFrame = new JDialog(nullFrame);
        respFrame.setTitle("ResponseV3");
        respFrame.addKeyListener(this);

        // dont let people close out
        respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        respFrame.setSize(d.width, d.height);
        respFrame.setLocation(0, 0);


        // hide cursor: begin
        Image noImage = tk.getImage("noFile");
        Point pZ = new Point(0, 0);
        Cursor curs = tk.createCustomCursor(noImage, pZ, "noCurs");
        respFrame.setCursor(curs);
        // hide cursor: end



        BlankPanel inputPanel = new BlankPanel(backColor);
        BlankPanel buttonPanel = new BlankPanel(backColor);

        dotOkButton = new JButton("OK");
        buttonPanel.add(dotOkButton);
        dotOkButton.addActionListener(this);

        synchronized (this) {
            respFrame.setVisible(true);
            respFrame.getContentPane().setLayout(new BorderLayout());
            respFrame.getContentPane().add(inputPanel, "Center");
            respFrame.getContentPane().add(buttonPanel, "South");
            respFrame.setVisible(true);
        }

        //One must free the thread for a short time to allow the frame to build before drawing on it.
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        closedProperly = true;
        inputDone = false;

        respFrame.addWindowListener(new WindowAdapter() {

            public synchronized void windowClosed(WindowEvent e) {
            }

            public synchronized void windowClosing(WindowEvent e) {
                //							inputDone = true;
                //              closedProperly = false;
            }
        });


        int boundary = 20;
        Dimension d1 = inputPanel.getSize();
        int dotAreaHeight = d1.height - (2 * boundary);
        int dotAreaWidth = d1.width - (2 * boundary);

//    Image im = respFrame.createImage (d1.width,d1.height);
        Image im = inputPanel.createImage(d1.width, d1.height);
        Graphics g = im.getGraphics();
        g.setColor(backColor);
        g.fillRect(0, 0, d.width, d.height);
        Graphics gPanel = inputPanel.getGraphics();

        int dotWidth = dotSize;
        int dotHeight = dotSize;
        Color blackDotColor = onDotColor;
        Color whiteDotColor = offDotColor;

        long seed1 = new Date().getTime();
        Random onRand = new Random(seed1);
        Random offRand = new Random(seed1);
        Random otherRand = new Random(seed1);

        int totalBlackDots = 0;
        int totalWhiteDots = 0;
        int totalDots = 0;
        int totalOffRand = 0;
        int totalOnRand = 0;

        if (low == high) {
            totalBlackDots = high;
        } else {
            totalBlackDots = otherRand.nextInt(high - low) + low;
        }

        g.setColor(blackDotColor);

        int i, j;
        for (i = 0; i < totalBlackDots; i++) {
            int x = onRand.nextInt(dotAreaWidth) + boundary;
            int y = onRand.nextInt(dotAreaHeight) + boundary;
            g.fillOval(x, y, dotWidth, dotHeight);
            totalOnRand++;
        }
        totalDots = totalBlackDots;

// this is the number of white dots to start
        if (startOffDots > totalBlackDots) {
            startOffDots = totalBlackDots;
        }
        for (i = 0; i < startOffDots; i++) {
            if (totalOffRand == totalDots) {
                offRand.setSeed(seed1);
                totalOffRand = 0;
            }
            g.setColor(whiteDotColor);
            int x = offRand.nextInt(dotAreaWidth) + boundary;
            int y = offRand.nextInt(dotAreaHeight) + boundary;
            g.fillOval(x, y, dotWidth, dotHeight);
            totalBlackDots--;
            totalWhiteDots++;
            totalOffRand++;
        }

        gPanel.drawImage(im, 0, 0, inputPanel);


        // show cursor
        //     respFrame.setCursor (null);

        int keyHit;
        while (!getInputDone()) {
            respFrame.toFront();
            respFrame.requestFocus();
            keyHit = getKeyCode();
            keyCode = 0;
            switch (keyHit) {
                case KeyEvent.VK_ENTER:
                    dotOkButton.doClick();
                    break;
                case KeyEvent.VK_NUMPAD7:
                    for (i = 0; i < largeChange; i++) {
                        if (totalBlackDots == 0) {
                            break;
                        }
                        if (totalOffRand == totalDots) {
                            offRand.setSeed(seed1);
                            totalOffRand = 0;
                        }
                        g.setColor(whiteDotColor);
                        int x = offRand.nextInt(dotAreaWidth) + boundary;
                        int y = offRand.nextInt(dotAreaHeight) + boundary;
                        g.fillOval(x, y, dotWidth, dotHeight);
                        totalBlackDots--;
                        totalWhiteDots++;
                        totalOffRand++;
                    }
                    gPanel.drawImage(im, 0, 0, inputPanel);
                    break;
                case KeyEvent.VK_NUMPAD8:
                    for (i = 0; i < mediumChange; i++) {
                        if (totalBlackDots == 0) {
                            break;
                        }
                        if (totalOffRand == totalDots) {
                            offRand.setSeed(seed1);
                            totalOffRand = 0;
                        }
                        g.setColor(whiteDotColor);
                        int x = offRand.nextInt(dotAreaWidth) + boundary;
                        int y = offRand.nextInt(dotAreaHeight) + boundary;
                        g.fillOval(x, y, dotWidth, dotHeight);
                        totalBlackDots--;
                        totalWhiteDots++;
                        totalOffRand++;
                    }
                    gPanel.drawImage(im, 0, 0, inputPanel);
                    break;
                case KeyEvent.VK_NUMPAD9:
                    for (i = 0; i < smallChange; i++) {
                        if (totalBlackDots == 0) {
                            break;
                        }
                        if (totalOffRand == totalDots) {
                            offRand.setSeed(seed1);
                            totalOffRand = 0;
                        }
                        g.setColor(whiteDotColor);
                        int x = offRand.nextInt(dotAreaWidth) + boundary;
                        int y = offRand.nextInt(dotAreaHeight) + boundary;
                        g.fillOval(x, y, dotWidth, dotHeight);
                        totalBlackDots--;
                        totalWhiteDots++;
                        totalOffRand++;
                    }
                    gPanel.drawImage(im, 0, 0, inputPanel);
                    break;
                case KeyEvent.VK_NUMPAD1:
                    for (i = 0; i < largeChange; i++) {
                        if (totalWhiteDots == 0) {
                            break;
                        }
                        if (totalOnRand == totalDots) {
                            onRand.setSeed(seed1);
                            totalOnRand = 0;
                        }
                        g.setColor(blackDotColor);
                        int x = onRand.nextInt(dotAreaWidth) + boundary;
                        int y = onRand.nextInt(dotAreaHeight) + boundary;
                        g.fillOval(x, y, dotWidth, dotHeight);
                        totalBlackDots++;
                        totalWhiteDots--;
                        totalOnRand++;
                    }
                    gPanel.drawImage(im, 0, 0, inputPanel);
                    break;
                case KeyEvent.VK_NUMPAD2:
                    for (i = 0; i < mediumChange; i++) {
                        if (totalWhiteDots == 0) {
                            break;
                        }
                        if (totalOnRand == totalDots) {
                            onRand.setSeed(seed1);
                            totalOnRand = 0;
                        }
                        g.setColor(blackDotColor);
                        int x = onRand.nextInt(dotAreaWidth) + boundary;
                        int y = onRand.nextInt(dotAreaHeight) + boundary;
                        g.fillOval(x, y, dotWidth, dotHeight);
                        totalBlackDots++;
                        totalWhiteDots--;
                        totalOnRand++;
                    }
                    gPanel.drawImage(im, 0, 0, inputPanel);
                    break;
                case KeyEvent.VK_NUMPAD3:
                    for (i = 0; i < smallChange; i++) {
                        if (totalWhiteDots == 0) {
                            break;
                        }
                        if (totalOnRand == totalDots) {
                            onRand.setSeed(seed1);
                            totalOnRand = 0;
                        }
                        g.setColor(blackDotColor);
                        int x = onRand.nextInt(dotAreaWidth) + boundary;
                        int y = onRand.nextInt(dotAreaHeight) + boundary;
                        g.fillOval(x, y, dotWidth, dotHeight);
                        totalBlackDots++;
                        totalWhiteDots--;
                        totalOnRand++;
                    }
                    gPanel.drawImage(im, 0, 0, inputPanel);
                    break;

            }

        }

        percentWhite = (double) totalWhiteDots / (double) totalDots;

        if (!closedProperly) {
            dotOkButton.doClick();
            JOptionPane.showMessageDialog(null, "You must choose an option.  Do NOT use the Close Box", "alert", JOptionPane.ERROR_MESSAGE);
        }
        g.dispose();
        gPanel.dispose();
    }

    public synchronized void getFrameResponse(Color backColor, Color frameColor, int halfSize) {

        JFrame nullFrame = new JFrame();
        respFrame = new JDialog(nullFrame);
        respFrame.setTitle("ResponseV3");
        respFrame.addKeyListener(this);

        // dont let people close out
        respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);


        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        respFrame.setSize(d.width, d.height);
        respFrame.setLocation(0, 0);

        BlankPanel inputPanel = new BlankPanel(backColor);
        BlankPanel buttonPanel = new BlankPanel(backColor);

        dotOkButton = new JButton("OK");
        buttonPanel.add(dotOkButton);
        dotOkButton.addActionListener(this);

        synchronized (this) {
            respFrame.show();
            respFrame.getContentPane().setLayout(new BorderLayout());
            respFrame.getContentPane().add(inputPanel, "Center");
            respFrame.getContentPane().add(buttonPanel, "South");
            respFrame.show();
        }

        //One must free the thread for a short time to allow the frame to build before drawing on it.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        closedProperly = true;
        inputDone = false;

        respFrame.addWindowListener(new WindowAdapter() {

            public synchronized void windowClosed(WindowEvent e) {
            }

            public synchronized void windowClosing(WindowEvent e) {
                //							inputDone = true;
                //							closedProperly = false;
            }
        });

        Dimension d1 = inputPanel.getSize();


        Image im = respFrame.createImage(d1.width, d1.height);
        Graphics g = im.getGraphics();
        g.setColor(backColor);
        g.fillRect(0, 0, d.width, d.height);
        Graphics gPanel = inputPanel.getGraphics();


        int i, j;

        int[] xPoints = {d1.width / 2 - halfSize, d1.width / 2 + halfSize, d1.width / 2 + halfSize, d1.width / 2 - halfSize};
        int[] yPoints = {d1.height / 2 - halfSize, d1.height / 2 - halfSize, d1.height / 2 + halfSize, d1.height / 2 + halfSize};
        int nPoints = 4;

        g.setColor(frameColor);
        g.drawPolygon(xPoints, yPoints, nPoints);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        gPanel.drawImage(im, 0, 0, inputPanel);

        int keyHit;
        while (!getInputDone()) {
            respFrame.toFront();
            respFrame.requestFocus();
            keyHit = getKeyCode();
            keyCode = 0;
            switch (keyHit) {
                case KeyEvent.VK_ENTER:
                    dotOkButton.doClick();
                    break;
                case KeyEvent.VK_NUMPAD9:
                    if (yPoints[1] > d1.height / 2 - halfSize) {
                        yPoints[1] -= 1;
                        yPoints[2] += 1;
                        g.setColor(backColor);
                        g.fillRect(0, 0, d1.width, d1.height);
                        g.setColor(frameColor);
                        g.drawPolygon(xPoints, yPoints, nPoints);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD3:
                    if (yPoints[1] < d1.height / 2) {
                        yPoints[1] += 1;
                        yPoints[2] -= 1;
                        g.setColor(backColor);
                        g.fillRect(0, 0, d1.width, d1.height);
                        g.setColor(frameColor);
                        g.drawPolygon(xPoints, yPoints, nPoints);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD5:
                    yPoints[0] = d1.height / 2 - halfSize;
                    yPoints[1] = d1.height / 2 - halfSize;
                    yPoints[2] = d1.height / 2 + halfSize;
                    yPoints[3] = d1.height / 2 + halfSize;
                    g.setColor(backColor);
                    g.fillRect(0, 0, d1.width, d1.height);
                    g.setColor(frameColor);
                    g.drawPolygon(xPoints, yPoints, nPoints);
                    gPanel.drawImage(im, 0, 0, inputPanel);
                    break;
                case KeyEvent.VK_NUMPAD7:
                    if (yPoints[0] > d1.height / 2 - halfSize) {
                        yPoints[0] -= 1;
                        yPoints[3] += 1;
                        g.setColor(backColor);
                        g.fillRect(0, 0, d1.width, d1.height);
                        g.setColor(frameColor);
                        g.drawPolygon(xPoints, yPoints, nPoints);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD1:
                    if (yPoints[0] < d1.height / 2) {
                        yPoints[0] += 1;
                        yPoints[3] -= 1;
                        g.setColor(backColor);
                        g.fillRect(0, 0, d1.width, d1.height);
                        g.setColor(frameColor);
                        g.drawPolygon(xPoints, yPoints, nPoints);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;

            }

        }

        int rightSide = yPoints[2] - yPoints[1];
        int leftSide = yPoints[3] - yPoints[0];
        double baseSide = (double) 2 * halfSize;
        double tmpDist;

        if (rightSide > leftSide) {
            tmpDist = (double) yPoints[0] - (double) yPoints[1];
            frameShortSide = "left";
        } else {
            tmpDist = (double) yPoints[1] - (double) yPoints[0];
            frameShortSide = "right";
        }

        if (tmpDist == 0) {
            frameAngle = 90;
            frameShortSide = "equal";
        } else {
            double tmp = (baseSide * baseSide) + (tmpDist * tmpDist);
            double hypot = Math.sqrt(tmp);
            frameAngle = Math.toDegrees(Math.asin((baseSide) / hypot));
        }

        if (!closedProperly) {
            dotOkButton.doClick();
            JOptionPane.showMessageDialog(null, "You must choose an option.  Do NOT use the Close Box", "alert", JOptionPane.ERROR_MESSAGE);
        }
        g.dispose();
        gPanel.dispose();
    }

    public synchronized int getLineResponse(Color backColor, Color lineColor, int largeChange, int mediumChange, int smallChange, int lineLength, int lineThickness, int border) {

        JFrame nullFrame = new JFrame();
        respFrame = new JDialog(nullFrame);
        respFrame.setTitle("ResponseV3");
        respFrame.addKeyListener(this);

        // dont let people close out
        respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);


        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        respFrame.setSize(d.width, d.height);
        respFrame.setLocation(0, 0);

        // hide cursor: begin
        Image noImage = tk.getImage("noFile");
        Point pZ = new Point(0, 0);
        Cursor curs = tk.createCustomCursor(noImage, pZ, "noCurs");
        respFrame.setCursor(curs);
        // hide cursor: end

        BlankPanel inputPanel = new BlankPanel(backColor);
        BlankPanel buttonPanel = new BlankPanel(backColor);

        dotOkButton = new JButton("OK");
        buttonPanel.add(dotOkButton);
        dotOkButton.addActionListener(this);

        synchronized (this) {
            respFrame.show();
            respFrame.getContentPane().setLayout(new BorderLayout());
            respFrame.getContentPane().add(inputPanel, "Center");
            respFrame.getContentPane().add(buttonPanel, "South");
            respFrame.show();
        }

        //One must free the thread for a short time to allow the frame to build before drawing on it.

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        closedProperly = true;
        inputDone = false;

        respFrame.addWindowListener(new WindowAdapter() {

            public synchronized void windowClosed(WindowEvent e) {
            }

            public synchronized void windowClosing(WindowEvent e) {
                //							inputDone = true;
                //							closedProperly = false;
            }
        });

        Dimension d1 = inputPanel.getSize();
        int boundary = 20;
        int drawAreaHeight = d1.height - (2 * border);
        int drawAreaWidth = d1.width - (2 * border);

        Image im = respFrame.createImage(d1.width, d1.height);
        Graphics g = im.getGraphics();
        g.setColor(backColor);
        g.fillRect(0, 0, d.width, d.height);
        Graphics gPanel = inputPanel.getGraphics();


        int i, j;

        int x = d1.width / 2 - lineLength / 2;
        int y = d1.height / 2;
        g.setColor(lineColor);

        g.fillRect(x, y, lineLength, lineThickness);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        gPanel.drawImage(im, 0, 0, inputPanel);

        int keyHit;
        while (!getInputDone()) {
            respFrame.toFront();
            respFrame.requestFocus();
            keyHit = getKeyCode();
            keyCode = 0;
            switch (keyHit) {
                case KeyEvent.VK_ENTER:
                    dotOkButton.doClick();
                    break;
                case KeyEvent.VK_NUMPAD9:
                    if (lineLength <= (drawAreaWidth - smallChange)) {
                        g.setColor(backColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        lineLength += smallChange;
                        x = d1.width / 2 - lineLength / 2;
                        y = d1.height / 2;

                        g.setColor(lineColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD3:
                    if (lineLength >= (2 + smallChange)) {
                        g.setColor(backColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        lineLength -= smallChange;
                        x = d1.width / 2 - lineLength / 2;
                        y = d1.height / 2;

                        g.setColor(lineColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD8:
                    if (lineLength <= (drawAreaWidth - mediumChange)) {
                        g.setColor(backColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        lineLength += mediumChange;
                        x = d1.width / 2 - lineLength / 2;
                        y = d1.height / 2;

                        g.setColor(lineColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD2:
                    if (lineLength >= (2 + mediumChange)) {
                        g.setColor(backColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        lineLength -= mediumChange;
                        x = d1.width / 2 - lineLength / 2;
                        y = d1.height / 2;

                        g.setColor(lineColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD7:
                    if (lineLength <= (drawAreaWidth - largeChange)) {
                        g.setColor(backColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        lineLength += largeChange;
                        x = d1.width / 2 - lineLength / 2;
                        y = d1.height / 2;

                        g.setColor(lineColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD1:
                    if (lineLength >= (2 + largeChange)) {
                        g.setColor(backColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        lineLength -= largeChange;
                        x = d1.width / 2 - lineLength / 2;
                        y = d1.height / 2;

                        g.setColor(lineColor);
                        g.fillRect(x, y, lineLength, lineThickness);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;

            }

        }

        if (!closedProperly) {
            dotOkButton.doClick();
            JOptionPane.showMessageDialog(null, "You must choose an option.  Do NOT use the Close Box", "alert", JOptionPane.ERROR_MESSAGE);
        }
        g.dispose();
        gPanel.dispose();

        return lineLength;
    }

    public synchronized int getCircleResponse(Color backColor, Color lineColor, int largeChange, int mediumChange, int smallChange, int circleDiam, int lineThickness, int border) {

        JFrame nullFrame = new JFrame();
        respFrame = new JDialog(nullFrame);
        respFrame.setTitle("ResponseV3");
        respFrame.addKeyListener(this);

        // dont let people close out
        respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);


        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        respFrame.setSize(d.width, d.height);
        respFrame.setLocation(0, 0);

        // hide cursor: begin
        Image noImage = tk.getImage("noFile");
        Point pZ = new Point(0, 0);
        Cursor curs = tk.createCustomCursor(noImage, pZ, "noCurs");
        respFrame.setCursor(curs);
        // hide cursor: end


        BlankPanel inputPanel = new BlankPanel(backColor);
        BlankPanel buttonPanel = new BlankPanel(backColor);

        dotOkButton = new JButton("OK");
        buttonPanel.add(dotOkButton);
        dotOkButton.addActionListener(this);

        synchronized (this) {
            respFrame.show();
            respFrame.getContentPane().setLayout(new BorderLayout());
            respFrame.getContentPane().add(inputPanel, "Center");
            respFrame.getContentPane().add(buttonPanel, "South");
            respFrame.show();
        }

        //One must free the thread for a short time to allow the frame to build before drawing on it.

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        closedProperly = true;
        inputDone = false;

        respFrame.addWindowListener(new WindowAdapter() {

            public synchronized void windowClosed(WindowEvent e) {
            }

            public synchronized void windowClosing(WindowEvent e) {
                //							inputDone = true;
                //							closedProperly = false;
            }
        });

        Dimension d1 = inputPanel.getSize();
        int boundary = 20;
        int drawAreaHeight = d1.height - (2 * border);
        int drawAreaWidth = d1.width - (2 * border);

        Image im = respFrame.createImage(d1.width, d1.height);
        Graphics g = im.getGraphics();
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(backColor);
        g2.fillRect(0, 0, d1.width, d1.height);
        g2.setStroke(new BasicStroke(lineThickness));

//    g.setColor (backColor);
//    g.fillRect(0,0,d.width,d.height);
        Graphics gPanel = inputPanel.getGraphics();


        int i, j;

        int x = d1.width / 2 - circleDiam / 2;
        int y = d1.height / 2 - circleDiam / 2;
        g2.setColor(lineColor);
        g2.drawOval(x, y, circleDiam, circleDiam);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        gPanel.drawImage(im, 0, 0, inputPanel);

        int keyHit;
        while (!getInputDone()) {
            respFrame.toFront();
            respFrame.requestFocus();
            keyHit = getKeyCode();
            keyCode = 0;
            switch (keyHit) {
                case KeyEvent.VK_ENTER:
                    dotOkButton.doClick();
                    break;
                case KeyEvent.VK_NUMPAD9:
                    if (circleDiam <= (drawAreaWidth - smallChange)) {
                        g2.setColor(backColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        circleDiam += smallChange;
                        x = d1.width / 2 - circleDiam / 2;
                        y = d1.height / 2 - circleDiam / 2;

                        g2.setColor(lineColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD3:
                    if (circleDiam >= (2 + smallChange)) {
                        g2.setColor(backColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        circleDiam -= smallChange;
                        x = d1.width / 2 - circleDiam / 2;
                        y = d1.height / 2 - circleDiam / 2;

                        g2.setColor(lineColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD8:
                    if (circleDiam <= (drawAreaWidth - mediumChange)) {
                        g2.setColor(backColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        circleDiam += mediumChange;
                        x = d1.width / 2 - circleDiam / 2;
                        y = d1.height / 2 - circleDiam / 2;

                        g2.setColor(lineColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD2:
                    if (circleDiam >= (2 + mediumChange)) {
                        g2.setColor(backColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        circleDiam -= mediumChange;
                        x = d1.width / 2 - circleDiam / 2;
                        y = d1.height / 2 - circleDiam / 2;

                        g2.setColor(lineColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD7:
                    if (circleDiam <= (drawAreaWidth - largeChange)) {
                        g2.setColor(backColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        circleDiam += largeChange;
                        x = d1.width / 2 - circleDiam / 2;
                        y = d1.height / 2 - circleDiam / 2;

                        g2.setColor(lineColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;
                case KeyEvent.VK_NUMPAD1:
                    if (circleDiam >= (2 + largeChange)) {
                        g2.setColor(backColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);

                        circleDiam -= largeChange;
                        x = d1.width / 2 - circleDiam / 2;
                        y = d1.height / 2 - circleDiam / 2;

                        g2.setColor(lineColor);
                        g2.drawOval(x, y, circleDiam, circleDiam);
                        gPanel.drawImage(im, 0, 0, inputPanel);
                    }
                    break;

            }

        }

        if (!closedProperly) {
            dotOkButton.doClick();
            JOptionPane.showMessageDialog(null, "You must choose an option.  Do NOT use the Close Box", "alert", JOptionPane.ERROR_MESSAGE);
        }
        g.dispose();
        gPanel.dispose();

        return circleDiam;
    }

    public boolean checkResponseAccuracy(int probeType) {
        if (probeType == 0) {
            correct = userChoice == sameChoice;
        } else {
            correct = userChoice == diffChoice;
        }
        return correct;
    }
    
    public boolean checkResponseAccuracy(int probeType, char response){
        if (probeType == 0) {
            correct = response == sameChoice;
        } else {
            correct = response == diffChoice;
        }
        return correct;
    }
    
    public boolean checkResponseAccuracy(String resp, int[] correctResp){
        if(correctResp[0] == 0){
            correct = resp.charAt(0) == sameChoice && resp.length() == correctResp[1];
        }
        else{
            correct = resp.charAt(0) == diffChoice && resp.length() == correctResp[1];
        }
        return correct;
    }

    public boolean checkResponseAccuracy(String correctResp, String userResp) {
        userChoice = sameChoice = diffChoice = ' ';
        correct = correctResp.equals(userResp);
        return correct;
    }
    
    public void checkUserResponse(JFrame parent){
        if (userChoice != sameChoice && userChoice != diffChoice && userChoice != '~')
            displayNotificationFrame(parent, "Your fingers are misplaced. Please reposition you fingers and click OK to continue");
    }
    
    public void checkUserResponse(BlankPanel endPanel){
//        Graphics g = endPanel.getGraphics();
//        FontMetrics fm = endPanel.getFontMetrics(new Font("Serrif", Font.PLAIN, 12));
//        Dimension d = endPanel.getSize();
//        char key = getUserChoice();
//        
//        g.setColor(Color.WHITE);
//        
//        int xDist1 = fm.stringWidth("Your fingers are misplace.");
//        int xDist2 = fm.stringWidth("Please reposition your fingers");
//        int xDist3 = fm.stringWidth("and press any key to continue");
//        
//        if (userChoice != sameChoice && userChoice != diffChoice && userChoice != '~') {
//           g.drawString("Your fingers are misplaced.", d.width / 2 - (xDist1/2), d.height / 2 + 40);
//           g.drawString("Please reposition your fingers", d.width / 2 - (xDist2/2), d.height / 2 + 80);
//           g.drawString("and press any key to continue.", d.width / 2 - (xDist3/2), d.height / 2 + 120);
//
//        
//        
//        while (getUserChoice() == key) {
//        }
//        setUserChoice(key);
//        }
        if (userChoice != sameChoice && userChoice != diffChoice && userChoice != '~')
            JOptionPane.showMessageDialog(endPanel, "Your fingers are misplaced. Please reposition you fingers and click OK to continue");
    }
    
    public synchronized void giveFeedback(BlankPanel endPanel) {
        Graphics g = endPanel.getGraphics();
        Dimension d = endPanel.getSize();
        char key = getUserChoice();
        JLabel feedbackLabel = new JLabel();
        FontMetrics fm = null;
        int wOffset = 0, hOffset = 0;

        g.setColor(Color.white);
        Font f = new Font("Serif", Font.BOLD, 24);
        g.setFont(f);

        delay(200);

        if (correct) {
            feedbackLabel.setText("Correct");
            fm = feedbackLabel.getFontMetrics(f);
            wOffset = fm.stringWidth(feedbackLabel.getText()) / 2;
            hOffset = fm.getHeight() / 2;
            g.drawString("Correct", d.width / 2 - wOffset, d.height / 2 + hOffset);
        }
        else if (userChoice != sameChoice && userChoice != diffChoice && userChoice != '~') {
            g.drawString("Your fingers are misplaced.", d.width / 2 - 130, d.height / 2 + 40);
            g.drawString("Please reposition your fingers", d.width / 2 - 139, d.height / 2 + 80);
            g.drawString("and press enter to continue.", d.width / 2 - 137, d.height / 2 + 120);
            while (getUserChoice() == key) {
            }
            setUserChoice(key);
        }
        else if (!correct) {
            feedbackLabel.setText("Incorrect");
            fm = feedbackLabel.getFontMetrics(f);
            wOffset = fm.stringWidth(feedbackLabel.getText()) / 2;
            hOffset = fm.getHeight() / 2;
            g.drawString("Incorrect", d.width / 2 - wOffset, d.height / 2 + hOffset);
        }
        

        feedbackLabel = null;
        endPanel.setVisible(true);
        delay(500);
        g.dispose();
    }
    
    public synchronized void giveFeedback(JFrame frame, BlankPanel endPanel){
        Graphics g = endPanel.getGraphics();
        Dimension d = endPanel.getSize();
        char key = getUserChoice();
        JLabel feedbackLabel = new JLabel();
        FontMetrics fm = null;
        int wOffset = 0, hOffset = 0;

        g.setColor(Color.white);
        Font f = new Font("Serif", Font.BOLD, 24);
        g.setFont(f);

        delay(200);

        if (correct) {
            displayNotificationFrame(frame, "Correct");
        }
       // else if(userChoice == '*'){
        //    displayNotificationFrame(frame, "There was an error registering your response. Please click OK to continue.");
        //}
        //else if (userChoice != sameChoice && userChoice != diffChoice) {
        //    displayNotificationFrame(frame, "Your fingers are misplaced. Please reposition your fingers and press enter to continue.");
        //}
        
        else if (!correct) {
            displayNotificationFrame(frame, "Incorrect");
        }


        feedbackLabel = null;
        endPanel.setVisible(true);
        delay(500);
        g.dispose();
    }
    
    /**
     * Gives the Feedback, allowing specification of feedback type and delays
     * @param frame         Container frame
     * @param endPanel      Panel that displays feedback
     * @param feedback      Feedback type
     * @param delay1        First delay
     * @param delay2        Second delay
     */
    @Override
    public synchronized void giveFeedbackSwitch(JFrame frame,
            BlankPanel endPanel, String feedback, int delay1, int delay2){
        giveFeedbackSwitch(frame, endPanel, feedback, delay1, delay2, null,
                "none");
    }

    public synchronized void giveFeedbackSwitch(JFrame frame,
            BlankPanel endPanel, String feedback, int delay1, int delay2,
            JDialog dialog, String dialogControl) {
        
        Graphics g = endPanel.getGraphics();
        Dimension d = endPanel.getSize();
        char key = getUserChoice();
        JLabel feedbackLabel = new JLabel();
        FontMetrics fm = null;
        int wOffset = 0, hOffset = 0;
        boolean showDialog = false;

        g.setColor(Color.white);
        Font f = new Font("Serif", Font.BOLD, 24);
        g.setFont(f);

        delay(delay1);

        // If the answer is correct and feedback is ALL then display feedback
        if (correct && feedback.equalsIgnoreCase("ALL")) {
           drawStringCentered(g, d, "Correct", f);
        }
        else if ((!correct && feedback.equalsIgnoreCase("ALL")) ||
            (!correct && feedback.equalsIgnoreCase("INCORRECT"))) {
            drawStringCentered(g, d, "Incorrect", f);
        }
        else if(userChoice == '*'){
            displayNotificationFrame(frame,
                "There was an error registering your response."
                    + " Please click OK to continue.");
        }
        else if (userChoice != sameChoice && userChoice != diffChoice) {
            displayNotificationFrame(frame,
                "Your fingers are misplaced. Please reposition your fingers"
                    + " and press enter to continue.");
        }
        // If the answer is incorrect
        // and the feedback is either all or incorrect, display message
        if(!feedback.equalsIgnoreCase("NONE")) {
            feedbackLabel = null;
            endPanel.setVisible(true);
            delay(delay2);

            endPanel.repaint();

            if(correct && dialogControl.equalsIgnoreCase("correct") ||
                    !correct && dialogControl.equalsIgnoreCase("incorrect") ||
                    dialogControl.equalsIgnoreCase("all")) {
                // Center dialog
                dialog.setLocation((frame.getWidth()  - dialog.getWidth()) / 2, 
                        (frame.getHeight() - dialog.getHeight()) / 2);
                
                // Make it visible
                dialog.setVisible(true);
            }

            //g.dispose();
        }
    }

    private void drawStringCentered(Graphics g, Dimension dimensions, String text, Font font) {
        JLabel feedbackLabel = new JLabel();
        feedbackLabel.setText(text);
        FontMetrics fm = feedbackLabel.getFontMetrics(font);
        int wOffset = fm.stringWidth(feedbackLabel.getText()) / 2;
        int hOffset = fm.getHeight() / 2;
        g.drawString(text, dimensions.width / 2 - wOffset, dimensions.height / 2 + hOffset);
    }

    public synchronized void giveFeedback(BlankPanel endPanel, String responseOrder, JFrame frame) {
        Graphics g = endPanel.getGraphics();
        Dimension d = endPanel.getSize();
        char key = getUserChoice();
        JLabel feedbackLabel = new JLabel();
        FontMetrics fm = null;
        int wOffset = 0, hOffset = 0;
        
        resetUserChoice();

        g.setColor(Color.white);
        Font f = new Font("Serif", Font.BOLD, 24);
        g.setFont(f);

        delay(200);

        if (correct) {
            feedbackLabel.setText("Correct");
            fm = feedbackLabel.getFontMetrics(f);
            wOffset = fm.stringWidth(feedbackLabel.getText()) / 2;
            hOffset = fm.getHeight() / 2;
            g.drawString("Correct", d.width / 2 - wOffset, d.height / 2 + hOffset);
        }
        else if (responseOrder.charAt(0) == '*'){
            displayNotificationFrame(frame, "Sorry, there was an issue registering your response. Please click OK to continue.");
        }
        else if (responseOrder.charAt(0) != sameChoice && responseOrder.charAt(0) != diffChoice) {
//            g.drawString("Your fingers are misplaced.", d.width / 2 - 130, d.height / 2 + 40);
//            g.drawString("Please reposition your fingers", d.width / 2 - 139, d.height / 2 + 80);
//            g.drawString("and press any key to continue.", d.width / 2 - 137, d.height / 2 + 120);
//            while (getUserChoice() == '~') {
//            }
//            setUserChoice(key);
            displayNotificationFrame(frame, "Your fingers are misplaced. Please reposition your fingers and press OK to continue.");
        }
        else {
            feedbackLabel.setText("Incorrect");
            fm = feedbackLabel.getFontMetrics(f);
            wOffset = fm.stringWidth(feedbackLabel.getText()) / 2;
            hOffset = fm.getHeight() / 2;
            g.drawString("Incorrect", d.width / 2 - wOffset, d.height / 2 + hOffset);
        }
        

        feedbackLabel = null;
        endPanel.setVisible(true);
        delay(500);
        g.dispose();
    }

    public synchronized void giveSoundFeedback(BlankPanel endPanel) {
        Graphics g = endPanel.getGraphics();
        Dimension d = endPanel.getSize();
        Toolkit tk = Toolkit.getDefaultToolkit();

        char key = getUserChoice();

        g.setColor(Color.white);
        g.setFont(new Font("Serif", Font.BOLD, 24));

        delay(200);

        if (!correct) {
            tk.beep();
        }
        if (userChoice != sameChoice && userChoice != diffChoice && userChoice != '~') {
            g.drawString("Your fingers are misplaced.", d.width / 2 - 130, d.height / 2 + 40);
            g.drawString("Please reposition your fingers", d.width / 2 - 139, d.height / 2 + 80);
            g.drawString("and press enter to continue.", d.width / 2 - 137, d.height / 2 + 120);
            while (getUserChoice() == key) {
            }
            setUserChoice(key);
        }

        endPanel.setVisible(true);
        delay(500);
        g.dispose();
    }

    public synchronized void delay(int milliseconds) {
        long timeA, timeB, timeC;
        timeA = new Date().getTime();
        do {
            timeB = new Date().getTime();
            timeC = timeB - timeA;
        } while (timeC < milliseconds);
    }

    public void getButtonMap(String labelSame, String labelDiff) {
        String out;
        out = "Please use the  '" + sameChoice + "'  key to indicate  '" + labelSame + "'  and the  '" + diffChoice + "'  to indicate  '" + labelDiff + "'";
        JOptionPane.showMessageDialog(null, out, "information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void getButtonMap(String labelSame, String labelDiff, JFrame parent){
        String out;
        out = "Please use the  '" + sameChoice + "'  key to indicate  '" + labelSame + "'  and the  '" + diffChoice + "'  to indicate  '" + labelDiff + "'";
        
        displayNotificationFrame(parent, out);
    }
    
    public void getMouseMap(String leftV, String rightV){
        String out = "Please left click to indicate " + leftV + " and right click to indicate " + rightV;
        JOptionPane.showMessageDialog(null, out, "information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void getMouseMap(String leftV, String rightV, JFrame parent){
        String out = "Please left click to indicate " + leftV + " and right click to indicate " + rightV;
        displayNotificationFrame(parent, out);
    }

    public void getButtonMap(String prompt) {
        JOptionPane.showMessageDialog(null, prompt, "information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void getButtonMap(String prompt, JFrame parent){
        displayNotificationFrame(parent, prompt);
    }

    public boolean getCorrect() {
        return this.correct;
    }

    public char getUserChoice() {
        return userChoice;
    }

    public void setSameChoice(char a) {
        sameChoice = a;
    }

    public void setDiffChoice(char a) {
        diffChoice = a;
    }

    public void setUserChoice(char a) {
        userChoice = a;
    }

    public void resetUserChoice() {
        userChoice = '~';
        keyboardInputDone = false;
    }

    public char getSameChoice() {
        return sameChoice;
    }

    public char getDiffChoice() {
        return diffChoice;
    }

    public int getSliderValue() {
        return sliderValue;
    }

    public long getRT() {
        return RT;
    }

    public long getLoopsPerMS() {
        return loopsPerMS;
    }

    public long getAddedMS() {
        return addedMS;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public String getAllSpecs() {
        String respSpecs = userChoice + "\t" + RT + "\t" + sliderValue + "\t" + sliderRT + "\t" + textValue + "\t" + textRT + "\t" + correct;
        return respSpecs;
    }

    public String getTwoChoiceSpecs() {
        String respSpecs = userChoice + "\t" + RT + "\t" + correct;
        return respSpecs;
    }

    public String getSliderSpecs() {
        String respSpecs = sliderValue + "\t" + sliderRT;
        return respSpecs;
    }

    public String getTextSpecs() {
        String respSpecs = textValue + "\t" + textRT;
        return respSpecs;
    }

    public String getSelectedCheckBoxes(String[] options){
                    String response = "";
            for(int i = 0; i < boxId.size(); i ++){
                response += options[boxId.get(i)];
            }
            
            return response;
    }
    public boolean getInputDone() {
        return inputDone;
    }

    public long getSliderRT() {
        return sliderRT;
    }
    
    public int getMouseClickButton(){
        return mouseClickButton;
    }
    
    public void resetMouseClickButton(){
        mouseClickButton = -1;
    }

    public long getTextRT() {
        return textRT;
    }

    public String getTextValue() {
        return textValue;
    }

    public double getPercentWhite() {
        return percentWhite;
    }

    public double getFrameAngle() {
        return frameAngle;
    }

    public String getFrameShortSide() {
        return frameShortSide;
    }
    public String getRadioResponse(){
        return radioValue;
    }
    public long getRadioRT(){
        return radioRT;
    }
    
    private DualMouseClickListener dmcl = new DualMouseClickListener();
    public synchronized long getTimedMouseClickResponse(Component parent){
        long timeA, timeB, tmpTime;
        long overRun;
        parent.addMouseListener(dmcl);
        
        overRun = 0;
        timeA = new Date().getTime();
        timeB = new Date().getTime();
        tmpTime = timeA;
        
        resetMouseClickButton();
        int x = getMouseClickButton();

        /**	The following polls a variable to make the computer wait for the mouse response
         **/
        while (x == -1) {
            x = getMouseClickButton();
            System.out.print("");
            timeB = new Date().getTime();
            if (tmpTime != timeB) {
                overRun = 0;
                tmpTime = timeB;
            } else {
                overRun++;
            }
        }
        addedMS = overRun / loopsPerMS;
        RT = (timeB - timeA) + (overRun / loopsPerMS);
        
        parent.removeMouseListener(dmcl);

        return RT;
    }
    
    private class DualMouseClickListener implements MouseListener{

        public void mouseClicked(MouseEvent e) {
            //do nothing
        }

        public void mousePressed(MouseEvent e) {
            int temp  = e.getButton();
            
            
            switch(temp){
                case 1:
                    mouseClickButton = temp;
                    break;
                case 3:
                    mouseClickButton = temp;
                    break;
                    
                default:
                    break;
            }
            
        }

        public void mouseReleased(MouseEvent e) {
            //do nothing
        }

        public void mouseEntered(MouseEvent e) {
            //do nothing
        }

        public void mouseExited(MouseEvent e) {
            //do nothing
        }
        
    }
    
    private disableMouseListener dml = new disableMouseListener();
    private boolean mouseEnabled = true;
    private JFrame mouseFrame;
    
    public void disableMouse(JFrame f){
        mouseFrame = f;
        f.addMouseMotionListener(dml);
        mouseEnabled = false;
    }
    
    public void enableMouse(JFrame f){
        f.removeMouseMotionListener(dml);
        mouseEnabled = true;
    }
    
    private class disableMouseListener implements MouseMotionListener{

        public void mouseDragged(MouseEvent e) {
            //do nothing
        }

        public void mouseMoved(MouseEvent e) {
            Experiment.resetMouseToCenterScreen();
        }
        
    }
    
    
    private volatile int keyCode = 0;
    private int mouseClickButton = -1;
    private double percentWhite;
    private volatile int sliderValue = 0; //accessed by multiple threads - declare volatile
    protected boolean isSliderMoved = false;
    protected String textValue;
    private String radioValue;
    private long sliderRT;
    protected long textRT;
    private long radioRT;
    private long RT = 99999;
    private int respListSize = -1, currentRespSize = 0;
    private char sameChoice = '+';
    private char diffChoice = '-';
    private volatile char userChoice = '~';
    private boolean correct = false;
    protected JDialog respFrame;
    private JButton sliderOkButton;
    protected JButton textOkButton;
    private JButton rbOkButton;
    private JButton dotOkButton;
    private JButton radioOkButton;
    private JSlider slider;
    private ButtonGroup buttonGroup;
    protected JTextField textInput;
    protected long startTime;
    protected long endTime;
    protected volatile boolean inputDone;
    private double frameAngle;
    private String frameShortSide;
    protected boolean closedProperly = true;
    private boolean isMultipleButton = false;//flag used in key listener if true listens for multiple responses
    private long loopsPerMS = 0;
    private long addedMS = 0;
    private NumPadResponse numPadResponse;
    private boolean legacy = true; //Used in actionPerformed for old JDialog numpad
    private JCheckBox[] buttons;
    private ArrayList<Integer> boxId;
    private ArrayList responseList;
}
