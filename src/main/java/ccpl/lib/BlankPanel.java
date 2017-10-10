package ccpl.lib;


import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;

/**
 * The following creates a blank panel with the appropriate background color.
 */
public class BlankPanel extends JPanel {
  public BlankPanel() {
    setBackground(Color.black);
  }

  public BlankPanel(Color backColor) {
    setBackground(backColor);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
  }

}

