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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;

/**
 * Helper class to abstract the concept of a NumberLine handle.
 *
 * <p>This manages the physical handle as well as the guide line.
 */
public class Handle extends Line2D.Double {

  private Color color;
  private Line2D guide;

  private Color baseColor;
  private Color activeColor;

  private int thickness;

  /**
   * Create a new Handle with a given Handle line, guide, and color.
   *
   * @param line Line that will be the handle.
   * @param guide Guide line to the specific value.
   * @param color Color of the handle and guide line.
   */
  public Handle(Line2D line, Line2D guide, Color color, int thickness) {
    super(line.getP1(), line.getP2());
    this.guide = new Line2D.Double(guide.getP1(), guide.getP2());
    this.color = color;
    this.baseColor = color;
    this.activeColor = color;
    this.thickness = thickness;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Draw the handle in guide line in a desired color.
   *
   * <p>The guide line in this implementation is 1px in size when drawn so the stroke is saved at
   * the beginning and restored afterwards.
   *
   * @param g Graphics used for drawing.
   */
  public void draw(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    Stroke stroke = g2.getStroke();

    g2.setColor(color);

    g2.setStroke(new BasicStroke(thickness));
    g2.draw(guide);

    g2.setStroke(stroke);
  }

  public void setBaseColor(Color newColor) {
    this.baseColor = newColor;
  }

  public void setActiveColor(Color newColor) {
    this.activeColor = newColor;
  }

  public void useBaseColor() {
    color = baseColor;
  }

  public void useActiveColor() {
    color = activeColor;
  }

  public Line2D getGuide() {
    return guide;
  }
}
