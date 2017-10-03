package ccpl.lib;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/*****	The DRAWEXPFRAME sets up a frame the size of the screen to run the experiment
 ******	in.
 *****/
public class DrawExpFrame extends JFrame {

  private static int screenHeight, screenWidth;
  private Cursor curs;

  public DrawExpFrame(Response resp) {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    addKeyListener(resp);
    assignKeyBindings(resp);

    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension d = tk.getScreenSize();
    screenHeight = d.height;
    screenWidth = d.width;
    setSize(screenWidth, screenHeight);

    BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

    curs = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

    setBackground(Color.BLACK);
    getContentPane().setBackground(Color.BLACK);
    setResizable(false);
    setLocation(0, 0);
    setFocusable(true);

    //ENABLES FULL SCREEN FUNCTION ONLY FOR MAC OS
    //OTHER OS SIMPLY SETTING THE WINDOW TO UNDECORATED ACCOMPLISHES THE SAME GOAL
    if (System.getProperty("os.name").startsWith("Mac")) {//  equalsIgnoreCase("mac os x"))
      // TODO: Figure out how to fix this on non-Mac machines
      // com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(this, true);
//        com.apple.eawt.Application.getApplication().requestToggleFullScreen(this);
    } else {
      setUndecorated(true); //Hides minimize and maximize buttons on jframe title bar
    }
    setVisible(true);

  }

  private void assignKeyBindings(Response resp) {
    ActionMap actionMap = getRootPane().getActionMap();
    int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;//changed #1
    InputMap inputMap = getRootPane().getInputMap(condition);

    KeyStroke quit = KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK);
    KeyStroke mouse = KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK);

    inputMap.put(quit, "quitAction");
    inputMap.put(mouse, "mouseAction");

    //control q quits
    actionMap.put("quitAction", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });

    actionMap.put("mouseAction", resp.returnMouseAction());
  }

  public void hideCursor() {
    //Cursor curs = tKit.createCustomCursor (tKit.createImage(""), new Point(), "blank");
    Toolkit tk = Toolkit.getDefaultToolkit();
    curs = tk.createCustomCursor(tk.createImage(""), new Point(), "blank");
    setCursor(curs);
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

  public void showCursor() {
    setCursor(Cursor.getDefaultCursor());
  }

  public static int getScreenHeight() {
    return screenHeight;
  }

  public static int getScreenWidth() {
    return screenWidth;
  }


}
