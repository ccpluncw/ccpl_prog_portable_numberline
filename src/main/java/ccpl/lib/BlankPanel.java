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

}

