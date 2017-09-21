package ccpl.lib;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class RTPressure {

    private Integer[] currRTWin, prevRTWin;
    private int writePtr, currentRT, lastRT, numIncorrect, totalTrialCnt, currDeadline, prevDeadline = -1;
    private double quantileThreshold, errorRate, actualErrorRate; //actualErrorRate for trials up to n-1
    private boolean rtBeep;
    private boolean useInit = false, headphoneFlag = false;

    public RTPressure(int numTrials, double qtile, double errRate){
        currRTWin = new Integer[numTrials]; //allocate memory for trial RTs
        prevRTWin = new Integer[numTrials]; //stores previous state of trial RTs
        quantileThreshold = qtile;
        errorRate = errRate;
        writePtr=-1;
        lastRT = -1;
        numIncorrect=0;
        totalTrialCnt=0;
        currentRT = -1;
        rtBeep = false;
    }
    
    public RTPressure(int numTrials, double qtile, double errRate, int initDeadline){
        currRTWin = new Integer[numTrials]; //allocate memory for trial RTs
        prevRTWin = new Integer[numTrials]; //stores previous state of trial RTs
        quantileThreshold = qtile;
        errorRate = errRate;
        writePtr=-1;
        lastRT = -1;
        numIncorrect=0;
        totalTrialCnt=0;
        currentRT = -1;
        rtBeep = false;
        currDeadline = initDeadline;
        useInit = true;
    }
    
    public void showHeadphoneDialog(Response r, JFrame parent){
        r.displayNotificationFrame(parent, "If you are not already wearing the headphones, please put them on now.");
        headphoneFlag = true;
    }
    
    public void showHeadphoneDialog(){
        JOptionPane.showMessageDialog(null, "If you are not already wearing the headphones, please put them on now.");
        headphoneFlag = true;
    }
    
    public void overrideHeadphoneFlag(boolean flag) {
	    headphoneFlag = flag;
    }
    
    public void add(int rtVal, boolean isTrialCorrect){
        if(headphoneFlag){
            if(currentRT != -1){
                writePtr = ++writePtr % currRTWin.length;

                if(currRTWin[0] != null){ //not empty
                    if((writePtr-1)>=0)
                        lastRT = currRTWin[writePtr-1];  //save last value in window
                    else
                        lastRT = currRTWin[(writePtr-1+currRTWin.length)];
                }

                currRTWin[writePtr] = currentRT; //overwrite last value in window

                //System.out.println("LastRT: " + lastRT + "  WritePtr: " + writePtr);

                if(lastRT > -1){ //LastRT was set
                    if((writePtr-1)>=0)
                        prevRTWin[(writePtr-1)] = lastRT;
                    else
                        prevRTWin[(writePtr-1+currRTWin.length)] = lastRT;
                }

            }
            currentRT = rtVal;

            totalTrialCnt++;
            setErrorRate(isTrialCorrect);
        }
        else{
            JOptionPane.showMessageDialog(null, "Error. Please remind subject to put on headphones");
            System.exit(0);
        }
    }
    
    private boolean isFull(){
        if(currRTWin[currRTWin.length-1] != null && prevRTWin[prevRTWin.length-1] != null)
            return true;
        else
            return false;
    }
    
    private void setErrorRate(boolean isTrialCorrect){
        if(isTrialCorrect == false){
            numIncorrect++;
            actualErrorRate = ((double)numIncorrect)/totalTrialCnt;
        }else{
            actualErrorRate = ((double)numIncorrect)/totalTrialCnt;
        }
        //System.out.println("AER: " + numIncorrect + "/" + totalTrialCnt + " = " + actualErrorRate);
    }

    private int calculateQuantile(boolean calcPrevious){
        int idx;
        Integer[] winCpy;
        if(!calcPrevious){
            winCpy = new Integer[currRTWin.length];
            winCpy = currRTWin.clone();
        }else{
            winCpy = new Integer[prevRTWin.length];
            winCpy = prevRTWin.clone();
        }
        Arrays.sort(winCpy); //sorts ascending - fastest to slowest
        idx = (int)(Math.ceil((winCpy.length) * quantileThreshold) - 1); //includes blocks only up to n-blocksFromN
        return winCpy[idx];
    }

    private int calculateDeadline(){
            int rtQuantileN1 = calculateQuantile(false);
            int rtQuantileN2;
            if(prevDeadline == -1) //if prevDeadline has not been initilized
                rtQuantileN2 = calculateQuantile(true);
            else
                rtQuantileN2 = prevDeadline;

            //System.out.println("QN1: " + rtQuantileN1 + " QN2: " + rtQuantileN2);

            if(actualErrorRate >= errorRate){
                if(rtQuantileN1 >  rtQuantileN2){
                    return rtQuantileN1;
                }else{
                    return rtQuantileN2;
                }
            }else{
                if(rtQuantileN1 < rtQuantileN2)
                    return rtQuantileN1;
                else
                    return rtQuantileN2;
            }
    }

    public void checkResponseTime(){
       if(isFull()){
           //System.out.println("Current RT: " + currentRT);
            if((currDeadline = calculateDeadline()) < currentRT){
                java.awt.Toolkit.getDefaultToolkit().beep();
                rtBeep = true;
            }else
                rtBeep = false;
       } else if(useInit) {
           if(currDeadline < currentRT){
               java.awt.Toolkit.getDefaultToolkit().beep();             
               rtBeep = true;
           }else {
               rtBeep = false;
           }
       }
    }
    
    public void checkResponseTime(int hz, int ms, double vol){
       if(isFull()){
           //System.out.println("Current RT: " + currentRT);
            if((currDeadline = calculateDeadline()) < currentRT){
                try{
                AudioClip.tone(hz, ms, vol); //play custom sound
                }
                catch(Exception e){e.printStackTrace(); System.exit(0);}
                rtBeep = true;
            }else
                rtBeep = false;
       }
    }
    
    public void checkResponseTime(int freq, int ms){
       if(isFull()){
           if(currDeadline != 0) {
               prevDeadline = currDeadline;
           }
           //System.out.print(prevDeadline);
           //System.out.println("Current RT: " + currentRT);
            if((currDeadline = calculateDeadline()) < currentRT){
                try{
                AudioClip.beep(freq,ms); //play custom sound
                }
                catch(Exception e){e.printStackTrace(); System.exit(0);}
                rtBeep = true;
            }else
                rtBeep = false;
       }
       else if(useInit){
           if(currDeadline < currentRT){
               try{
                   AudioClip.beep(freq,ms); //play custom sound
               }
               catch(Exception e){e.printStackTrace(); System.exit(0);}
               rtBeep = true;
           }else
               rtBeep = false;
           }
     }

    public boolean isRTBeep(){
        return rtBeep;
    }

    public int getDeadline(){
        return currDeadline;
    }
}
