package ccpl.lib.feedback;

import ccpl.lib.Experiment;
import ccpl.lib.ImageProcessingPanel;
import ccpl.lib.fractal.FractalGenerator;

import java.awt.Color;
import java.io.InputStream;
import java.net.URL;
import javax.swing.JPanel;

public class Feedback {
    private enum FEEDBACKTYPE {ANIMATION, IMAGE, FRACTAL, NONE}
    private static FEEDBACKTYPE feedbackType = null;
    private static FeedbackVideoPlayer player = null;
    private static FractalGenerator fractalGenerator = null;
    private static boolean useJulia = false, useMandelbrot = false;
    private static ImageProcessingPanel imagePanel = null;
    private static int imagePresentTime = 2000;
    private static boolean giveVideo = false, giveFractal = false, giveImage = false;

    private Feedback(){ }

    public static void setFeedback(boolean provideVideoFeedback, boolean provideFractalFeedback, boolean provideImageFeedback){
        giveVideo = provideVideoFeedback;
        giveFractal = provideFractalFeedback;
        giveImage = provideImageFeedback;
    }
    
    public static void initVideoFeedback(InputStream videoListStream, boolean isDetermined, int playFreq){
        if(giveVideo)
            player = new FeedbackVideoPlayer(videoListStream, isDetermined, playFreq);
    }

    public static void initImageFeedback(InputStream imgListStream, int presentTime){
        if(giveImage){
            imagePanel = new ImageProcessingPanel(imgListStream, true);
            imagePresentTime = presentTime;
        }
    }

    public static void initFractalFeedback(int transitionTime, int presentTime, boolean isDetermined, int freq, boolean blockTrans, boolean fadeTrans, boolean showJulia, boolean showMandelbrot){
        if(giveFractal){
            fractalGenerator = new FractalGenerator(transitionTime, presentTime, isDetermined, freq, blockTrans, fadeTrans);
            useJulia = showJulia;
            useMandelbrot = showMandelbrot;
        }
    }
    
    public static String getCurrentResourceName(){
        String resourceName = "";
        switch(feedbackType){
            case ANIMATION:
                resourceName = player.getCurrentVideoName();
                break;
            case IMAGE:
                resourceName = imagePanel.getCurrentImageName();
            default: // For debugging purposes. Handles unexpected feedbackType
                break;
        }
        return resourceName;
    }


    public static void setNextFeedback(int trialType, int trialNum, int totalTrials){
                //Set the next video from our player if applicable
                boolean animationToPlay = false;
                boolean fractalToDisplay = false;
                
                if(giveVideo){
                   if(trialType == 0 && trialNum == (totalTrials-1)){ //Only play animation at end of practice trials
                        player.getRandomVideoAsResource();
                        animationToPlay = true;
                   }else if(trialType == 1) //Play animation according to parameters of db file for experiment trials
                        animationToPlay = player.nextVideoAsResource();
                   if(animationToPlay){
                     feedbackType = FEEDBACKTYPE.ANIMATION;
                   }
                }

                
                else if(giveFractal){
                   if(!animationToPlay){
                        fractalToDisplay = fractalGenerator.nextFractal(useJulia, useMandelbrot);
                        if(fractalToDisplay)
                            feedbackType = FEEDBACKTYPE.FRACTAL;
                   }
                }

                else if(giveImage){
                    imagePanel.getRandomImageAsResource();
                    if(!animationToPlay && !fractalToDisplay){
                        feedbackType = FEEDBACKTYPE.IMAGE;
                    }
                }
                else {
                    feedbackType = FEEDBACKTYPE.NONE;
                }
                
                // Null condition checking.
                if(feedbackType == null) {
                    // Default to none.
                    feedbackType = FEEDBACKTYPE.NONE;
                }
    }

    public static void loadFeedbackResource(URL feedbackResource){
        switch(feedbackType){
           case ANIMATION:
                player.loadVideoAsResource(feedbackResource);
                break;
           case IMAGE:
               imagePanel.loadImageAsResource(feedbackResource);
               break;
        }
    }
    
    public static JPanel getFeedbackPanel(){
       JPanel feedbackPanel = null;
       switch(feedbackType){
           case ANIMATION:
               feedbackPanel = player.mediaPanel;
               break;
           case FRACTAL:
               JPanel contentPanel = new JPanel(null);
               contentPanel.setBackground(Color.BLACK);
               contentPanel.add(FractalGenerator.fractalPanel);
               feedbackPanel = contentPanel;
               break;
           case IMAGE:
               feedbackPanel = imagePanel;
       }
       return feedbackPanel;
    }
    
    public static void playFeedback(){
         switch(feedbackType){
           case ANIMATION:
                    //Set the player on the experiment frame
                    //player.loadVideoAsResource(getResource("/resources/animations/" + player.getCurrentVideoName()));
                    //frame.setContentPane(player.mediaPanel);
                    //frame.validate();
                    player.playVideo();  //Blocking call to ensure video is played through before continuing
                    //frame.remove(player.mediaPanel);
                    break;
             case FRACTAL:
                    //JPanel fractalPanel = FractalGenerator.fractalPanel;
                    //JPanel contentPanel = new JPanel(null);
                    //contentPanel.setBackground(Color.BLACK);
                    //contentPanel.add(fractalPanel);
                    //frame.setContentPane(contentPanel);
                    //frame.validate();
                    fractalGenerator.playTransition();
                    //frame.remove(contentPanel);
                    //contentPanel.remove(fractalPanel);
                    //frame.validate();
                    break;
             case IMAGE:
                    //frame.setContentPane(imagePanel);
                    //frame.validate();
                    Experiment.delay(imagePresentTime);
                    //frame.remove(imagePanel);
           }
    }

    public static void freeFeedback(){
        switch(feedbackType){
            case ANIMATION:
                player.freeVideo();  //Frees memory used in video playback
        }
        
        // Reset to default value.
         feedbackType = null;
    }

    public static String getFeedbackType(){
        return feedbackType.toString();
    }

}
