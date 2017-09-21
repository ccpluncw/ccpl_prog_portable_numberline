package ccpl.lib.fractal;

import ccpl.lib.DrawExpFrame;
import ccpl.lib.RandomIntGenerator;
import ccpl.lib.feedback.FeedbackController;

import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

/**
 * @author Kyle
 */
public class FractalGenerator {
   private static FractalTransition fractalTrans;
   private static int fractalTransitionTime = 1000;
   private static int fractalPresentTime = 1000;
   public static final int fractalWidth = DrawExpFrame.getScreenWidth();
   public static final int fractalHeight = DrawExpFrame.getScreenHeight();
   private static FeedbackController controller;
   public static JPanel fractalPanel;
   public static String fractalName;
   public boolean useBlockTransition;
   public boolean useFadeTransition;
   
   public FractalGenerator( int transitionRunTime, int presentTime, boolean determined, int frequency, boolean useBlock, boolean useFade){
        controller = new FeedbackController(determined, frequency);
        fractalTransitionTime = transitionRunTime;
        fractalPresentTime = presentTime;
        useBlockTransition = useBlock;
        useFadeTransition = useFade;
    }
   
   public boolean nextFractal(boolean useJulia, boolean useMandelbrot){
        fractalPanel = null;
        controller.incCount();
        boolean showFractal = controller.feedbackStatus();
        if(showFractal){
            fractalTrans = getRandomTransition(getRandomFractal(useJulia, useMandelbrot));
            fractalPanel = fractalTrans.getPanel();
            fractalPanel.setLocation(0, 0);
        }else
            fractalName = "-";
        return showFractal;
   }

   public void playTransition(){
       fractalTrans.play();
       fractalName = "-";
   }


   private Fractal getRandomFractal(boolean useJulia, boolean useMandelbrot){
       Fractal fractal = null;
       RandomIntGenerator fractalGen = new RandomIntGenerator(0, 1);
       if((useJulia && (useMandelbrot && fractalGen.draw() == 0)) || !useMandelbrot){
           fractalGen = new RandomIntGenerator(5.8, 6.6, 0.2);
           float rW = (float)fractalGen.drawDoubleWithInterval();
           float rH = (float)fractalGen.drawDoubleWithInterval();
           fractalGen = new RandomIntGenerator(-4.0, 1.0, 0.15);
           double aCn = fractalGen.drawDoubleWithInterval();
           double bCn = fractalGen.drawDoubleWithInterval();
           fractalGen = new RandomIntGenerator(70, 80);
           int iterations = fractalGen.draw();
           fractalGen = new RandomIntGenerator(25.0, 29.0, 1.0);
           float blowup = (float)fractalGen.drawDoubleWithInterval();
           fractal = new Julia(fractalWidth, fractalHeight, new Rectangle2D.Float(-2.0f, -4.0f, rW, rH), aCn, bCn, iterations, blowup);
       }else{
           fractalGen = new RandomIntGenerator(-2.5, -1.0, 0.2);
           float rX = (float)fractalGen.drawDoubleWithInterval();
           float rY = (float)fractalGen.drawDoubleWithInterval();
           fractalGen = new RandomIntGenerator(2.0, 4.0, 0.2);
           float rW = (float)fractalGen.drawDoubleWithInterval();
           float rH = (float)fractalGen.drawDoubleWithInterval();
           fractalGen = new RandomIntGenerator(-1.0, 1.0, 0.015);
           float x = (float)fractalGen.drawDoubleWithInterval();
           float y = (float)fractalGen.drawDoubleWithInterval();
           fractalGen = new RandomIntGenerator(2,8);
           int bounds = fractalGen.draw();
           fractal = new Mandelbrot(fractalWidth, fractalHeight, new Rectangle2D.Float(rX, rY, rW, rH), x, y, bounds);
       }
       fractalName = fractal.getClass().getSimpleName();
       return fractal;
   }
   
   private FractalTransition getRandomTransition(Fractal fractal){
       FractalTransition transition = null;
       RandomIntGenerator fractalGen = new RandomIntGenerator(0, 1);
       if((useBlockTransition && (useFadeTransition && fractalGen.draw() == 0)) || !useFadeTransition){
           transition = new RandomBlockTransition(fractal, fractalTransitionTime, fractalPresentTime);
       }else{
           transition = new FadeTransition(fractal, fractalTransitionTime, fractalPresentTime);
       }
       return transition;
   }



}

