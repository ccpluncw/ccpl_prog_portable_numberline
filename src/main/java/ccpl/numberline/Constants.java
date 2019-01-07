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

package ccpl.numberline;

/**
 * Shared static constants.
 * <p>
 * These are used to keep track of globally changeable
 * </p>
 */
public class Constants {
  public static String outputDirectory = ".";
  public static String lastConfigSaveDir = outputDirectory;

  public static void setOutputDirectory(String newDir) {
    outputDirectory = newDir;
  }

  public static void setLastConfigSaveDirectory(String newDir) {
    lastConfigSaveDir = newDir;
  }
}
