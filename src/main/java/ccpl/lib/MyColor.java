package ccpl.lib;

import java.awt.*;



/***** The MYCOLOR class was written to change the BRIGHTER method so one can
****** specify the increase in intensity of the  color.
*****/
public class MyColor extends Color
{ 
	public MyColor (int r, int g, int b) {
		super (r,g,b);
	}

	public MyColor (int col) {
		super (col);
	}

	public MyColor brighter (float brightenIncrement) {
		float[] hsbVals = new float [3];
		int newColor1;
		int i;
		
		int r = getRed ();
		int g = getGreen ();
		int b = getBlue ();
		
		RGBtoHSB (r,g,b,hsbVals);
		hsbVals [2] += brightenIncrement;
		if (hsbVals [2] >= 1.0) hsbVals [2] = (float) 1.0;
		newColor1 = HSBtoRGB (hsbVals[0],hsbVals[1],hsbVals[2]);
		return new MyColor (newColor1);
	}	
	
	private int r;
	private int g;
	private int b;
}
			
