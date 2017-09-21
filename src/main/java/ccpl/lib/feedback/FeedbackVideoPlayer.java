package ccpl.lib.feedback;

import ccpl.lib.DrawExpFrame;
import ccpl.lib.RandomIntGenerator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.NoDataSourceException;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.control.FormatControl;
import javax.media.format.VideoFormat;
import javax.swing.JPanel;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.media.MediaLocator;
import javax.media.protocol.DataSource;

public class FeedbackVideoPlayer{
    private String currentVideoName;
    private static ArrayList<String> videos;
    private static String videoDir;
    private static RandomIntGenerator randAnimation;
    public MediaPanel mediaPanel;
    private FeedbackController controller;

    public FeedbackVideoPlayer(String videoLoc, boolean determined, int frequency){
        mediaPanel = null;
        videoDir = videoLoc;
        controller = new FeedbackController(determined, frequency);
        getAnimations(videoLoc);
        randAnimation = new RandomIntGenerator(0, (videos.size()-1));
        Manager.setHint( Manager.LIGHTWEIGHT_RENDERER, true );
        currentVideoName = "-";
    }

    public FeedbackVideoPlayer(InputStream animationListStream, boolean determined, int frequency){
        mediaPanel = null;
        controller = new FeedbackController(determined, frequency);
        getAnimationsAsResource(animationListStream);
        randAnimation = new RandomIntGenerator(0, (videos.size()-1));
        Manager.setHint( Manager.LIGHTWEIGHT_RENDERER, true );
        currentVideoName = "-";
    }


    private void getAnimations(String animations){
        FeedbackVideoPlayer.videos = new ArrayList<String>();
        String videoList[];
        File dir = new File(animations);
        if(dir.exists()){
           videoList = dir.list();
           for(int i=0;i<videoList.length;++i)
               FeedbackVideoPlayer.videos.add(videoList[i]);
        }
    }

    public String getCurrentVideoName(){
       return currentVideoName;
    }

    private void getAnimationsAsResource(InputStream videoListStream){
        FeedbackVideoPlayer.videos = new ArrayList<String>();
        String animation;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(videoListStream));
            int i = 0;
            if(in != null){
                while((animation = in.readLine()) != null) {
                    FeedbackVideoPlayer.videos.add(i, animation);
                    ++i;
                }
                in.close();
            }else
                System.err.println("Cannot read video animation list");
        } catch (IOException ex) {
            Logger.getLogger(FeedbackVideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean nextVideo(){
        mediaPanel = null;
        controller.incCount();
        boolean playVideo = controller.feedbackStatus();
        if(playVideo == true){
           int idx = randAnimation.draw();
           currentVideoName =  videos.get(idx);
           File f = new File(videoDir, videos.get(idx));
            try {
                mediaPanel = new MediaPanel(f.toURI().toURL());
            } catch (MalformedURLException ex) {
                Logger.getLogger(FeedbackVideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return playVideo;
    }

    public boolean nextVideoAsResource(){
        mediaPanel = null;
        controller.incCount();
        boolean playVideo = controller.feedbackStatus();
        if(playVideo == true){
           int idx = randAnimation.draw();
           currentVideoName =  videos.get(idx);
        }
        return playVideo;
    }

    public void loadVideoAsResource(URL videoResource){
        mediaPanel = new MediaPanel(videoResource);
    }

    public void loadRandomVideo(){
         int idx = randAnimation.draw();
         currentVideoName =  videos.get(idx);
         File f = new File(videoDir, videos.get(idx));
         try {
             mediaPanel = new MediaPanel(f.toURI().toURL());
         } catch (MalformedURLException ex) {
             Logger.getLogger(FeedbackVideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
         }
    }

    public void getRandomVideoAsResource(){
         int idx = randAnimation.draw();
         currentVideoName =  videos.get(idx);
    }

    public void playVideo(){
        try {
            mediaPanel.play();
        } catch (InterruptedException ex) {
            Logger.getLogger(FeedbackVideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void freeVideo() {
        mediaPanel.free();
        mediaPanel = null;
    }
    
    public static class MediaPanel extends JPanel{
       private Player mediaPlayer;
       public MediaPanel( URL mediaURL ){
          setLayout( null );
          setBackground(Color.BLACK);
          // Use lightweight components for Swing compatibility
          Manager.setHint( Manager.LIGHTWEIGHT_RENDERER, true );

          try{
             // create a player to play the media specified in the URL
             MediaLocator ml = new MediaLocator(mediaURL);
             DataSource ds = null;
             if (mediaURL.getProtocol().equals("jar")){
                 ds = new JarEntryDataSource (ml);
             }else {
                try {
                    ds = Manager.createDataSource(ml);
                } catch (NoDataSourceException ex) {
                    Logger.getLogger(FeedbackVideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
             }
             mediaPlayer = Manager.createRealizedPlayer(ds);
             // get the components for the video and the playback controls
             Component video = mediaPlayer.getVisualComponent();
             //Component controls = mediaPlayer.getControlPanelComponent();
             if( video != null ){
                add( video, BorderLayout.CENTER ); // add video component

                FormatControl formatControl = (FormatControl)mediaPlayer.getControl ("javax.media.control.FormatControl");
                VideoFormat videoFormat = (VideoFormat) formatControl.getFormat();

                video.setLocation(((DrawExpFrame.getScreenWidth()/2)-(videoFormat.getSize().width/2)), ((DrawExpFrame.getScreenHeight()/2)-(videoFormat.getSize().height/2)));
                video.setSize(videoFormat.getSize().width,videoFormat.getSize().height);
                add(video); // add video component
                mediaPlayer.prefetch();
             }else
                 System.err.println("Video not loading");

          }catch ( NoPlayerException NoPlayerException){
             System.err.println( "No media player found" );
          }catch ( CannotRealizeException CannotRealizeException ){
             System.err.println( "Could not realize media player" );
          }catch ( IOException IOException ){
             System.err.println( "Error reading from the source" );
          }
       } // end MediaPanel constructor

       public void play() throws InterruptedException{
           if(mediaPlayer != null){
               
                while(mediaPlayer.getState() != mediaPlayer.Prefetched)
                   Thread.sleep(100);

                mediaPlayer.start(); // start playing the media clip
                Thread.sleep((long)(mediaPlayer.getDuration().getSeconds()*1000));
                mediaPlayer.stop();
                removeAll(); //removes video component from this panel
           }
       }

       public void free(){
           if(mediaPlayer != null){
               mediaPlayer.stop();
               if(mediaPlayer.getState() != mediaPlayer.Started)
                   mediaPlayer.deallocate();
               mediaPlayer.close();
               mediaPlayer = null;
           }
       }
    } // end class MediaPanel



} //end VideoFeedbackPlayer class
