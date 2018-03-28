package ccpl.lib;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Helper class to abstract the concept of a NumberLine handle.
 *
 * This manages the physical handle as well as the guide line.
 */
public class Handle extends Line2D.Double {

  private Color color;
  private Line2D guide;

  /**
   * Create a new Handle with a given Handle line, guide, and color.
   * @param line    Line that will be the handle.
   * @param guide   Guide line to the specific value.
   * @param color   Color of the handle and guide line.
   */
  public Handle(Line2D line, Line2D guide, Color color) {
    super(line.getP1(), line.getP2());
    this.guide = new Line2D.Double(guide.getP1(), guide.getP2());
    this.color = color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Draw the handle in guide line in a desired color.
   *
   * The guide line in this implementation is 1px in size when drawn
   * so the stroke is saved at the beginning and restored afterwards.
   *
   * @param g   Graphics used for drawing.
   */
  public void draw(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    Stroke stroke = g2.getStroke();

    g2.setColor(color);
    g2.draw(this);

    g2.setStroke(new BasicStroke(1));
    g2.draw(guide);

    g2.setStroke(stroke);
  }

  public Line2D getGuide() {
    return guide;
  }
}
