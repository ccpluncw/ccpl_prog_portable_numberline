package ccpl.lib;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
//import corejava.*;
   
/*****	The DRAWEXPFRAME sets up a frame the size of the screen to run the experiment
******	in.
*****/
public class DrawExpFrame extends JFrame
{
   private static int screenHeight, screenWidth;
   private final boolean USE_LISTENER = true;
   private Cursor curs;
   private Cursor show;
   
   public DrawExpFrame(Response resp)
   {
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      
      if(USE_LISTENER)
          addKeyListener (resp);
      
      //assign bindings and issue an error message if it fails
      else{
          boolean flag;
          flag = assignKeyBindings(resp);
          if(flag == false){
              JOptionPane.showMessageDialog(null, "Error Assigning Key Bindings", null, JOptionPane.ERROR_MESSAGE);
              System.exit(0);
          }
      }  
     assignKeyBindings(resp);
      
     
     
    Toolkit tk = Toolkit.getDefaultToolkit ();
    Dimension d = tk.getScreenSize ();
    screenHeight = d.height;
    screenWidth = d.width;
    setSize(screenWidth, screenHeight);
    
    BufferedImage blankCursor = null;
    BufferedImage showCursor = null;
    BufferedImage cursorImg = new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
    
    //try {
        
        //blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0,0), "blank cursor");
           //blankCursor = ImageIO.read(getClass().getResource("/app_imgs/blank1.gif"));
           //showCursor = ImageIO.read(getClass().getResource("/app_imgs/transparent.png"));
    //} catch (IOException ex) {
    //       Logger.getLogger(DrawExpFrame.class.getName()).log(Level.SEVERE, null, ex);
    //}
    
    //curs = tk.createCustomCursor(blankCursor, new Point(0,0), "blank");
    curs = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
    //show = tk.createCustomCursor(showCursor, new Point(0,0), "show");
    
    setBackground(Color.BLACK);
    getContentPane().setBackground(Color.BLACK);
    setResizable(false);
    setLocation (0, 0);
    setFocusable(true);
    
    //ENABLES FULL SCREEN FUNCTION ONLY FOR MAC OS
    //OTHER OS SIMPLY SETTING THE WINDOW TO UNDECORATED ACCOMPLISHES THE SAME GOAL
    if(System.getProperty("os.name").startsWith("Mac")){//  equalsIgnoreCase("mac os x"))
      // TODO: Figure out how to fix this on non-Mac machines
      // com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(this, true);
//        com.apple.eawt.Application.getApplication().requestToggleFullScreen(this);
    }
    
    else
        setUndecorated(true); //Hides minimize and maximize buttons on jframe title bar
    setVisible(true);
    
   }
   
   private boolean assignKeyBindings(Response resp){
        ActionMap actionMap = getRootPane().getActionMap();
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;//changed #1
        InputMap inputMap = getRootPane().getInputMap(condition);
        
        KeyStroke quit = KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke mouse = KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK);
        
        inputMap.put(quit, "quitAction");
        inputMap.put(mouse, "mouseAction");
        
        //actionMap.put("sameAction", resp.returnAction());
        //actionMap.put("diffAction", resp.returnAction());
        //control q quits
        actionMap.put("quitAction", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                System.exit(0);}});
        actionMap.put("mouseAction", resp.returnMouseAction());
        //assignAllKeyActions(resp, inputMap, actionMap);
        
        return true;
   }
   
    private void assignAllKeyActions(Response resp, InputMap inputMap, ActionMap actionMap){
        int charCode = 0;
        do{
            KeyStroke currentStroke = KeyStroke.getKeyStroke(charCode, 0);
            inputMap.put(currentStroke, "keyAction");
            charCode ++;
        }while(charCode < 128);
        //assign the action
        actionMap.put("keyAction", resp.returnAction());
        
        //handles macro keys such as shift ctrl and alt
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, KeyEvent.SHIFT_DOWN_MASK), "keyAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, KeyEvent.CTRL_DOWN_MASK), "keyAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, KeyEvent.ALT_DOWN_MASK), "keyAction");
        inputMap.put(KeyStroke.getKeyStroke(157, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "keyAction");//handles command key
   }
   
   public void setTwoButtonResponse(char s, char d){
       KeyStroke same = KeyStroke.getKeyStroke(s);
       KeyStroke diff = KeyStroke.getKeyStroke(d);
       
       int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
       InputMap inputMap = getRootPane().getInputMap(condition);
       
       inputMap.put(same, "sameAction");
       inputMap.put(diff, "diffAction");
   }
   
   public void hideCursor () {
        //Cursor curs = tKit.createCustomCursor (tKit.createImage(""), new Point(), "blank");
       Toolkit tk = Toolkit.getDefaultToolkit();
       curs = tk.createCustomCursor(tk.createImage(""), new Point(), "blank");
       setCursor (curs);
       Experiment.resetMouseToCenterScreen();
   }
   
   public void hideCursor(int x, int y) {
       Robot bot;
       try {
            bot = new Robot();
            bot.mouseMove(x, y);    
            bot.mousePress(InputEvent.BUTTON1_MASK);
            bot.mouseRelease(InputEvent.BUTTON1_MASK);
       } catch (AWTException ex) {
           Logger.getLogger(DrawExpFrame.class.getName()).log(Level.SEVERE, null, ex);
       }
       hideCursor();
   }
	 
   public void showCursor () {
	setCursor (Cursor.getDefaultCursor());
//       setCursor(show);
   }

   public static int getScreenHeight(){
       return screenHeight;
   }

   public static int getScreenWidth(){
       return screenWidth;
   }

   
}
