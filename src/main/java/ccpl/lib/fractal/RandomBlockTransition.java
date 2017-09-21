package ccpl.lib.fractal;

import ccpl.lib.RandomIntGenerator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import javax.swing.JPanel;
import javax.swing.Timer;


public class RandomBlockTransition extends FractalTransition implements ActionListener {

    private int dimension;
    private SubImagePanel[][] subimages;
    private volatile int rowIdx;
    private volatile int colIdx;
    private volatile boolean transitionedIn;

    public RandomBlockTransition(Fractal f, int time, int presentTime){
        super(f, time, presentTime);
        //dimension = blockDim;
        //updateRate = TRANSITION_TIME/(dimension*dimension);
        dimension = (int)Math.sqrt((double)TRANSITION_TIME/REFRESH_RATE);
        rowIdx = 0;
        colIdx = 0;
        transitionedIn = false;
    }

    private SubImagePanel[][] getSubimages(){
        SubImagePanel[][] fractSubimages = new SubImagePanel[dimension][dimension];
        int subImagePixelW = fractal.getWidth()/dimension;
        int subImagePixelH = fractal.getHeight()/dimension;

        Image fractImg = fractal.image;
        for(int row=0;row<dimension;++row){
            for(int col=0;col<dimension;++col){
                int rowoffset = subImagePixelW*row;
                int coloffset = subImagePixelH*col;
                fractSubimages[row][col] = new SubImagePanel(fractImg, rowoffset, coloffset, subImagePixelW, subImagePixelH);
            }
        }
        shuffle(fractSubimages);
        return fractSubimages;
    }
     
     private static void shuffle(SubImagePanel[][] subimages){
        SubImagePanel temp = null;
        RandomIntGenerator randomGen = new RandomIntGenerator(0, (subimages.length-1));
        for(int r=0;r<subimages.length;++r){
            for(int c=0;c<subimages.length;++c){
                int randR = randomGen.draw();
                int randC = randomGen.draw();
                //swap w/ random subimage
                temp = subimages[r][c];
                subimages[r][c] = subimages[randR][randC];
                subimages[randR][randC] = temp;
            }
        }
    }
     
     
    private void addCurrentSubImage(){
        transitionPanel.add(subimages[rowIdx][colIdx]);
        transitionPanel.revalidate();
        transitionPanel.repaint();
    }
    
    private void removeCurrentSubImage(){
        subimages[rowIdx][colIdx].clear();
        transitionPanel.revalidate();
        transitionPanel.repaint();
    }

    @Override
    public void play(){
        updateTimer = new Timer(REFRESH_RATE, this);
        subimages = getSubimages();
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

    public void actionPerformed(ActionEvent e) {
        if(!updateTimer.isRunning()){
            updateTimer.stop();
            return;
        }

        if(!transitionedIn){
            while(rowIdx < dimension){
                while(colIdx < dimension){
                    addCurrentSubImage();
                    ++colIdx;
                    break;
                }
                if(colIdx == (dimension)){
                    ++rowIdx;
                    colIdx = 0;
                }
                break;
            }
        }else{
             while(rowIdx < dimension){
                while(colIdx < dimension){
                    removeCurrentSubImage();
                    ++colIdx;
                    break;
                }
                if(colIdx == (dimension)){
                    ++rowIdx;
                    colIdx = 0;
                }
                break;
            }
        }

        if(rowIdx == dimension){
            rowIdx = 0;
            colIdx = 0;
            transitionedIn = true;
            updateTimer.stop();
        }
    }

    private class SubImagePanel extends JPanel {
        private Image image;

        public SubImagePanel(Image img, int x, int y, int width, int height) {
            super(null);
            setSubimage(img, x, y, width, height);
            setBounds(x, y, width, height);
        }
        
        private void setSubimage(Image img, int xLoc, int yLoc, int width, int height){
            image = createImage(new FilteredImageSource(img.getSource(), new CropImageFilter(xLoc, yLoc, width, height)));
        }

        private void clear(){
            image.flush();
            image = null;
        }

        @Override
        public void paintComponent(Graphics g){
           super.paintComponent(g);
           if(image != null)
                g.drawImage(image,0,0,null);
           else{
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
           }
        }
        
    }
}

