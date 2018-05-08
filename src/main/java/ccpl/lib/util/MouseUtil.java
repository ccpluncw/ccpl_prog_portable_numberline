package ccpl.lib.util;

import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.Robot;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MouseUtil {
  /**
   * Resets the mouse to the center of a JFrame.
   * @param frame   JFrame to move the mouse in.
   */
  public static void resetMouseToCenter(JFrame frame) {
    try {
      new Robot().mouseMove(frame.getWidth() / 2, frame.getHeight() / 2);
    } catch (AWTException e) {
      Logger.getLogger(MouseUtil.class.getName()).log(Level.WARNING, e.getLocalizedMessage());
    }
  }
}
