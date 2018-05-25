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

  private static final char delimiter = ':';
  private static final char commentCharacter = '#';

  public static Bundle readDbFile(URL path) {
    Bundle dbFileValues = new Bundle();

    BufferedReader br;
    try {
      br = new BufferedReader(new InputStreamReader(path.openStream()));

      List<String> linesToParse = br.lines()
          .filter(line -> !line.isEmpty())
          .filter(line -> line.charAt(0) != commentCharacter)
          .map(line -> line.split(String.valueOf(commentCharacter))[0])
          .collect(Collectors.toCollection(ArrayList::new));

      linesToParse.forEach(line -> {
        String[] split = line.split(String.valueOf(delimiter));
        dbFileValues.add(split[0], split[1].trim());
      });
    } catch (IOException e) {
      Logger.getLogger(DatabaseFileReader.class.getName()).log(Level.SEVERE, e.getLocalizedMessage());
    }


    return dbFileValues;
  }

  public static void writeDbFile(Bundle bundle, URL path) {
    try {
      File file = new File(path.toURI());
      file.getParentFile().mkdirs();

      PrintWriter pw = new PrintWriter(new FileOutputStream(file));
      pw.println(bundle.toString());
      pw.close();
    } catch (URISyntaxException | FileNotFoundException e) {
      Logger.getLogger(DatabaseFileReader.class.getName()).log(Level.SEVERE, e.getLocalizedMessage());
    }
  }
}
