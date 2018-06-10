package ccpl.lib.util;

import ccpl.lib.Bundle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DatabaseFileReader {

  private static final Logger logger = Logger.getLogger(DatabaseFileReader.class.getName());

  private static final char delimiter = ':';
  private static final char commentCharacter = '#';

  /**
   * Read a configuration file at a given URL and turn it into a Bundle.
   *
   * @param path Path to file
   * @return Bundle containing configuration
   */
  public static Bundle readDbFile(URL path) {
    Bundle dbFileValues = new Bundle();

    BufferedReader br;
    try {
      br = new BufferedReader(new InputStreamReader(path.openStream()));

      List<String> linesToParse =
          br.lines()
              .filter(line -> !line.isEmpty())
              .filter(line -> line.charAt(0) != commentCharacter)
              .map(line -> line.split(String.valueOf(commentCharacter))[0])
              .collect(Collectors.toCollection(ArrayList::new));

      linesToParse.forEach(
          line -> {
            String[] split = line.split(String.valueOf(delimiter));
            dbFileValues.add(split[0], split[1].trim());
          });
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getLocalizedMessage());
    }

    return dbFileValues;
  }

  /**
   * Write a Bundle to a given file.
   *
   * @param bundle Bundle containing content
   * @param path Path to a file.
   */
  public static void writeDbFile(Bundle bundle, URL path) {
    try {
      File file = new File(path.toURI());
      file.getParentFile().mkdirs();

      PrintWriter pw = new PrintWriter(new FileOutputStream(file));
      pw.println(bundle.toString());
      pw.close();
    } catch (URISyntaxException | FileNotFoundException e) {
      logger.log(Level.SEVERE, e.getLocalizedMessage());
    }
  }
}
