package ccpl.lib;

import java.awt.*;

/*****	The following class is used to draw the trial.  Use the constructor to set
******	Stimuli variables, and the draw method to do the drawing.
*****/





public class DrawTrial 
{  

	public DrawTrial (Specification stim) {
		numDots = stim.getParsedIntSpec(1);
	}
	
	public synchronized void draw (BlankPanel endPanel) {
			int i;
			Graphics g = endPanel.getGraphics ();
			
			Dimension d = endPanel.getSize ();
			int windowHeight = d.height;
			int windowWidth = d.width;

			int width1 = 10;
			int height1 = 10;
			MyColor c2 = new MyColor (200,200,200);
			Color c3 = new Color (200,0,0);

			g.setColor (new Color (200,200,200));
			for (i=0; i<numDots; i++) {
					c3 = g.getColor ();
					MyColor c1 = new MyColor (c3.getRed(), c3.getGreen(), c3.getBlue());
					int x = new RandomIntGenerator (0,windowWidth - 3).draw();
					int y = new RandomIntGenerator (0,windowHeight - 3).draw();
					c2 = c1.brighter ((float) 0.01);
					g.setColor (c2);
					g.fillOval (x, y, width1, height1);
			}
		g.dispose ();
   }
	 
	 private int numDots;
	 
}

