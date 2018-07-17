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
}
