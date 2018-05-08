package ccpl.lib.util;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Map;

public class UiUtil {
  private static Toolkit tk = Toolkit.getDefaultToolkit();

  public static int screenWidth() {
    return tk.getScreenSize().width;
  }

  public static int screenHeight() {
    return tk.getScreenSize().height;
  }

  public static void expandGridPanel(JPanel panel, String label, JComponent comp) {
    GridLayout gridLayout = (GridLayout) panel.getLayout();
    gridLayout.setRows(gridLayout.getRows() + 1);
    panel.add(new JLabel(label + ": "));
    panel.add(comp);
  }

  public static void addTrackedTxtField(String key, String label, JPanel panel,
                                        Map<String, JTextField> tracker, boolean expandGrid) {
    JTextField textField = new JTextField("0");
    addTrackedTxtField(textField, key, label, panel, tracker, expandGrid);
  }

  public static void addTrackedTxtField(JTextField txt, String key, String label, JPanel panel,
                                        Map<String, JTextField> tracker, boolean expandGrid) {
    tracker.put(key, txt);
    txt.setText("0");

    if (expandGrid) {
      expandGridPanel(panel, label, txt);
    } else {
      panel.add(new JLabel(label + ": "));
      panel.add(txt);
    }
  }
}
