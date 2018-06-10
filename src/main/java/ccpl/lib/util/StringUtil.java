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
