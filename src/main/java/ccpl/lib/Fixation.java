/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ccpl.lib;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author Owner
 */
public class Fixation extends JPanel{

    private static final Color DEFAULT_COLOR = Color.GRAY;
    private final int radius, x, y, diameter;

    private final java.awt.geom.Line2D fixationLine;
    private final Color lineColor;
    private final int strokeSize;

    public Fixation(Color bkgColor, Color fixLineColor, int stroke, java.awt.geom.Line2D line){
        super();
        setBackground(bkgColor);
        radius = 0;
        x = 0;
        y = 0;
        diameter = 0;
        fixationLine = line;
        lineColor = fixLineColor;
        strokeSize = stroke;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(fixationLine != null){
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new java.awt.BasicStroke(strokeSize));
            g2.setColor(lineColor);
            g2.draw(fixationLine);
        }else{
            g.setColor(DEFAULT_COLOR);
            g.fillOval(x-radius, y-radius, diameter, diameter);
        }
    }
}
