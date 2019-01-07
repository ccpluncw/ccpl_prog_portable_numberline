/*
 * This file is part of the Cohen Ray Number Line.
 *
 * Latesco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Latesco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Latesco.  If not, see <http://www.gnu.org/licenses/>.
 */

package ccpl.lib;

import static ccpl.lib.util.MouseUtil.resetMouseToCenter;
import static ccpl.lib.util.StringUtil.notPartOfNumber;

import ccpl.lib.numberline.abs.AbstractNumberLine;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * The RESPONSE class is very important. It specifies methods for collecting and checking responses.
 * It implements KEYLISTENER so that keyboard responses can be collected. The class must be added to
 * the panel as the keyListener (i.e., addKeyListener (Response) Before any timing routine is run,
 * testTimer () must be run once
 */
public class Response implements KeyListener, ActionListener {

  private String textValue;
  private long textRt;
  private volatile char userChoice = '~';
  private JDialog respFrame;
  private JButton textOkButton;
  private JTextField textInput;
  private long startTime;
  private volatile boolean inputDone;
  private long loopsPerMs = 0;
  private boolean closedProperly;

  public AbstractAction returnMouseAction() {
    return new MouseAction();
  }

  private class MouseAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      if (mouseFrame != null) {
        if (mouseEnabled) {
          disableMouse(mouseFrame);
        } else {
          enableMouse(mouseFrame);
        }
      }
    }
  }

  public Response() {
  }

  public void setFrame(JFrame f) {
    mouseFrame = f;
  }

  @Override
  public void keyTyped(KeyEvent keyEvent) {
  }

  /**
   * When a button is hit this is triggered saves the button that was hit and deal with it depending
   * on the response mode.
   *
   * @param evt KeyEvent
   */
  public void keyPressed(KeyEvent evt) {
    if (evt.isControlDown() && (evt.getKeyChar() + "").toLowerCase().charAt(0) == 'q') {
      System.exit(0);
    } else {
      userChoice = (evt.getKeyChar() + "").toLowerCase().charAt(0);
    }
  }

  @Override
  public void keyReleased(KeyEvent keyEvent) {
  }

  /**
   * Calibrates timing to ensure accuracy. YOU HAVE TO CALL THIS TO MAKE THE RESPONSE CLASS WORK.
   *
   * @param inputPanel panel this method will draw in (its just going to indicate the timer is being
   *     calibrated.
   * @param textColor text color for the panel
   * @param reps how many reps the timing calibrator will iterate through (normally 1000)
   */
  public synchronized void testTimer(BlankPanel inputPanel, Color textColor, int reps) {
    long timeA;
    long timeB;
    long devisor;
    long overRun;
    long[] testLoop = new long[reps];
    long tmpTot = 0;

    // reset the user choice so that the polling call below will work.
    Font myFont = new Font("SansSerif", Font.BOLD, 24);
    Dimension d1 = inputPanel.getSize();
    final int dotAreaHeight = d1.height;
    final int dotAreaWidth = d1.width;

    JLabel timerLabel = new JLabel("Please wait while the timer is calibrated.");
    inputPanel.setLayout(null);
    timerLabel.setForeground(textColor);
    timerLabel.setFont(myFont);
    timerLabel.setSize(timerLabel.getPreferredSize());
    timerLabel.setLocation(
        dotAreaWidth / 2 - (timerLabel.getWidth() / 2),
        dotAreaHeight / 2 - (timerLabel.getHeight() / 2));
    inputPanel.add(timerLabel);
    inputPanel.revalidate();
    inputPanel.repaint();

    for (int i = 0; i < reps; i++) {
      resetUserChoice();
      overRun = 0;
      timeA = new Date().getTime();
      while (getUserChoice() == '~') {
        timeB = new Date().getTime();
        if (timeA != timeB) {
          setUserChoice('a');
          devisor = timeB - timeA;
          testLoop[i] = overRun / devisor;
        } else {
          overRun++;
        }
      }
    }

    for (int i = 0; i < reps; i++) {
      tmpTot += testLoop[i];
    }

    loopsPerMs = tmpTot / reps;

    inputPanel.remove(timerLabel);
    inputPanel.validate();
  }

  private long pollForKeyResponse(char c) {
    long overRun = 0;
    long timeA = new Date().getTime();
    long timeB = new Date().getTime();
    long tmpTime = timeA;

    while (getUserChoice() != c) {
      timeB = new Date().getTime();
      if (tmpTime != timeB) {
        overRun = 0;
        tmpTime = timeB;
      } else {
        overRun++;
      }
    }

    return (timeB - timeA) + (overRun / loopsPerMs);
  }

  private long pollForMouseResponse(AbstractNumberLine numLine) {
    long overRun = 0;
    long timeA = new Date().getTime();
    long timeB = new Date().getTime();
    long tempTime = timeA;

    // This is a hack to pass variables between anonymous classes.
    // Since the array is final, the anonymous class can use it
    // But since it is an array, you can change the element at a position
    // Huzzah! You just learned some truly black magic, Java wizardry.
    // Try NOT to do this, it may have unintended side effects!
    final boolean[] flag = {true};

    MouseListener tempListener =
        new MouseListener() {
          @Override
          public void mouseClicked(MouseEvent e) {
            flag[0] = false;
          }

          @Override
          public void mousePressed(MouseEvent e) {
          }

          @Override
          public void mouseReleased(MouseEvent e) {
          }

          @Override
          public void mouseEntered(MouseEvent e) {
          }

          @Override
          public void mouseExited(MouseEvent e) {
          }
        };

    numLine.getPanel().addMouseListener(tempListener);

    // TODO: Change this to listen to a mouse click
    while (flag[0]) {
      try {
        Thread.sleep(1);
      } catch (Exception e) {
        e.printStackTrace();
      }
      timeB = new Date().getTime();
      if (tempTime != timeB) {
        overRun = 0;
        tempTime = timeB;
      } else {
        overRun++;
      }
    }

    numLine.getPanel().removeMouseListener(tempListener);

    return (timeB - timeA) + (overRun / loopsPerMs);
  }

  private long pollForResponse() {
    long overRun = 0;

    long timeA = new Date().getTime();
    long timeB = new Date().getTime();
    long tmpTime = timeA;

    while (isInputRunning()) {
      timeB = new Date().getTime();
      if (tmpTime != timeB) {
        overRun = 0;
        tmpTime = timeB;
      } else {
        overRun++;
      }
    }

    return (timeB - timeA) + (overRun / loopsPerMs);
  }

  /**
   * Displays a notification frame with a desired message.
   *
   * @param parent Parent JFrame
   * @param info Message to be displayed.
   */
  public synchronized void displayNotificationFrame(JFrame parent, String info) {
    inputDone = false;
    textOkButton = new JButton("OK");
    createNotificationFrame(parent, info, textOkButton, 500, 250);
  }

  /**
   * Get a timed numberline response.
   *
   * @param numberLine NumberLine panel to use.
   * @param useMouse If response if based on mouse click
   * @return The amount of time it look for the user to click.
   */
  public long getTimedNumberLineResponse(AbstractNumberLine numberLine, boolean useMouse) {
    // Only the spacebar will trigger a timer stop, anything else is ignored
    char submitKey = ' ';

    if (loopsPerMs == 0) {
      JOptionPane.showMessageDialog(
          null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }

    // reset the user choice so that the polling call below will work.
    resetUserChoice();

    // Idle here until user has dragged the handle
    while (!numberLine.isHandleDragged()) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException ex) {
        Logger.getLogger(Response.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    // Reset choice because spacebar may have been pressed already.
    resetUserChoice();

    // User has moved handle so respond to the space bar
    long rt;
    if (!useMouse) {
      rt = pollForKeyResponse(submitKey);
    } else {
      rt = pollForMouseResponse(numberLine);
    }

    return rt;
  }

  /**
   * Get a timed text response.
   *
   * @param parent Parent JFrame
   * @param info First string
   * @param info2 Second string
   * @param columns Column width of the response
   * @param just Justification
   * @param mouseVisible Should the mouse be displayed
   * @throws IOException IOException
   */
  public synchronized void getTimedTextResponseJustified(
      JFrame parent, String info, String info2, int columns, String just, boolean mouseVisible)
      throws IOException {
    inputDone = false;
    final boolean fullScreen = false;

    textInput = new JTextField(columns);
    textInput.setText(null);
    if (just.equals("center")) {
      textInput.setHorizontalAlignment(JTextField.CENTER);
    }

    AbstractDocument doc = (AbstractDocument) textInput.getDocument();
    doc.setDocumentFilter(
        new DocumentFilter() {
          public String formatString(String s) {
            String format = "";
            Boolean dec = false;
            int decimalLocation = 0;
            for (int i = 0; i < s.length(); i++) {
              if (Character.toString(s.charAt(i)).equals(".")) {
                dec = true;
                decimalLocation = i;
              }
            }
            if (dec) {
              format = format + ".";
              for (int i = decimalLocation + 1; i < s.length(); i++) {
                if (Character.toString(s.charAt(i)).equals("0")) {
                  format = format + "0";

                } else if (Character.toString(s.charAt(i)).matches("[1-9]")) {
                  format = format + "0";
                }
              }
            }

            return format;
          }

          @Override
          public void replace(FilterBypass fb, int off, int length, String str, AttributeSet a)
              throws BadLocationException {
            Document doc = fb.getDocument();
            String text = doc.getText(0, doc.getLength());

            if (notPartOfNumber(str, text)) {
              return;
            }

            text = text.substring(0, off) + str + text.substring(off, text.length());
            // text = text.substring(0,off)+str+text.substring(off,fb.getDocument().getLength());
            text = text.replaceAll(",", "");

            if (text.matches("-?[0-9]*.?[0-9]*")) {
              String f = "#,##0";
              String signHolder = "";
              if (text.contains("-")) {
                signHolder = "-";
              }

              String formatter = formatString(text);
              f = f + formatter;
              DecimalFormat df = new DecimalFormat(f);
              if (str.equals(".")) {
                if (fb.getDocument().getLength() == 0
                    || (fb.getDocument().getLength() == 1 && signHolder.equals("-"))) {
                  super.replace(fb, off, length, "0" + str, a);
                } else {
                  BigDecimal t = new BigDecimal(text);
                  text = df.format(t);
                  super.replace(fb, 0, fb.getDocument().getLength(), text, a);
                }
              } else if (str.length() + fb.getDocument().getLength() > 3) {
                BigDecimal t = new BigDecimal(text);
                text = df.format(t);
                super.replace(fb, 0, fb.getDocument().getLength(), text, a);
              } else {
                super.replace(fb, off, length, str, a);
              }
            }
          }

          @Override
          public void insertString(FilterBypass fb, int offs, String str, AttributeSet a)
              throws BadLocationException {
            Document doc = fb.getDocument();
            String text = doc.getText(0, doc.getLength());

            if (notPartOfNumber(str, text)) {
              return;
            }

            text = text.substring(0, offs) + str + text.substring(offs, text.length());
            // text = text.substring(0,offs)+str+text.substring(fb.getDocument().getLength());
            text = text.replaceAll(",", "");
            String f = "#,##0";
            String signHolder = "";
            if (text.contains("-")) {
              System.out.println(text);
              signHolder = "-";
            }
            String formatter = formatString(text);
            f = f + formatter;
            DecimalFormat df = new DecimalFormat(f);
            if (str.length() + fb.getDocument().getLength() > 3) {
              BigDecimal t = new BigDecimal(text);
              text = df.format(t);
              super.replace(
                  fb,
                  0,
                  fb.getDocument().getLength(),
                  text.substring(0, fb.getDocument().getLength()),
                  a);
              if (str.equals("0")) {
                super.insertString(
                    fb,
                    fb.getDocument().getLength(),
                    text.substring(fb.getDocument().getLength()),
                    a);
              }
              super.insertString(
                  fb,
                  fb.getDocument().getLength(),
                  text.substring(fb.getDocument().getLength()),
                  a);
            } else {
              super.replace(fb, offs, offs + str.length(), str, a);
            }
          }

          @Override
          public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
              throws BadLocationException {
            super.remove(fb, offset, length);
            String text = fb.getDocument().getText(0, fb.getDocument().getLength());
            text = text.replaceAll(",", "");
            String f = "#,##0";
            String formatter = formatString(text);
            f = f + formatter;
            System.out.println(f);
            DecimalFormat df = new DecimalFormat(f);
            if (text.length() > 3) {
              BigDecimal t = new BigDecimal(text);
              text = df.format(t);
              super.remove(fb, 0, fb.getDocument().getLength());
              super.insertString(fb, fb.getDocument().getLength(), text, null);
            } else {
              super.remove(fb, 0, fb.getDocument().getLength());
              super.insertString(fb, 0, text, null);
            }
          }
        });

    textOkButton = new JButton("OK");

    createResponseFrameMouseSwitch(
        parent, textInput, textOkButton, info, info2, false, mouseVisible, fullScreen);
  }

  /**
   * Create a Response Frame with a mouse switch.
   *
   * @param parent Parent JFrame
   * @param inputComponent InputComponent
   * @param okButton Ok button
   * @param stdlabel Standard label
   * @param probelabel Label for the probe
   * @param isFrameModal Use modality
   * @param mouseVisible Show mouse?
   * @param fullScreen Fullscreen?
   * @throws IOException IOException
   */
  public synchronized void createResponseFrameMouseSwitch(
      JFrame parent,
      JComponent inputComponent,
      JButton okButton,
      String stdlabel,
      String probelabel,
      boolean isFrameModal,
      boolean mouseVisible,
      boolean fullScreen)
      throws IOException {
    respFrame = new JDialog(parent, isFrameModal);
    respFrame.setTitle("Response");
    respFrame.addKeyListener(this);
    respFrame.setResizable(false);
    respFrame.setUndecorated(true);

    // dont let people close out
    respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    closedProperly = true;
    respFrame.addWindowListener(
        new WindowAdapter() {

          public synchronized void windowClosed(WindowEvent e) {
          }

          public synchronized void windowClosing(WindowEvent e) {
            // inputDone = true;
            // closedProperly = false;
          }
        });

    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension d = tk.getScreenSize();

    BlankPanel backPanel = new BlankPanel(Color.PINK);
    backPanel.setPreferredSize(d);
    backPanel.setLayout(new GridLayout(3, 3));
    for (int i = 0; i < 5; i++) {
      BlankPanel b = new BlankPanel(Color.PINK);
      backPanel.add(b);
    }
    BlankPanel inputPanel = new BlankPanel(Color.lightGray);
    BlankPanel blackPanelB = new BlankPanel(Color.lightGray);

    Font font = new Font("Arial", Font.PLAIN, 18);
    JLabel label = new JLabel(probelabel);
    label.setFont(font);
    blackPanelB.add(label);

    Insets inset = new Insets(10, 10, 10, 10);
    GridBagLayout gridBag = new GridBagLayout();
    inputPanel.setLayout(gridBag);
    GridBagConstraints c = new GridBagConstraints();
    c.insets = inset;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.fill = GridBagConstraints.BOTH;

    c.gridx = 0;
    c.gridy = 1;
    c.gridheight = 1;
    c.gridwidth = 2;

    BlankPanel bp = new BlankPanel(Color.LIGHT_GRAY);
    gridBag.setConstraints(bp, c);
    inputPanel.add(bp);

    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 1;
    c.gridheight = 1;
    inputComponent.setFont(font);
    gridBag.setConstraints(inputComponent, c);
    inputPanel.add(blackPanelB);
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    gridBag.setConstraints(blackPanelB, c);
    inputPanel.add(inputComponent);
    BlankPanel buttonPanel = new BlankPanel(Color.lightGray);
    buttonPanel.add(okButton);
    okButton.addActionListener(this);
    respFrame.getContentPane().setLayout(new BorderLayout());
    respFrame.getContentPane().add(inputPanel, BorderLayout.CENTER);
    respFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    respFrame.pack();
    respFrame.validate();
    Dimension trueD = respFrame.getPreferredSize();
    respFrame.setLocation(
        (d.width - (int) trueD.getWidth()) / 2, (d.height - (int) trueD.getHeight()) / 2);

    // because this is a modal dialog, the show () command will not return until the frame has been
    // removed from sight
    respFrame.getRootPane().setDefaultButton(okButton);

    respFrame.setVisible(true);

    startTime = new Date().getTime();
    while (!inputDone) {
      Thread.yield();
    }
    if (!closedProperly) {
      okButton.doClick();
      JOptionPane.showMessageDialog(
          null,
          "You must choose an option.  Do NOT use the Close Box",
          "alert",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Perform an action of the response frame.
   *
   * @param evt Action event
   */
  public void actionPerformed(ActionEvent evt) {
    if (respFrame == null) {
      return;
    }

    JButton button = (JButton) evt.getSource();

    if (button.equals(textOkButton)) {
      long endTime = new Date().getTime();
      textRt = endTime - startTime;
      if (textInput != null && !"".equals(textInput.getText())) {
        textValue = (textInput.getText()).trim();
        respFrame.dispose();
        inputDone = true;
      } else if (textInput == null) {
        respFrame.setVisible(false);
        respFrame.dispose();
        inputDone = true;
      }
    }
  }

  /**
   * Create a NotificationFrame with a desired message, button, and dimensions.
   *
   * @param parent Parent JFrame
   * @param labelText Message to be displayed.
   * @param okButton Custom OK Button.
   * @param width Width of the frame.
   * @param height Height of the frame.
   */
  private void createNotificationFrame(
      JFrame parent, String labelText, JButton okButton, int width, int height) {
    respFrame = new JDialog(parent, false);
    respFrame.setTitle("Notification");

    // dont let people close out
    respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension d = tk.getScreenSize();
    respFrame.setSize(width, height);
    respFrame.setLocation((d.width - width) / 2, (d.height - height) / 2);
    Color tempC = new Color(200, 200, 200);
    BlankPanel inputPanel = new BlankPanel(tempC);
    final BlankPanel buttonPanel = new BlankPanel(tempC);
    final BlankPanel blackPanelA = new BlankPanel(tempC);
    final BlankPanel blackPanelB = new BlankPanel(tempC);
    final BlankPanel blackPanelC = new BlankPanel(tempC);

    /* this set of code allows one to get multiple line headers on the slider */
    JLabel label = new JLabel(labelText);
    Font labelFont = label.getFont();
    FontMetrics fm = label.getFontMetrics(labelFont);
    int fontWidth;

    int i = 0;
    int totalTokens;
    String[] outString = new String[5];
    String tmp;
    StringTokenizer t = new StringTokenizer(labelText, " ");

    do {
      tmp = t.nextToken();
      totalTokens = t.countTokens();
      if (totalTokens > 0) {
        do {
          tmp += " " + t.nextToken();
          fontWidth = fm.stringWidth(tmp);
          totalTokens = t.countTokens();
        } while (fontWidth < 200 && totalTokens > 0);
      }
      outString[i] = tmp + "\n";
      i++;
    } while (totalTokens > 0 && i < 5);

    int j;
    for (j = 0; j < i; j++) {
      JLabel responseLabel = new JLabel(outString[j]);
      blackPanelA.add(responseLabel);
    }

    Insets inset = new Insets(10, 10, 10, 10);
    GridBagLayout gridBag = new GridBagLayout();
    inputPanel.setLayout(gridBag);
    GridBagConstraints c = new GridBagConstraints();
    c.insets = inset;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.anchor = GridBagConstraints.CENTER;
    c.fill = GridBagConstraints.VERTICAL;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 5;
    c.gridheight = 1;
    gridBag.setConstraints(blackPanelB, c);
    inputPanel.add(blackPanelB);

    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 5;
    c.gridheight = 5;
    gridBag.setConstraints(blackPanelA, c);
    inputPanel.add(blackPanelA);

    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = 5;
    c.gridheight = 1;
    gridBag.setConstraints(blackPanelC, c);
    inputPanel.add(blackPanelC);

    buttonPanel.add(okButton);
    okButton.addActionListener(this);

    respFrame.getContentPane().setLayout(new BorderLayout());
    respFrame.getContentPane().add(inputPanel, "Center");
    respFrame.getContentPane().add(buttonPanel, "South");

    startTime = new Date().getTime();
    // because this is a modal dialog, the show () command will not return until the frame has been
    // removed from sight

    respFrame.getRootPane().setDefaultButton(okButton);
    respFrame.setVisible(true);

    while (isInputRunning()) {
      Thread.yield();
    }
  }

  private char getUserChoice() {
    return userChoice;
  }

  private void setUserChoice(char a) {
    userChoice = a;
  }

  private void resetUserChoice() {
    userChoice = '~';
  }

  private boolean isInputRunning() {
    return !inputDone;
  }

  public String getTextValue() {
    return textValue;
  }

  private final DisableMouseListener dml = new DisableMouseListener();
  private boolean mouseEnabled = true;
  private JFrame mouseFrame;

  private void disableMouse(JFrame f) {
    mouseFrame = f;
    f.addMouseMotionListener(dml);
    mouseEnabled = false;
  }

  private void enableMouse(JFrame f) {
    f.removeMouseMotionListener(dml);
    mouseEnabled = true;
  }

  private class DisableMouseListener implements MouseMotionListener {
    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
      resetMouseToCenter(mouseFrame);
    }
  }
}
