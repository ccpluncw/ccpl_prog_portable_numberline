/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ccpl.lib;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 *
 * @author programmer
 */
public class SliderNoThumb extends JSlider {
    
    private SliderNoThumbUI noThumb;
    public boolean hideThumb;
    private Color thumbColor;
    
    public SliderNoThumb(int min, int max, int initial, Color c){
        super(min, max, initial);
        setUI(new SliderNoThumbUI(this));
        hideThumb = true;
        thumbColor = c;
    }
    
    
    private class SliderNoThumbUI extends BasicSliderUI {
        
        SliderNoThumbUI(JSlider j){
            super(j);
        }
        

        @Override
        public void paintThumb(Graphics g){
            if(hideThumb){
            //do nothing to hide thumb
           }
            else{
                int[] x = new int[4];
                int[] y = new int[4];
                int n;
                
//                Integer cX = thumbRect.x;
//                Integer cY = thumbRect.y;
//                
//                x[0] = cX - 25;
//                x[1] = cX - 25;
//                y[0] = cY - 25;
//                y[1] = cY + 25;
//                x[2] = cX + 25;
//                x[3] = cX + 25;
//                y[2] = cY - 25;
//                y[3] = cY + 25;
//                n = 4;
//                
//                Polygon thumb = new Polygon(x, y, n);
                
                Rectangle thumb = new Rectangle(thumbRect.x + 3, thumbRect.y, 5, 20);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(thumbColor);
                g2.fill(thumb);
                
                
                //super.paintThumb(g);
            }
        }
        
        @Override
        protected void scrollDueToClickInTrack(int direction) {

            int value = slider.getValue(); 

            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                value = this.valueForXPosition(slider.getMousePosition().x);
            } else if (slider.getOrientation() == JSlider.VERTICAL) {
                value = this.valueForYPosition(slider.getMousePosition().y);
            }
            slider.setValue(value);
            hideThumb = false;
        }
        
        @Override
        public void paintFocus(Graphics g){
            //do nothing so there is no rectangle border when selected
        }
        
    }

}
