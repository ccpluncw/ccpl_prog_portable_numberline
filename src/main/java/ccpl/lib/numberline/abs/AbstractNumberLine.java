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

package ccpl.lib.numberline.abs;

import java.awt.geom.Line2D;
import javax.swing.JPanel;

public interface AbstractNumberLine {
  /**
   * Return state of handle.
   *
   * @return State of handle.
   */
  boolean isHandleDragged();

  /**
   * Return the pixel length of a single unit on the number line.
   *
   * @return Length of 1 unit.
   */
  double getUnitLength();

  /**
   * Convert a response into a decimal value.
   *
   * @param resp User's response
   * @return Decimal value
   */
  double getUnitLength(String resp);

  double getUnitError(boolean inPercent);

  double getUnitError(boolean inPercent, String userResp);

  int getBaseWidth();

  double getUserResponse();

  /**
   * Return the number line panel.
   *
   * @return Number Line Panel
   */
  JPanel getPanel();

  /**
   * Line that is displayed during fixation periods.
   *
   * @return Fixation line
   */
  Line2D getFixationLine();
}
