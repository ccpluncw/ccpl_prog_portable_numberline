/*
 * This file is part of the Cohen Ray Number Line.
 *
 * Latesco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Latesco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Latesco.  If not, see <http://www.gnu.org/licenses/>.
 */

package ccpl.lib.util;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class MouseUtil {
  /**
   * Resets the mouse to the center of a JFrame.
   *
   * @param frame JFrame to move the mouse in.
   */
  public static void resetMouseToCenter(JFrame frame) {
    try {
      new Robot().mouseMove(frame.getWidth() / 2, frame.getHeight() / 2);
    } catch (AWTException e) {
      Logger.getLogger(MouseUtil.class.getName()).log(Level.WARNING, e.getLocalizedMessage());
    }
  }
}
