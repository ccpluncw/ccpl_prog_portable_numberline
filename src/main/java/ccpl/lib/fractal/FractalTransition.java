package ccpl.lib.fractal;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.Timer;


public abstract class FractalTransition{

    protected Fractal fractal;
    protected JPanel transitionPanel;
    protected final int TRANSITION_TIME;
    protected final int PRESENT_TIME;
    protected Timer updateTimer;
    protected static final int REFRESH_RATE = FractalTransition.getRefreshRate();

    public FractalTransition(Fractal f, int transitionTime, int presentTime){
        fractal = f;
        TRANSITION_TIME = transitionTime;
        PRESENT_TIME = presentTime;
        transitionPanel = new JPanel(null);
        transitionPanel.setSize(fractal.getWidth(), fractal.getHeight());
        transitionPanel.setBackground(Color.BLACK);
    }
    
    public abstract void play();

    public JPanel getPanel(){
        return transitionPanel;
    }

    protected static void delay(int millseconds){
        try {
            Thread.sleep(millseconds);
        } catch (InterruptedException ex) {
            Logger.getLogger(FractalTransition.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static int getRefreshRate(){
        final int DEFAULT_RATE = 60;
        int rate = 1;
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();
        for (GraphicsDevice device : devices) {
            rate = device.getDisplayMode().getRefreshRate();
            break;
        }
        if(rate == DisplayMode.REFRESH_RATE_UNKNOWN)
            rate = DEFAULT_RATE;
        return milliPerHertz(rate);
    }

    private static int milliPerHertz(int hertz){
        return (int)Math.ceil(1000.0/hertz);
    }

}
