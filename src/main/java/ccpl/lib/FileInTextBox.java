package ccpl.lib;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;


/***  This class reads in a file and places the contents in a non-editable text box.  This
 ****	box is created so the reader has to click ok before the program will continue.
 ***/


public class FileInTextBox implements ActionListener {

  protected JFrame frame;
  protected final String filename;
  protected final URL urlFileName;
  protected JButton textOkButton;

  public FileInTextBox(URL u) {
    urlFileName = u;
    filename = null;
  }

  public synchronized void presentFile() {

    frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.setTitle("Please Read");
    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension d = tk.getScreenSize();
    frame.setSize(d.width, d.height);
    frame.setLocation(0, 0);

    BlankPanel buttonPanel = new BlankPanel(Color.lightGray);

    JTextArea fileText = new JTextArea();
    fileText.setEditable(false);
    textOkButton = new JButton("OK");
    textOkButton.addActionListener(this);

    JScrollPane scrollPane = new JScrollPane(fileText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    buttonPanel.add(textOkButton);

    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    if (filename != null) {
      readInFile(fileText, filename);
    } else if (urlFileName != null) {
      readInURL(fileText, urlFileName);
    } else {
      System.err.println("FileInTextBox object is invalid");
      System.exit(1);
    }

    frame.getRootPane().setDefaultButton(textOkButton);
    frame.setVisible(true);

    try {
      wait();
    } catch (InterruptedException ex) {
      Logger.getLogger(FileInTextBox.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public synchronized void actionPerformed(ActionEvent evt) {
    JButton button = (JButton) evt.getSource();
    if (button == textOkButton) {
      frame.setVisible(false);
      frame.dispose();
      notify();
    }
  }

  public static void readInFile(JTextArea fileText, String file1) {
    String entry;
    try {
      BufferedReader in = new BufferedReader(new
          FileReader(file1));
      fileText.setText(" ");
      while ((entry = in.readLine()) != null) {
        fileText.append(entry + "\n");
      }
      in.close();
    } catch (IOException e) {
      System.out.print("Error: " + e);
      System.exit(1);
    }
  }

  public static void readInURL(JTextArea fileText, URL fileURL) {
    String entry;
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(fileURL.openStream()));
      fileText.setText(" ");
      while ((entry = in.readLine()) != null) {
        fileText.append(entry + "\n");
      }
      in.close();
    } catch (IOException e) {
      System.out.print("Error: " + e);
      System.exit(1);
    }
  }

}
