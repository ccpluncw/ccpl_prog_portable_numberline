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

public class Mask extends JPanel{

    private int numLines = 350;
    private final int lineThickness;
    private final int minLength = 50; //in pixels
    private boolean redraw;
    private Image intermediateImage;
    int[][] points;
    
    private final Color[] colors;
    public Mask(int lineThick, Color bkgColor, Color[] col){
        super();
        setBackground(bkgColor);
        lineThickness = lineThick;
        colors = col;
        numLines = 350;
        redraw = true;
        intermediateImage = null;
    }

    public void setNumberOfLines(int numberLines){
        numLines = numberLines;
        
        points = new int[numLines][4];
        
        /*
        final int overlap = 100;
        final int maxX = getWidth()+overlap, maxY = getHeight()+overlap;
        //RandomIntGenerator rColor = new RandomIntGenerator(0,colors.length-1);
        RandomIntGenerator rX = new RandomIntGenerator(0-overlap, maxX);
        RandomIntGenerator rY = new RandomIntGenerator(0-overlap, maxY);
        int x1, y1, x2, y2;
        for(int i=0;i<numLines;++i){
            //g2.setColor(colors[rColor.draw()]);
            x1 = rX.draw(); y1 = rY.draw(); x2 = rX.draw(); y2 = rY.draw();
            if(java.awt.Point.distance(x1, y1, x2, y2) < minLength){
                x2 += minLength;
                y2 += minLength;
            }
            points[i][0] = clamp(x1, maxX);
            points[i][1] = clamp(y1, maxY);
            points[i][2] = clamp(x2, maxX);
            points[i][3] = clamp(y2, maxY);
            //g2.drawLine(x1, y1, x2, y2);
        }
        */
    }
    
    public void refreshMask(){
        final int overlap = 100;
        final int maxX = getWidth()+overlap, maxY = getHeight()+overlap;
        //RandomIntGenerator rColor = new RandomIntGenerator(0,colors.length-1);
        RandomIntGenerator rX = new RandomIntGenerator(0-overlap, maxX);
        RandomIntGenerator rY = new RandomIntGenerator(0-overlap, maxY);
        int x1, y1, x2, y2;
        for(int i=0;i<numLines;++i){
            //g2.setColor(colors[rColor.draw()]);
            x1 = rX.draw(); y1 = rY.draw(); x2 = rX.draw(); y2 = rY.draw();
            if(java.awt.Point.distance(x1, y1, x2, y2) < minLength){
                x2 += minLength;
                y2 += minLength;
            }
            points[i][0] = clamp(x1, maxX);
            points[i][1] = clamp(y1, maxY);
            points[i][2] = clamp(x2, maxX);
            points[i][3] = clamp(y2, maxY);
            //g2.drawLine(x1, y1, x2, y2);
        }
    }

    public void renderLineMask(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(new BasicStroke(lineThickness));
        
        final int overlap = 100;
        final int maxX = getWidth()+overlap, maxY = getHeight()+overlap;
        RandomIntGenerator rColor = new RandomIntGenerator(0,colors.length-1);
        RandomIntGenerator rX = new RandomIntGenerator(0-overlap, maxX);
        RandomIntGenerator rY = new RandomIntGenerator(0-overlap, maxY);
        int x1, y1, x2, y2;
        for(int i=0;i<numLines;++i){
            g2.setColor(colors[rColor.draw()]);
            x1 = rX.draw(); y1 = rY.draw(); x2 = rX.draw(); y2 = rY.draw();
            if(java.awt.Point.distance(x1, y1, x2, y2) < minLength){
                x2 += minLength;
                y2 += minLength;
            }
            x1 = clamp(x1, maxX);
            y1 = clamp(y1, maxY);
            x2 = clamp(x2, maxX);
            y2 = clamp(y2, maxY);
            g2.drawLine(x1, y1, x2, y2);
        }
        
        /*
        RandomIntGenerator rColor = new RandomIntGenerator(0,colors.length-1);
        for(int i = 0; i < numLines; i ++){
            g2.setColor(colors[rColor.draw()]);
            g2.drawLine(points[i][0], points[i][1], points[i][2], points[i][3]);
        }
                */
    }


    public void redraw(boolean draw){
        redraw = draw;
        if(!draw)
            intermediateImage = null;
    }


    private static int clamp(int coord, int maxCoord){
        if(coord < 0)
            coord = 0;
        else if(coord > maxCoord)
            coord = maxCoord;
        return coord;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(intermediateImage == null){
            GraphicsConfiguration gc = getGraphicsConfiguration();
            intermediateImage = gc.createCompatibleImage(this.getParent().getWidth(),this.getParent().getHeight(), Transparency.BITMASK);
            Graphics2D gImg = (Graphics2D)intermediateImage.getGraphics();
            gImg.setComposite(AlphaComposite.Src);
            gImg.setColor(new Color(0,0,0,0));
            gImg.fillRect(0,0,this.getParent().getWidth(),this.getParent().getHeight());
            renderLineMask(gImg);
            gImg.dispose();
        }
        g.drawImage(intermediateImage, 0, 0, null);
    }
}
