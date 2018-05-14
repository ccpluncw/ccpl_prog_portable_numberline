package ccpl.numberline.config;

import ccpl.lib.Bundle;

import static ccpl.lib.util.UiUtil.screenWidth;
import static java.lang.Double.parseDouble;

public class ConfigValidator {
  private static final String startUnitStr  = "Target \"From\"";
  private static final String endUnitStr    = "Target \"To\"";

  /**
   * Create a StringBuilder containing all the errors with the configuration.
   *
   * @param bun   Configuration bundle
   *
   * @return      StringBuilder with any error messages.
   */
  public static StringBuilder generateConfigErrors(Bundle bun) {
    StringBuilder err = new StringBuilder();

    // Unpack all the relevant values from the bundle.
    int numTrials = bun.getAsInt("num_trials");

    boolean isEstimation  = bun.getAsBoolean("estimation_task");
    boolean isBounded     = bun.getAsBoolean("bound_exterior");

    double targLow   = parseDouble(bun.getAsString("target_unit_low"));
    double targHigh  = parseDouble(bun.getAsString("target_unit_high"));

    double leftBound   = parseDouble(bun.getAsString("start_unit"));
    double rightBound  = parseDouble(bun.getAsString("end_unit"));

    int margin = bun.getAsInt("left_margin_low");
    double largestTarget = parseDouble(bun.getAsString("largest_target"));

    int scalarField = bun.getAsInt("scalar_field");
    boolean stimOn = !bun.getAsBoolean("stim_time_off");

    // Check for potential errors.
    if (numTrials == 0) {
      err.append("Experiment contains no trials.\n" +
          "Please set the number of trials to a minimum of 1\n");
    }

    if (leftBound > targLow) {
      err.append(startUnitStr + " value is less than the left bound.\n" +
          "Please set the " + startUnitStr + " value greater than the left bound.\n");
    }

    if (leftBound > rightBound) {
      err.append("Left bound is greater than the right bound.\n" +
          "Please set the left bound to less than the right bound.\n");
    }

    if (targHigh >= largestTarget) {
      err.append(endUnitStr + " value is equal to or greater than the largest target\n" +
          "Please set the " + endUnitStr + " value to less than the largest target.");
    }

    if (isBounded) {
      if (targHigh >= rightBound) {
        err.append(endUnitStr + " value is equal to or greater than the right bound.\n" +
            "Please set the " + endUnitStr + " value to less than the right bound.\n");
      }

      if (rightBound > screenWidth() - margin * 2) {
        err.append(endUnitStr + " cannot fit on screen. Maximum end unit is $largestTarget \n" +
            "Please set the " + endUnitStr + " value to at most $largestTarget\n");
      }
    }

    if (isEstimation) {
      if (rightBound > largestTarget) {
        err.append("Right bound is greater than the largest estimation target.\n" +
            "Please set the right bound to less than or equal to the largest estimation target\n");
      }

      if (targHigh > largestTarget) {
        err.append(endUnitStr + " value is greater than maximum target.\n" +
            "Please set the " + endUnitStr + " value to less than or equal to the maximum target\n");
      }

      if (stimOn && scalarField < 1) {
        err.append("The scaling factor of your refresh rate must be greater than 0.\n");
      }
    }

    return err;
  }
}
