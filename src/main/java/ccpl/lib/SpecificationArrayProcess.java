package ccpl.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

/**
 * Use the following class to work with arrays of the SPECIFICATION objects. The methods read data
 * in, write data, and randomize data.
 */
public class SpecificationArrayProcess {

  private boolean isLocalWriteDir;

  public SpecificationArrayProcess() {
    isLocalWriteDir = false;
  }

  private Specification[] readData(BufferedReader in) throws IOException {

    ArrayList<Specification> stims = new ArrayList<>();
    int i = 0;
    String line;
    boolean isRead = true;
    if ((line = in.readLine()) != null) {
      if (!line.matches("^\\d+$")) {
        line = Specification.stripComments(line);
        if (!line.equals("")) {
          stims.add(new Specification(line));
          ++i;
        }
        isRead = true;
      }
    }

    while (isRead) {
      stims.add(new Specification());
      isRead = stims.get(i).readData(in);
      ++i;
      if (isRead) {
        if (stims.get(i - 1).isEmpty()) {
          --i;
          stims.remove(i);
        }
      }
    }
    if (stims.size() >= 1) {
      stims.remove(i - 1); // Remove last Specification because it holds no input
    }

    // Convert ArrayList of Specification Objects into Specification Array to return
    Object[] stimsArray = stims.toArray();
    Specification[] stimsCpy = new Specification[stimsArray.length];
    stims.toArray(stimsCpy);

    return stimsCpy;
  }

  /**
   * Read Specification data from a file.
   *
   * @param fileUrl URL to the file
   * @return Specifications from within the file.
   */
  public Specification[] readFromUrl(URL fileUrl) {
    Specification[] specs = null;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
      specs = readData(in);
      in.close();
    } catch (IOException e) {
      System.out.println("Error: " + e);
      System.out.println("File: " + fileUrl.getFile());
      // System.exit(1);
    } catch (Exception e) {
      System.out.print("Error: " + e);
    }
    return specs;
  }

  private void createLocalWriteDir(String filename) {
    String path = filename.substring(0, filename.lastIndexOf("/"));
    File filePath = new File(path);

    if (!filePath.isDirectory()) {
      //noinspection ResultOfMethodCallIgnored
      filePath.mkdir();
    }

    isLocalWriteDir = true;
  }

  private void writeToFile(String filename, String outString) {
    if (!isLocalWriteDir) {
      createLocalWriteDir(filename);
    }

    try {
      PrintWriter out = new PrintWriter(new FileOutputStream(filename, true));
      out.println(outString);
      out.close();
    } catch (IOException ioe) {
      System.out.print("Error: " + ioe);
    }
  }

  private void writeToFile(URL fileUrl, String outString) {
    writeToFile(fileUrl.getPath(), outString);
  }

  public void writeToUrl(URL filename, String outString) {
    writeToFile(filename, outString);
  }
}
