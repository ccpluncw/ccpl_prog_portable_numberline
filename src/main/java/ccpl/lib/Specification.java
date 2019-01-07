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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Specification {
  Specification(String s) {
    allSpecs = s;
  }

  Specification() {
  }

  /**
   * Strip comments from an input line.
   *
   * @param line Input line
   * @return Input line without comments.
   */
  static String stripComments(String line) {
    int idx;
    if ((idx = line.indexOf("//")) >= 0) {
      line = line.substring(0, idx).trim();
    }
    return line;
  }

  /**
   * Read data from a BufferedReader.
   *
   * @param in Reader
   * @return Every specification stripped of comments.
   * @throws IOException Throw an IOException if there is a problem reading.
   */
  boolean readData(BufferedReader in) throws IOException {
    if ((allSpecs = in.readLine()) != null) {
      allSpecs = stripComments(allSpecs);
      return true;
    } else {
      return false;
    }
  }

  boolean isEmpty() {
    return allSpecs.isEmpty();
  }

  /**
   * The following method, and the ones below it, return one piece of information from the input
   * string. You specify the token number, and the method returns the info at that point in the
   * string. Use the appropriate method for the type of info you are returning, i.e., String, int,
   * double, or char.
   */
  public String getParsedStringSpec(int stringPosition) {
    int i;
    String outString = "";

    StringTokenizer t = new StringTokenizer(allSpecs, "\t");

    for (i = 0; i < stringPosition; i++) {
      outString = t.nextToken();
    }

    return outString;
  }

  /** This returns the entire string, unparsed. */
  public String getAllSpecs() {
    return allSpecs;
  }

  private String allSpecs;
}
