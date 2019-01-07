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

public class StringUtil {
  /**
   * Check if an input is part of a number in the appropriate place.
   *
   * @param input Single character
   * @param text Whole text, for contextual information.
   * @return True if it's not part of a number.
   */
  public static boolean notPartOfNumber(String input, String text) {
    return !input.matches("(-|[0-9]|\\.)")
        || (input.matches("-") && !text.isEmpty())
        || (input.matches("\\.") && text.contains("."));
  }
}
