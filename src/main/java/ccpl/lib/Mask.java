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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Transparency;
import javax.swing.JPanel;

public class Mask extends JPanel {

  private int numLines;
  private final int lineThickness;
  private Image intermediateImage;

  private final Color[] colors;

  /**
   * Initialize a mask to disturb visual after images.
   *
   * @param lineThick Thickness of the line
   * @param bkgColor Background color
   * @param col Colors for the lines
   */
  public Mask(int lineThick, Color bkgColor, Color[] col) {
    super();
    setBackground(bkgColor);
    lineThickness = lineThick;
    colors = col;
    numLines = 350;
    intermediateImage = null;
  }

  private void renderLineMask(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setStroke(new BasicStroke(lineThickness));

    final int overlap = 100;
    final int maxX = getWidth() + overlap;
    final int maxY = getHeight() + overlap;
    RandomIntGenerator randColor = new RandomIntGenerator(0, colors.length - 1);
    RandomIntGenerator randX = new RandomIntGenerator(0 - overlap, maxX);
    RandomIntGenerator randY = new RandomIntGenerator(0 - overlap, maxY);

    int x1;
    int y1;
    int x2;
    int y2;

    for (int i = 0; i < numLines; ++i) {
      g2.setColor(colors[randColor.draw()]);
      x1 = randX.draw();
      y1 = randY.draw();
      x2 = randX.draw();
      y2 = randY.draw();
      // in pixels
      int minLength = 50;
      if (java.awt.Point.distance(x1, y1, x2, y2) < minLength) {
        x2 += minLength;
        y2 += minLength;
      }
      x1 = clamp(x1, maxX);
      y1 = clamp(y1, maxY);
      x2 = clamp(x2, maxX);
      y2 = clamp(y2, maxY);
      g2.drawLine(x1, y1, x2, y2);
    }
  }

  private static int clamp(int coord, int maxCoord) {
    if (coord < 0) {
      coord = 0;
    } else if (coord > maxCoord) {
      coord = maxCoord;
    }
    return coord;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (intermediateImage == null) {
      GraphicsConfiguration gc = getGraphicsConfiguration();

      intermediateImage =
          gc.createCompatibleImage(
              this.getParent().getWidth(), this.getParent().getHeight(), Transparency.BITMASK);

      Graphics2D graphicsImg = (Graphics2D) intermediateImage.getGraphics();
      graphicsImg.setComposite(AlphaComposite.Src);
      graphicsImg.setColor(new Color(0, 0, 0, 0));
      graphicsImg.fillRect(0, 0, this.getParent().getWidth(), this.getParent().getHeight());
      renderLineMask(graphicsImg);
      graphicsImg.dispose();
    }
    g.drawImage(intermediateImage, 0, 0, null);
  }
}
