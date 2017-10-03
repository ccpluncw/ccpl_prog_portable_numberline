package ccpl.lib.feedback;

import ccpl.lib.Experiment;

public class Feedback {
    private enum FEEDBACKTYPE {ANIMATION, IMAGE, FRACTAL}
    private static FEEDBACKTYPE feedbackType = null;

  private Feedback(){ }


  public static void playFeedback(){
         switch (feedbackType){
           case ANIMATION:
                    break;
             case FRACTAL:
                    break;
             case IMAGE:
               int imagePresentTime = 2000;
               Experiment.delay(imagePresentTime);
           }
    }

    public static void freeFeedback(){
        // Reset to default value.
         feedbackType = null;
    }
}
