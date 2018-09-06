package ccpl.lib;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import javax.swing.JPanel;

/** The Fixation line shown before a trial so the subject will focus on it. */
public class Fixation extends JPanel {

  private static final Color DEFAULT_COLOR = Color.GRAY;

  private final int radius;
  private final int diameter;

  private final int coordX;
  private final int coordY;

  private final java.awt.geom.Line2D fixationLine;
  private final Color lineColor;
  private final int strokeSize;

  /**
   * Create a Fixation panel with a background color, line color, stroke width, and line.
   *
   * @param bkgColor Background color
   * @param fixLineColor Line color
   * @param stroke Stroke width in pixels
   * @param line Line object being drawn
   */
  public Fixation(Color bkgColor, Color fixLineColor, int stroke, Line2D line) {
    super();
    setBackground(bkgColor);
    radius = 0;
    coordX = 0;
    coordY = 0;
    diameter = 0;
    fixationLine = line;
    lineColor = fixLineColor;
    strokeSize = stroke;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (fixationLine != null) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setStroke(new java.awt.BasicStroke(strokeSize));
      g2.setColor(lineColor);
      g2.draw(fixationLine);

      return;
    }

    g.setColor(DEFAULT_COLOR);
    g.fillOval(coordX - radius, coordY - radius, diameter, diameter);
  }
}
