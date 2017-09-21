package ccpl.lib;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class can be used to play audio files
 * they should be .wav
 * 
 * @author programmer
 */
public class AudioClip extends Thread {

    private java.net.URL filename;
    private long startTime;
    private int audioLength;
    private Clip auclip;
    private boolean stopClip;
    
    private static final float SAMPLE_RATE = 2000f;


    public AudioClip(java.net.URL wavfile) {
        filename = wavfile;
        stopClip = false;
        audioLength = 0;
        startTime = 0;
    }
    
    /**
     * Plays a tone 
     * you can specify hertz and duration of the tone
     * 
     * @param hz
     *  frequency in hertz
     * @param ms
     *  duration in milliseconds
     * @throws LineUnavailableException 
     */
    public static void tone(int hz, int ms) throws LineUnavailableException{
        tone(hz, ms, 1.0);
    }
    
    /**
     * Plays a tone with adjustable volume
     * @param hz
     *  frequency in hertz
     * @param ms
     *  duration in milliseconds
     * @param vol
     *  volume factor (1 is normal.... > 1 loud......< 1 soft)
     * @throws LineUnavailableException 
     */
    public static void tone(int hz, int ms, double vol) throws LineUnavailableException{
        byte[] buf = new byte[1];
        AudioFormat af = 
            new AudioFormat(
                SAMPLE_RATE, // sampleRate
                8,           // sampleSizeInBits
                1,           // channels
                true,        // signed
                false);      // bigEndian
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        for (int i=0; i < ms*8; i++) {
            double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
            buf[0] = (byte)(Math.sin(angle) * 127.0 * vol);
            sdl.write(buf,0,1);
        }
        sdl.drain();
        sdl.stop();
        sdl.close();
    }
    
    /**
     * Plays a beep 
     * 
     * @param frequency
     *  frequency of the beep
     * @param duration
     *  duration of the beep (milliseconds)
     * @throws Exception 
     */
    public static void beep(double frequency, int duration) throws Exception{
      
        int nChannel = 1;         // number of channel : 1 or 2

        // samples per second
        float sampleRate = 16000;  // valid:8000,11025,16000,22050,44100
        int nBit = 16;             // 8 bit or 16 bit sample

        int bytesPerSample = nChannel*nBit/8;

        double durationInSecond = (double) duration/1000.0;
        int bufferSize = (int) (nChannel*sampleRate*durationInSecond*bytesPerSample);
        byte[] audioData = new byte[bufferSize];

        // "type cast" to ShortBuffer
        java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.wrap(audioData);
        java.nio.ShortBuffer shortBuffer = byteBuffer.asShortBuffer();


        int sampleLength = audioData.length/bytesPerSample;

        // generate the sine wave
        double volume = 8192;   // 0-32767
        double PI = Math.PI;
        for(int i = 0; i < sampleLength; i++){
            double time = i/sampleRate;
            double freq = frequency;
            double angle = 2*PI*freq*time;
            double sinValue = Math.sin(angle);

            double fade=1;
            int decay=sampleLength*1/3;  // start fade out at 2/3 of the total time
            if (i>=sampleLength-1-decay) fade=(double)(sampleLength-1-i)/decay;

            short amplitude = (short) (volume*fade*sinValue);

            for (int c=0;c<nChannel;c++)
            {
                shortBuffer.put(amplitude);
            }

        }//end generating sound wave sample


        boolean isSigned=true;
        boolean isBigEndian=true;

        // Define audio format
        AudioFormat audioFormat =
        new AudioFormat(sampleRate, nBit, nChannel, isSigned,isBigEndian);


        javax.sound.sampled.DataLine.Info dataLineInfo =
        new javax.sound.sampled.DataLine.Info(
            SourceDataLine.class, audioFormat);

        // get the SourceDataLine object
        SourceDataLine sourceDataLine =
        (SourceDataLine)
        AudioSystem.getLine(dataLineInfo);

        sourceDataLine.open(audioFormat);
        sourceDataLine.start();

        // actually play the sound
        sourceDataLine.write(audioData,0,audioData.length);

        // "flush",  wait until the sound is completed
        sourceDataLine.drain();
  
  }


    public int getAudioLength(){
        return audioLength;
    }

    /**
     * Stops the clip
     * should be automatically called in run method
     */
    public void stopClip(){
        if(auclip != null){
            auclip.stop();
        }
        stopClip = true;
    }

    public void close(){
        auclip.close();
    }
    
    public long getStartTime(){
        return startTime;
    }

    /**
     * run runs the audio file for the audio clip object
     * overrides parent class Thread's run method.
     * 
     * Therefore AudioClip a.start() will invoke this method
     * 
     * Java Thread tutorials are available through oracle
     */
    @Override
    public void run() {

        /*File soundFile = new File(filename);
        if (!soundFile.exists()) {
            System.err.println("Wave file not found: " + filename);
            return;
        }*/

        AudioInputStream audioInputStream = null;

        try {
            audioInputStream = AudioSystem.getAudioInputStream(filename);
        } catch (UnsupportedAudioFileException e1) {
            e1.printStackTrace();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        //AudioFormat audioFormat = audioInputStream.getFormat();
        //Line.Info clipInfo = new Line.Info(Clip.class, audioFormat);
        Line.Info clipInfo = new Line.Info(Clip.class);
        try {
            auclip = (Clip) AudioSystem.getLine(clipInfo);
            auclip.open(audioInputStream);
        }catch (IOException ex) {
            Logger.getLogger(AudioClip.class.getName()).log(Level.SEVERE, null, ex);
        }catch (LineUnavailableException ex) {
            Logger.getLogger(AudioClip.class.getName()).log(Level.SEVERE, null, ex);
        }

        audioLength = (int) (auclip.getMicrosecondLength()/1000);

        startTime = new Date().getTime();

        if (auclip.isControlSupported(FloatControl.Type.VOLUME)) {
            FloatControl vol = (FloatControl) auclip.getControl(FloatControl.Type.VOLUME);
            vol.setValue(1.0f);
        }

        if(!stopClip){
            auclip.start();

            //Make sure clip is active before proceeding to next loop
            while(!auclip.isActive()){
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AudioClip.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            while(auclip.isActive() || auclip.isRunning()){
                try {
                Thread.sleep(250);
                } catch (InterruptedException ex) {
                Logger.getLogger(AudioClip.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(!stopClip)
                auclip.stop();
        }
            auclip.close();
            auclip = null;
    }
    
    //used to test tone method
    public static void main(String[] args) throws Exception{
//        AudioClip.tone(100, 20, 1);
//        Thread.sleep(1000);
//        AudioClip.tone(100, 20, 1);
//        Thread.sleep(1000);
//        AudioClip.tone(100, 20, 1);
//        Thread.sleep(1000);
//        AudioClip.tone(100, 20, 1);
//        Thread.sleep(1000);
          AudioClip.beep(120, 30);
          Thread.sleep(1000);
          AudioClip.beep(120, 30);
          Thread.sleep(1000);
          AudioClip.beep(120, 30);
          Thread.sleep(1000);
          AudioClip.beep(120, 30);
          Thread.sleep(1000);
    }
    
}

