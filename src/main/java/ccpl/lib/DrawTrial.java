package ccpl.lib;

import java.awt.*;

import static java.awt.Color.HSBtoRGB;
import static java.awt.Color.RGBtoHSB;

/*****	The following class is used to draw the trial.  Use the constructor to set
 ******	Stimuli variables, and the draw method to do the drawing.
 *****/
public class DrawTrial {

  private int numDots;

  public DrawTrial(Specification stim) {
    numDots = stim.getParsedIntSpec(1);
  }

  public synchronized void draw(BlankPanel endPanel) {
    int i;
    Graphics g = endPanel.getGraphics();

    Dimension d = endPanel.getSize();
    int windowHeight = d.height;
    int windowWidth = d.width;

    int width1 = 10;
    int height1 = 10;
    Color c2;
    Color c3;

    g.setColor(new Color(200, 200, 200));
    for (i = 0; i < numDots; i++) {
      c3 = g.getColor();
      Color c1 = new Color(c3.getRed(), c3.getGreen(), c3.getBlue());
      int x = new RandomIntGenerator(0, windowWidth - 3).draw();
      int y = new RandomIntGenerator(0, windowHeight - 3).draw();
      c2 = brighten(c1, 0.01f);
      g.setColor(c2);
      g.fillOval(x, y, width1, height1);
    }
    g.dispose();
  }

  private Color brighten(Color color, float brightenIncrement) {
    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();

    float[] hsbVals = new float[3];
    RGBtoHSB(r, g, b, hsbVals);
    hsbVals[2] += brightenIncrement;

    if (hsbVals[2] >= 1.0) {
      hsbVals[2] = (float) 1.0;
    }

    return new Color(HSBtoRGB(hsbVals[0], hsbVals[1], hsbVals[2]));
  }
}

