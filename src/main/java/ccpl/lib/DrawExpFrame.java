package ccpl.lib;

import static ccpl.lib.util.MouseUtil.resetMouseToCenter;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

/** The DRAWEXPFRAME sets up a frame the size of the screen to run the experiment in. */
public class DrawExpFrame extends JFrame {

  private Cursor curs;

  /**
   * The frame that the experiment is displayed on.
   *
   * @param resp Response object.
   */
  DrawExpFrame(Response resp) {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    addKeyListener(resp);
    assignKeyBindings(resp);

    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension d = tk.getScreenSize();
    setSize(d.width, d.height);

    BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

    curs =
        Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

    setBackground(Color.BLACK);
    getContentPane().setBackground(Color.BLACK);
    setResizable(false);
    setLocation(0, 0);
    setFocusable(true);

    // ENABLES FULL SCREEN FUNCTION ONLY FOR MAC OS
    // OTHER OS SIMPLY SETTING THE WINDOW TO UNDECORATED ACCOMPLISHES THE SAME GOAL
    if (System.getProperty("os.name").startsWith("Mac")) {
      // TODO: Figure out how to fix this on non-Mac machines
      // com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(this, true);
      try {
        Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
        Class[] params = new Class[] {Window.class, Boolean.TYPE};
        Method method = util.getMethod("setWindowCanFullScreen", params);
        method.invoke(util, this, true);
      } catch (NoSuchMethodException
          | IllegalAccessException
          | InvocationTargetException
          | ClassNotFoundException e) {
        Logger.getLogger(DrawExpFrame.class.getName()).log(Level.SEVERE, e.getMessage());
      }
    } else {
      setUndecorated(true); // Hides minimize and maximize buttons on jframe title bar
    }

    setVisible(true);
  }

  private void assignKeyBindings(Response resp) {
    ActionMap actionMap = getRootPane().getActionMap();
    int condition = JComponent.WHEN_IN_FOCUSED_WINDOW; // changed #1
    InputMap inputMap = getRootPane().getInputMap(condition);

    KeyStroke quit = KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK);
    KeyStroke mouse = KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK);

    inputMap.put(quit, "quitAction");
    inputMap.put(mouse, "mouseAction");

    // control q quits
    actionMap.put(
        "quitAction",
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            System.exit(0);
          }
        });

    actionMap.put("mouseAction", resp.returnMouseAction());
  }

  /** Hide the cursor. */
  public void hideCursor() {
    Toolkit tk = Toolkit.getDefaultToolkit();
    curs = tk.createCustomCursor(tk.createImage(""), new Point(), "blank");
    setCursor(curs);

    resetMouseToCenter(this);
  }

  public void showCursor() {
    setCursor(Cursor.getDefaultCursor());
  }
}
