package ccpl.lib;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/*****	Use the following class to work with arrays of the SPECIFICATION objects.
 ******  The methods read data in, write data, and randomize data.
 *****/
public class SpecificationArrayProcess {

  private boolean isLocalWriteDir;

  public SpecificationArrayProcess() {
    isLocalWriteDir = false;
  }

  public Specification[] readData(BufferedReader in) throws IOException {

    ArrayList<Specification> stims = new ArrayList<>();
    int i = 0;
    String line = null;
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
    //int n = Integer.parseInt(in.readLine());
    //Specification[] stims = new Specification[n];
    //for (i = 0; i < n; i++) {

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
      stims.remove(i - 1); //Remove last Specification because it holds no input
    }

    //Convert ArrayList of Specification Objects into Specification Array to return
    Object stimsArray[] = stims.toArray();
    Specification stimsCpy[] = new Specification[stimsArray.length];
    System.arraycopy(stimsArray, 0, stimsCpy, 0, stimsArray.length);

    return stimsCpy;
  }

  public Specification[] readFromFile(String filename) {
    Specification[] specs = null;
    try {
      BufferedReader in = new BufferedReader(new
          FileReader(filename));
      specs = readData(in);
      in.close();
    } catch (IOException e) {
      System.out.println("Error: " + e);
      System.out.println("File: " + filename);
      //System.exit(1);
    }
    return specs;
  }

  public Specification[] readFromURL(URL fileURL) {
    Specification[] specs = null;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(fileURL.openStream()));
      specs = readData(in);
      in.close();
    } catch (IOException e) {
      System.out.println("Error: " + e);
      System.out.println("File: " + fileURL.getFile());
      //System.exit(1);
    } catch (Exception e) {
      System.out.print("Error: " + e);
    }
    return specs;
  }

  private void createLocalWriteDir(String filename) {
    String path = filename.substring(0, filename.lastIndexOf("/"));
    File filePath = new File(path);

    if (!filePath.isDirectory()) {
      filePath.mkdir();
    }

    isLocalWriteDir = true;
  }

  public boolean writeToFile(String filename, String outString) {
    boolean success = false;

    if (!isLocalWriteDir) {
      createLocalWriteDir(filename);
    }

    try {
      PrintWriter out = new PrintWriter(new FileOutputStream(filename, true));
      out.println(outString);
      out.close();
      success = true;
    } catch (IOException ioe) {
      System.out.print("Error: " + ioe);
    }
    return success;
  }

  public boolean writeToFile(URL fileURL, String outString) {
    return writeToFile(fileURL.getPath(), outString);
  }

  public boolean writeToURL(URL cgi, URL filename, String outString) {
    boolean success = false;

    if (cgi != null) {
      try {
        final String message = "out=" + URLEncoder.encode(outString, "UTF-8") + "&file="
            + URLEncoder.encode(filename.getPath(), "UTF-8");

        URLConnection conn = cgi.openConnection();
        ((HttpURLConnection) conn).setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", "" + message.length());

        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

        dos.writeBytes(message);
        dos.flush();
        dos.close();

        // the server responds by echoing 1 for success/0 for failure
        BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String resp;
        if ((resp = input.readLine()) != null) {
          if (resp.equals("0")) {
            System.err.println("Error posting data to server");
          } else {
            success = true;
          }
        }
        input.close();
      } catch (IOException ioe) {
        ioe.getMessage();
      }
    } else {
      success = writeToFile(filename, outString);
    }

    return success;
  }


  public Specification[] randomize(Specification[] specs) {
    int tmp1;
    int tmp2;
    Specification tmpSpec1;

    RandomIntGenerator random1 = new RandomIntGenerator(0, specs.length - 1);

    for (int i = 0; i < 50; i++) {
      for (int j = 0; j < specs.length; j++) {
        tmp1 = random1.draw();
        tmp2 = random1.draw();
        tmpSpec1 = specs[tmp1];
        specs[tmp1] = specs[tmp2];
        specs[tmp2] = tmpSpec1;
      }
    }
    return specs;
  }

}
