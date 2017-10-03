package ccpl.lib;

import java.io.IOException;
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
    private Clip auclip;
    private boolean stopClip;
    


    public AudioClip(java.net.URL wavfile) {
        filename = wavfile;
        stopClip = false;
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
            double angle = 2*PI* frequency *time;
            double sinValue = Math.sin(angle);

            double fade=1;
            int decay= sampleLength /3;  // start fade out at 2/3 of the total time
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

        AudioInputStream audioInputStream;

        try {
            audioInputStream = AudioSystem.getAudioInputStream(filename);
        } catch (UnsupportedAudioFileException | IOException e1) {
            e1.printStackTrace();
            return;
        }

        Line.Info clipInfo = new Line.Info(Clip.class);
        try {
            auclip = (Clip) AudioSystem.getLine(clipInfo);
            auclip.open(audioInputStream);
        } catch (IOException | LineUnavailableException ex) {
            Logger.getLogger(AudioClip.class.getName()).log(Level.SEVERE, null, ex);
        }

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
}

