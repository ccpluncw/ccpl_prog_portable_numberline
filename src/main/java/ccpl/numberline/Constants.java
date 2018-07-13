package ccpl.numberline;

/**
 * Shared static constants.
 *
 * These are used to keep track of globally changeable
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
