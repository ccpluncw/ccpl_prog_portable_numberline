package ccpl.lib;

import java.awt.*;
import javax.swing.*;



/*****	The following creates a blank panel with the appropriate background color.
*****/
public class BlankPanel extends JPanel
{
	public BlankPanel () {
		setBackground (Color.black);
	}

	public BlankPanel (Color backColor) {
		setBackground (backColor);
	}

        @Override
	public void paintComponent (Graphics g) {
		super.paintComponent (g);
	}
  
/*** this method creates a virtual matrix in which one can place stimuli.  It returns 
**** the x y coordinates of the center of the cell requested.  You provide the 
**** x and y boundaries (these are areas that the matrix falls within), number of columns
**** and rows (columns, rows) and the cell number indexed by its row and column 
**** numbers (columnNumber, rowNumber)
***/
  public int [] getMatrixPosition (int xSize, int ySize, int columns, 
      int rows, int columnNumber, int rowNumber) {
      int [] xyVal = new int [2];
      int xBoundary, yBoundary;
      Dimension d1 = getSize ();
      
      /** I am determining the center points of each cell, so I must 
      *** add 1 to the matrix size. By adding 1, the center point is where the 
      *** boundaries cross
      **/
      columns += 1;
      rows += 1;
      
      xBoundary = (d1.width - xSize)/2;
      yBoundary = (d1.height - ySize)/2;
      
      
      xyVal [0] = (((d1.width - (xBoundary*2)) / columns) * columnNumber) + xBoundary;
      xyVal [1] = (((d1.height - (yBoundary*2)) / rows) * rowNumber) + yBoundary;
      
      return xyVal;
  }

}

