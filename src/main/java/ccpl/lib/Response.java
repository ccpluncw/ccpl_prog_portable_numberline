
package ccpl.lib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static ccpl.lib.Util.MouseUtilKt.resetMouseToCenter;

/**
 * The RESPONSE class is very important.  It specifies methods for collecting
 * and checking responses.  It implements KEYLISTENER so that keyboard responses
 * can be collected.  The class must be added to the panel as the keyListener
 * (i.e., addKeyListener (Response)
 * Before any timing routine is run, testTimer () must be run once
 */
public class Response implements KeyListener, ActionListener, ChangeListener {

  private int mouseClickButton = -1;
  protected boolean isSliderMoved = false;
  protected String textValue;
  protected long textRt;
  private long RT = 99999;
  private volatile char userChoice = '~';
  protected JDialog respFrame;
  private JButton sliderOkButton;
  protected JButton textOkButton;
  private JButton rbOkButton;
  private JButton dotOkButton;
  private JButton radioOkButton;
  protected JTextField textInput;
  protected long startTime;
  protected long endTime;
  protected volatile boolean inputDone;
  protected boolean closedProperly = true;
  private long loopsPerMS = 0;
  private NumPadResponse numPadResponse;
  private JCheckBox[] buttons;
  private List<Character> responseList;

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
    numPadResponse = null;
  }

  public void setFrame(JFrame f) {
    mouseFrame = f;
  }

  @Override
  public void keyTyped(KeyEvent keyEvent) {
  }

  /**
   * When a button is hit this is triggered
   * saves the button that was hit
   * and deals with it depending on the response mode
   *
   * @param evt
   */
  public void keyPressed(KeyEvent evt) {
    try {
      boolean isMultipleButton = false;
      if (evt.isControlDown() && (evt.getKeyChar() + "").toLowerCase().charAt(0) == 'q') {
        System.exit(0);
      } else if (isMultipleButton) {
        userChoice = (evt.getKeyChar() + "").toLowerCase().charAt(0);
        responseList.add(userChoice);
        resetUserChoice();
      } else {
        userChoice = (evt.getKeyChar() + "").toLowerCase().charAt(0);
      }
    } catch (Exception ignored) {
    }
  }

  @Override
  public void keyReleased(KeyEvent keyEvent) {

  }

  /**
   * YOU HAVE TO CALL THIS TO MAKE THE RESPONSE CLASS WORK
   * Calibrates timing to ensure accuracy
   *
   * @param inputPanel panel this method will draw in (its just going to indicate the timer is being
   *                   calibrated.
   * @param textColor  text color for the panel
   * @param reps       how many reps the timing calibrator will iterate through (normally 1000)
   * @return
   */
  public synchronized long testTimer(BlankPanel inputPanel, Color textColor, int reps) {
    long timeA, timeB;
    int i;
    long devisor, overRun;
    long[] testLoop = new long[reps];
    long tmpTot = 0;

    // reset the user choice so that the polling call below will work.
    Font myFont = new Font("SansSerif", Font.BOLD, 24);
    Dimension d1 = inputPanel.getSize();
    int dotAreaHeight = d1.height;
    int dotAreaWidth = d1.width;

    JLabel timerLabel = new JLabel("Please wait while the timer is calibrated.");
    inputPanel.setLayout(null);
    timerLabel.setForeground(textColor);
    timerLabel.setFont(myFont);
    timerLabel.setSize(timerLabel.getPreferredSize());
    timerLabel.setLocation(dotAreaWidth / 2 - (timerLabel.getWidth() / 2),
        dotAreaHeight / 2 - (timerLabel.getHeight() / 2));
    inputPanel.add(timerLabel);
    inputPanel.revalidate();
    inputPanel.repaint();

    for (i = 0; i < reps; i++) {
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

    for (i = 0; i < reps; i++) {
      tmpTot += testLoop[i];
    }

    loopsPerMS = tmpTot / reps;

    inputPanel.remove(timerLabel);
    inputPanel.validate();

    return loopsPerMS;
  }

  private long pollForKeyResponse(char c) {
    long rt;
    long timeA, timeB, tmpTime;
    long overRun;

    overRun = 0;
    timeA = new Date().getTime();
    timeB = new Date().getTime();
    tmpTime = timeA;

    while (getUserChoice() != c) {
      timeB = new Date().getTime();
      if (tmpTime != timeB) {
        overRun = 0;
        tmpTime = timeB;
      } else {
        overRun++;
      }
    }

    rt = (timeB - timeA) + (overRun / loopsPerMS);

    return rt;
  }

  private long pollForMouseResponse(NumberLine numLine) {
    long rt;
    long timeA, timeB, tempTime;
    long overRun;

    overRun = 0;
    timeA = new Date().getTime();
    timeB = new Date().getTime();
    tempTime = timeA;

    // This is a hack to pass variables between anonymous classes.
    // Since the array is final, the anonymous class can use it
    // But since it is an array, you can change the element at a position
    // Huzzah! You just learned some truly black magic, Java wizardry.
    // Try NOT to do this, it may have unintended side effects!
    final boolean[] flag = {true};

    MouseListener tempListener = new MouseListener() {
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

    numLine.linePanel.addMouseListener(tempListener);

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

    numLine.linePanel.removeMouseListener(tempListener);

    rt = (timeB - timeA) + (overRun / loopsPerMS);

    return rt;
  }

  private long pollForResponse() {
    long rt;
    long timeA, timeB, tmpTime;
    long overRun = 0;


    timeA = new Date().getTime();
    timeB = new Date().getTime();
    tmpTime = timeA;


    while (!getInputDone()) {
      timeB = new Date().getTime();
      if (tmpTime != timeB) {
        overRun = 0;
        tmpTime = timeB;
      } else {
        overRun++;
      }
    }

    rt = (timeB - timeA) + (overRun / loopsPerMS);
    return rt;
  }

  public synchronized void displayNotificationFrame(JFrame parent, String info) {
    inputDone = false;
    textOkButton = new JButton("OK");
    createNotificationFrame(parent, info, textOkButton, 500, 250);
  }


  public void getTimedNumPadResponse(JFrame parent, String info, String targetFieldFormat) {

    if (loopsPerMS == 0) {
      JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error", "alert", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }

    createNumPadResponseFrame(parent, info, targetFieldFormat);

    inputDone = false;
    textRt = pollForResponse();

    textValue = numPadResponse.getResponse();
  }


  public long getTimedNumberLineResponse(NumberLine lPanel, boolean useMouse) {
    //Only the spacebar will trigger a timer stop, anything else is ignored
    char submitKey = ' ';

    if (loopsPerMS == 0) {
      JOptionPane.showMessageDialog(null, "Must run the testTimer first.  Fatal Error",
          "alert", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }

    // reset the user choice so that the polling call below will work.
    resetUserChoice();

    //Idle here until user has dragged the handle
    while (!lPanel.isHandleDragged()) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException ex) {
        Logger.getLogger(Response.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    // Reset choice because spacebar may have been pressed already.
    resetUserChoice();

    //User has moved handle so respond to the space bar
    if (!useMouse) {
      RT = pollForKeyResponse(submitKey);
    } else {
      RT = pollForMouseResponse(lPanel);
    }

    return RT;
  }

  public void actionPerformed(ActionEvent evt) {
    if (respFrame != null) {
      JButton button = (JButton) evt.getSource();

      if (button.equals(sliderOkButton)) {
        if (isSliderMoved) {
          endTime = new Date().getTime();
          respFrame.setVisible(false);
          respFrame.dispose();
          respFrame = null;
          inputDone = true;
        }
      }
      if (button.equals(textOkButton)) {
        endTime = new Date().getTime();
        textRt = endTime - startTime;
        if (textInput != null && !"".equals(textInput.getText())) {
          textValue = (textInput.getText()).trim();
          respFrame.dispose();
          inputDone = true;
        } else if (numPadResponse != null) {
          textValue = numPadResponse.getResponse();
          if (numPadResponse.validateResponse(textValue)) {
            boolean legacy = true;
            if (legacy) {
              respFrame.setVisible(false);
              respFrame.dispose();
              respFrame = null;
            } else {
              respFrame.setVisible(false);
            }

            inputDone = true;
          }
        } else if (textInput == null) {
          respFrame.setVisible(false);
          respFrame.dispose();
          inputDone = true;
        }
      }

      if (button.equals(rbOkButton)) {
        endTime = new Date().getTime();
        List<Integer> boxId = new ArrayList<>();

        int temp = boxId.size();

        for (int i = 0; i < buttons.length; i++) {

          if (buttons[i].isSelected()) {
            boxId.add(i);
          }
        }

        if (temp != boxId.size()) {
          inputDone = true;
          respFrame.setVisible(false);
          respFrame.dispose();
          respFrame = null;
        } else {
          JOptionPane.showMessageDialog(respFrame.getContentPane(), "Please select one or more options");
        }
      }
      if (button.equals(dotOkButton)) {
        respFrame.dispose();
        inputDone = true;
      }
      if (button.equals(radioOkButton)) {
        endTime = new Date().getTime();
        respFrame.setVisible(false);
        respFrame.dispose();
        respFrame = null;
        inputDone = true;
      }
    }
  }

  public void stateChanged(ChangeEvent changeEvent) {
    Object source = changeEvent.getSource();
    if (source instanceof JSlider) {
      JSlider jslider = (JSlider) source;
      if (!jslider.getValueIsAdjusting()) {
        isSliderMoved = true;
      }
    }
  }

  public void createNotificationFrame(JFrame parent, String label, JButton okButton, int width, int height) {
    respFrame = new JDialog(parent, false);
    respFrame.setTitle("Notification");

    // dont let people close out
    respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    closedProperly = true;

    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension d = tk.getScreenSize();
    respFrame.setSize(width, height);
    respFrame.setLocation((d.width - width) / 2, (d.height - height) / 2);
    Color tempC = new Color(200, 200, 200);
    BlankPanel inputPanel = new BlankPanel(tempC);
    BlankPanel buttonPanel = new BlankPanel(tempC);
    BlankPanel blackPanelA = new BlankPanel(tempC);
    BlankPanel blackPanelB = new BlankPanel(tempC);
    BlankPanel blackPanelC = new BlankPanel(tempC);

    /*** this set of code allows one to get multiple line headers on the slider ***/
    JLabel rLabel = new JLabel(label);
    Font labelFont = rLabel.getFont();
    FontMetrics fm = rLabel.getFontMetrics(labelFont);
    int fontWidth;

    int i = 0;
    int totalTokens;
    String[] outString = new String[5];
    String tmp;
    StringTokenizer t = new StringTokenizer(label, " ");

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

    Insets cInset = new Insets(10, 10, 10, 10);
    GridBagLayout gridBag = new GridBagLayout();
    inputPanel.setLayout(gridBag);
    GridBagConstraints c = new GridBagConstraints();
    c.insets = cInset;
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

    while (!getInputDone()) {
      Thread.yield();
    }

    if (!closedProperly) {
      okButton.doClick();
      JOptionPane.showMessageDialog(null,
          "You must choose an option.  Do NOT use the Close Box", "alert",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public void createNumPadResponseFrame(JFrame parent, String label, String targetFieldFormat) {
    respFrame = new JDialog(parent);
    respFrame.setTitle("Response");

    respFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    numPadResponse = new NumPadResponse(targetFieldFormat.trim(), respFrame);

    textOkButton = new JButton("OK");
    JPanel inputPanel = new JPanel(new BorderLayout(3, 5));
    JPanel labelPanel = new JPanel(new FlowLayout());
    JPanel buttonPanel = new JPanel(new FlowLayout());

    labelPanel.add(new JLabel(label));
    buttonPanel.add(textOkButton);

    inputPanel.add(labelPanel, BorderLayout.NORTH);
    inputPanel.add(numPadResponse.getPanel(), BorderLayout.CENTER);
    inputPanel.add(buttonPanel, BorderLayout.SOUTH);
    respFrame.getContentPane().add(inputPanel);

    Dimension respFrameDim = respFrame.getPreferredSize();

    final int respFrameWidth = respFrameDim.width + 150;
    final int respFrameHeight = respFrameDim.height + 75;
    respFrame.setSize(respFrameWidth, respFrameHeight);
    respFrame.setLocation((parent.getWidth() - respFrameWidth) / 2, (parent.getHeight() - respFrameHeight) / 2);
    respFrame.setResizable(false);

    textOkButton.addActionListener(this);
    respFrame.getRootPane().setDefaultButton(textOkButton);
    respFrame.addKeyListener(this);
    respFrame.setVisible(true);

  }

  public char getUserChoice() {
    return userChoice;
  }

  public void setUserChoice(char a) {
    userChoice = a;
  }

  public void resetUserChoice() {
    userChoice = '~';
  }

  public long getRT() {
    return RT;
  }

  public boolean getInputDone() {
    return inputDone;
  }

  public int getMouseClickButton() {
    return mouseClickButton;
  }

  public void resetMouseClickButton() {
    mouseClickButton = -1;
  }

  public long getTextRT() {
    return textRt;
  }

  public String getTextValue() {
    return textValue;
  }

  private final DualMouseClickListener dmcl = new DualMouseClickListener();

  public synchronized long getTimedMouseClickResponse(Component parent) {
    long timeA, timeB, tmpTime;
    long overRun;
    parent.addMouseListener(dmcl);

    overRun = 0;
    timeA = new Date().getTime();
    timeB = new Date().getTime();
    tmpTime = timeA;

    resetMouseClickButton();
    int x = getMouseClickButton();

    /*
     * The following polls a variable to make the computer wait for the
     * mouse response
     */
    while (x == -1) {
      x = getMouseClickButton();
      System.out.print("");
      timeB = new Date().getTime();
      if (tmpTime != timeB) {
        overRun = 0;
        tmpTime = timeB;
      } else {
        overRun++;
      }
    }

    RT = (timeB - timeA) + (overRun / loopsPerMS);

    parent.removeMouseListener(dmcl);

    return RT;
  }

  private class DualMouseClickListener implements MouseListener {

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
      int temp = e.getButton();


      switch (temp) {
        case 1:
          mouseClickButton = temp;
          break;
        case 3:
          mouseClickButton = temp;
          break;

        default:
          break;
      }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
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
    public void mouseDragged(MouseEvent e) { }

    public void mouseMoved(MouseEvent e) {
      resetMouseToCenter(mouseFrame);
    }
  }
}
