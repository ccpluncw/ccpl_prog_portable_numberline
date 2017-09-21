package ccpl.lib.fractal;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.Timer;


public class FadeTransition extends FractalTransition implements ActionListener{

    private SlidePanel slidePanel;
    private volatile double opacity, opacityStep;
    private volatile boolean transitionedIn, atTransitionEnd;

    public FadeTransition(Fractal f, int time, int presentTime){
        super(f, time, presentTime);
        opacity = 0.0;
        opacityStep = 100.0*REFRESH_RATE/TRANSITION_TIME;
        transitionedIn = false;
        atTransitionEnd = false;
    }

    @Override
    public void play() {
        updateTimer = new Timer(REFRESH_RATE,this);
        slidePanel = new SlidePanel();
        transitionPanel.add(slidePanel);
        transitionPanel.revalidate();
        updateTimer.start();
        while(updateTimer.isRunning()){
            delay(10);
        }
        updateTimer.setInitialDelay(PRESENT_TIME);
        updateTimer.restart();
        while(updateTimer.isRunning()){
            delay(10);
        }
    }

    private void transitionIn(){
            opacity+=opacityStep;
            if(!atTransitionEnd && opacity >= 100){
                opacity = 100.0;
                atTransitionEnd = true;
            }else if (opacity >= 100.0){
                transitionedIn = true;
                atTransitionEnd = false;
                opacity = 100.0;
                updateTimer.stop();
            }
    }

    private void transitionOut(){
         opacity-=opacityStep;
         if(!atTransitionEnd && opacity <= 0){
             opacity = 0.0;
             atTransitionEnd = true;
             updateTimer.stop();
         }
    }

    public void actionPerformed(ActionEvent ae) {
        if(!updateTimer.isRunning()){
            updateTimer.stop();
            return;
        }
        //slidePanel.fade();
        slidePanel.repaint();

        if(!transitionedIn){
            transitionIn();
        }else{
            transitionOut();
        }
    }
    
    private class SlidePanel extends JPanel {
        private int width;
        private int height;
        private Image fractalImage;
        
        public SlidePanel(){
            super(null);
            width = fractal.getWidth();
            height = fractal.getHeight();
            setBackground(Color.BLACK);
            setBounds(0, 0, width, height);
            fractalImage = fractal.image;
        }


        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            if(fractalImage != null){
              g2.setComposite(AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float)(opacity/100.0)));
              g2.drawImage(fractalImage, 0, 0, null);
            }
        }
        
    }

}
