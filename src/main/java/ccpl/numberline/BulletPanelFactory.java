package ccpl.numberline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BulletPanelFactory {
  public static JPanel getBulletPanel(List<String> args, ActionListener listener) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);
    JPanel bodyPanel = new JPanel();
    JPanel okPanel = new JPanel();

    bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
    bodyPanel.setBackground(Color.WHITE);

    JButton ok = new JButton("OK");
    ok.addActionListener(listener);
    okPanel.add(ok);
    okPanel.setBackground(Color.white);

    List<JLabel> labels = new ArrayList<>();

    for (String arg : args) {
      JLabel label = new JLabel("• " + arg);
      label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      labels.add(label);
    }

    labels.forEach(bodyPanel::add);
    bodyPanel.add(okPanel);

    JPanel wrapperPanel = new JPanel(new GridBagLayout());
    wrapperPanel.setBackground(Color.WHITE);
    wrapperPanel.add(bodyPanel);

    panel.add(wrapperPanel, BorderLayout.CENTER);

    return panel;
  }
}
