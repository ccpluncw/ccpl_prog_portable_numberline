package ccpl.lib.Util

import java.awt.Robot
import javax.swing.JFrame

/**
 * Resets the mouse to the center of a JFrame.
 * @param frame   JFrame to move the mouse in.
 */
fun resetMouseToCenter(frame: JFrame) = Robot().mouseMove(frame.width / 2, frame.height / 2)
