package ccpl.lib.feedback;


import ccpl.lib.RandomIntGenerator;

/**
 *
 * @author Kyle
 */
public class FeedbackController {

    private int frequency, count;
    private boolean isDetermined;
    private RandomIntGenerator randFeedback;

    public FeedbackController(boolean determined, int freq){
        isDetermined = determined;
        frequency = freq;
        count = 0;
        randFeedback = new RandomIntGenerator(1, freq);
    }

    public void incCount(){
        ++count;
    }

    public boolean feedbackStatus(){
        boolean status;
        if(isDetermined){
            if(count % frequency == 0){
                count = 0;
                status = true;
            }else
                status = false;
        }else{
            if(randFeedback.draw() == 1)
                status = true;
            else
                status = false;
        }
        return status;
    }
}
