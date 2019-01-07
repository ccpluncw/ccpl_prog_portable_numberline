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

package ccpl.lib;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/** The following creates a blank panel with the appropriate background color. */
public class BlankPanel extends JPanel {
  public BlankPanel() {
    setBackground(Color.black);
  }

  public BlankPanel(Color backColor) {
    setBackground(backColor);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
  }
}
