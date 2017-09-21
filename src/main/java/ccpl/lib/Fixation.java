/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ccpl.lib;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import javax.swing.JPanel;

/**
 *
 * @author Owner
 */
public class Fixation extends JPanel{

    /*public static void main(String[] args){
        DrawExpFrame frame = new DrawExpFrame(new Response());
        frame.setBackground(Color.white);
        Fixation fix = new Fixation(Color.WHITE, Color.WHITE, Color.BLACK, (DrawExpFrame.getScreenWidth()/2), (DrawExpFrame.getScreenHeight()/2));
        frame.setContentPane(fix);
        frame.setVisible(true);
    }*/

    private static final Color DEFAULT_COLOR = Color.GRAY;
    private final int radius, x, y, diameter;

    private java.awt.geom.Line2D fixationLine;
    private final Color lineColor;
    private final int strokeSize;

    private RadialGradientPaint gradient;

    private Point2D center;
    
    public Fixation(Color bkgColor, int xPos, int yPos){
        super();
        setBackground(bkgColor);
        radius = 15;
        x = xPos;
        y = yPos;
        diameter = radius*2;
        fixationLine = null;
        lineColor = null;
        strokeSize = 0;
    }

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

    public Fixation(Color bkgColor, Color outerColor, Color innerColor, int centerWidth, int centerHeight, int theRadius){
        super();
        radius = theRadius;
        x = 0;
        y = 0;
        diameter = radius*2;
        lineColor = null;
        strokeSize = 0;
        setBackground(bkgColor);
        center = new Point2D.Float(centerWidth, centerHeight);
        float[] dist = {0.001f, 1.0f};
        Color[] colors = {innerColor, outerColor};
        gradient = new RadialGradientPaint(center, radius, dist, colors);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(fixationLine != null){
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new java.awt.BasicStroke(strokeSize));
            g2.setColor(lineColor);
            g2.draw(fixationLine);
        }else if(gradient != null){
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(gradient);
            g2.fillOval((int)(center.getX()-radius), (int)(center.getY()-radius), diameter, diameter);
        }else{
            g.setColor(DEFAULT_COLOR);
            g.fillOval(x-radius, y-radius, diameter, diameter);
        }
    }
}
