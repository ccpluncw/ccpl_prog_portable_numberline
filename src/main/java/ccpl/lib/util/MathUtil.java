package ccpl.lib.util;

public class MathUtil {

  /** Prevent instantiation */
  private MathUtil() {}

  /**
   * Checks if two bound contain a number, inclusively.
   *
   * @param start Start
   * @param end End
   * @param point Point potentially inside
   * @return True if inside, false otherwise
   */
  public static boolean contains(double start, double end, double point) {
    return start <= point && point <= end;
  }

  /**
   * Calculate the number of distinct targets.
   *
   * @param start Start value
   * @param end End value
   * @param interval Increment that values may exist on.
   * @param leftBnd Left bound position
   * @param rightBnd Right bound position
   * @param excludeLeft Prevent the left bound from being a target?
   * @param excludeRight Prevent the right bound from being a target?
   * @return Number of distinct targets
   */
  public static int calcDistinctCount(
      double start,
      double end,
      double interval,
      double leftBnd,
      double rightBnd,
      boolean excludeLeft,
      boolean excludeRight) {
    int numExcludePoints = 0;

    if (excludeLeft) {
      numExcludePoints += MathUtil.contains(start, end, leftBnd) ? 1 : 0;
    }

    if (excludeRight) {
      numExcludePoints += MathUtil.contains(start, end, rightBnd) ? 1 : 0;
    }

    return (int) ((end - start + 1) / interval - numExcludePoints);
  }
}
