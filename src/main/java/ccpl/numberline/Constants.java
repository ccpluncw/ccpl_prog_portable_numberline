package ccpl.numberline;

/**
 * Shared static constants.
 *
 * These are used to keep track of globally changeable
 */
public class Constants {
  public static String outputDirectory = ".";

  public static void setOutputDirectory(String newDir) {
    outputDirectory = newDir;
  }
}
