package ccpl.lib.util;

public class StringUtil {
  public static boolean notPartOfNumber(String input, String text) {
    return !input.matches("(-|[0-9]|\\.)")
        || (input.matches("-") && !text.isEmpty())
        || (input.matches("\\.") && text.contains("."));
  }
}
