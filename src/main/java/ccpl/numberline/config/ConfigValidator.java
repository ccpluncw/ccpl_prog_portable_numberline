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

package ccpl.numberline.config;

import static ccpl.lib.util.UiUtil.screenWidth;
import static java.lang.Double.parseDouble;

import ccpl.lib.Bundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Helper class for verifying user interface input.
 */
public class ConfigValidator {
  private static final String startUnitStr = "Target \"From\"";
  private static final String endUnitStr = "Target \"To\"";

  /**
   * Create a StringBuilder containing all the errors with the configuration.
   *
   * @param bun Configuration bundle
   * @return StringBuilder with any error messages.
   */
  public static StringBuilder generateConfigErrors(Bundle bun) {
    StringBuilder err = new StringBuilder();

    // Unpack all the relevant values from the bundle.
    int numTrials = bun.getAsInt(Keys.NUM_TRIALS);

    final boolean isEstimation = bun.getAsBoolean(Keys.EST_TASK);
    final boolean isBounded = bun.getAsBoolean(Keys.BOUND_EXTERIOR);

    final double targLow = parseDouble(bun.getAsString(Keys.TARGET_UNIT_LOW));
    final double targHigh = parseDouble(bun.getAsString(Keys.TARGET_UNIT_HIGH));
    final double targInt = parseDouble(bun.getAsString(Keys.TARGET_UNIT_INTERVAL));

    final double leftBound = parseDouble(bun.getAsString(Keys.START_UNIT));
    final double rightBound = parseDouble(bun.getAsString(Keys.END_UNIT));

    final int margin = bun.getAsInt(Keys.LEFT_MARGIN_LOW);
    final double largestTarget = parseDouble(bun.getAsString(Keys.LARGEST_TARGET));

    final int scalarField = bun.getAsInt(Keys.SCALAR_FIELD);
    final boolean stimOn = !bun.getAsBoolean(Keys.EST_STIM_TIME_OFF);

    final int interval = bun.getAsInt(Keys.TARGET_UNIT_INTERVAL);

    final int numOfDistinctTargets = bun.getAsInt(Keys.DISTINCT_TARGETS);

    // Check for potential errors.
    if (numTrials == 0) {
      err.append(
          "Experiment contains no trials.\n"
              + "Please set the number of trials to a minimum of 1\n");
      err.append("\n ");
    }

    if (leftBound > targLow) {
      err.append(
          startUnitStr
              + " value is less than the left bound.\n"
              + "Please set the "
              + startUnitStr
              + " value greater than the left bound. \n");
      err.append(" \n");
    }

    if (leftBound > rightBound) {
      err.append(
          "Left bound is greater than the right bound.\n"
              + "Please set the left bound to less than the right bound.\n");
      err.append("\n");
    }

    if (interval < 1) {
      err.append(
          "\"By\" value is less than one.\n"
              + "Please set the \"By\" to greater than or equal to 1\n");
      err.append("\n");
    }

    if (targHigh > largestTarget) {
      err.append(
          endUnitStr
              + " value is greater than the largest target\n"
              + "Please set the "
              + endUnitStr
              + " value to less than or equal to the largest target.\n");
      err.append("\n");
    }

    if (targInt > (targHigh - targLow)) {
      err.append(
          "Target interval exceeds the difference between the \"To\" and \"From\" values.\n"
              + "Please set the target interval to less than or equal to "
              + "\"To\" - \"From\" values.\n");
      err.append("\n");
    }

    if (rightBound > largestTarget) {
      err.append(
          "Right bound exceeds largest right bound.\n"
              + "Please set the right bound to less than or equal to the largest right bound.\n\n");
    }

    if (numOfDistinctTargets <= 0) {
      err.append("Number of distinct targets is equal to or less than 0.\n\n");
    }

    if (isBounded) {
      if (targHigh > rightBound) {
        err.append(
            endUnitStr
                + " value is equal to or greater than the right bound.\n"
                + "Please set the "
                + endUnitStr
                + " value to less than the right bound.\n");
        err.append("\n");
      }

      if (rightBound > screenWidth() - margin * 2) {
        err.append(
            endUnitStr
                + " cannot fit on screen. Maximum end unit is $largestTarget \n"
                + "Please set the "
                + endUnitStr
                + " value to at most $largestTarget\n");
        err.append("\n");
      }
    }

    if (isEstimation) {
      if (rightBound > largestTarget) {
        err.append(
            "Right bound is greater than the largest estimation target.\n"
                + "Please set the right bound to less than or equal to the "
                + "largest estimation target\n");
        err.append("\n");
      }

      if (targHigh > largestTarget) {
        err.append(
            endUnitStr
                + " value is greater than maximum target.\n"
                + "Please set the "
                + endUnitStr
                + " value to less than or equal to the maximum target\n");
        err.append("\n");
      }

      if (stimOn && scalarField < 1) {
        err.append("The scaling factor of your refresh rate must be greater than 0.\n");
        err.append("\n");
      }
    }

    return err;
  }

  /**
   * Count the number of errors in the user's configuration.
   * @param bun   Data bundle
   * @return Number of errors
   */
  public static int countErrors(Bundle bun) {
    String errors = generateConfigErrors(bun).toString();

    Pattern pattern = Pattern.compile("\n\n");
    Matcher matcher = pattern.matcher(errors);

    int counter = 0;
    while (matcher.find()) {
      counter++;
    }

    return counter;
  }
}
