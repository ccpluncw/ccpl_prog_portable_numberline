package ccpl.lib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
	
/*************
 * ImageProcessingPanel is a panel that provides functions to load images,
 * rotate images and flip images.
 ************/
public class ImageProcessingPanel extends JPanel {

	public ImageProcessingPanel (Color backColor) {
		setBackground (backColor);
                drawRandom = false;
                currX = 0;
                currY = 0;
                currentImageName = "-";
                drawToFit = false;
	}

	public ImageProcessingPanel () {
		this(Color.BLACK);
	}

        public ImageProcessingPanel(String imageDirPath, boolean useRandPos){
            this(imageDirPath);
            drawRandom = useRandPos;
        }

        public ImageProcessingPanel(InputStream imageListStream, boolean useRandPos){
            this(Color.BLACK);
            drawRandom = useRandPos;
            ImageProcessingPanel.images = new ArrayList<String>();
            String imageName;
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(imageListStream));
                int i = 0;
                if(in != null){
                    while((imageName = in.readLine()) != null) {
                        ImageProcessingPanel.images.add(i, imageName);
                        ++i;
                    }
                    in.close();
                }else
                    System.err.println("Cannot read video animation list");
            } catch (IOException ex) {
                Logger.getLogger(ImageProcessingPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            randImageIdx = new RandomIntGenerator(0, images.size()-1);
            
        }

        public ImageProcessingPanel(String imageDirPath){
            this(Color.BLACK);
            imagePath = imageDirPath;
            ImageProcessingPanel.images = new ArrayList<String>();
            String[] imageList = null;
            File dir = new File(imageDirPath);
            if(dir.exists()){
               imageList = dir.list();
               for(int i=0;i<imageList.length;++i)
                    ImageProcessingPanel.images.add(imageList[i]);
            }
            randImageIdx = new RandomIntGenerator(0, imageList.length-1);
        }


    @Override
      public void paint(Graphics g){
        if(!drawToFit){
          if(!drawRandom)
            drawCentered(g);
          else
            drawRandom(g);
        }else
            drawToFit(g);
      }
  
    public void loadImage (String imageFile) {
                try {
                    image = ImageIO.read(new File(imageFile));
                } catch (IOException e) {
                }
                if(drawRandom){
                    setRandomPos();
                }
    }
    
    public void setImage(BufferedImage bi){
        if(bi != null)
            image = bi;
        else
            System.err.println(image.toString());
    }

    public String loadImage() {
                String imageFile = null;
                try {
                    int idx = randImageIdx.draw();
                    imageFile = images.get(idx);
                    image = ImageIO.read(new File(imagePath+File.separator+imageFile));
                } catch (IOException e) {
                    System.err.println(image.toString());
                }
                currentImageName = imageFile;
                if(drawRandom)
                    setRandomPos();
                return imageFile;
    }

    public void getRandomImageAsResource(){
          int idx = randImageIdx.draw();
          currentImageName = images.get(idx);
    }
    java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
    Dimension screen = tk.getScreenSize();
    public void loadImageAsResource(URL resourceImage){
        try {
            image = ImageIO.read(resourceImage);
            this.setSize(new Dimension(image.getWidth(), image.getHeight()));
        } catch (IOException ex) {
            Logger.getLogger(ImageProcessingPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(drawRandom)
            setRandomPos();
        
    }
    
    public void loadImageAsResourceRestricted(URL resourceImage, int maxX, int maxY){
                try {
            image = ImageIO.read(resourceImage);
            double scaleX = 0.75, scaleY = 0.75;
            System.out.println(image.getWidth());
            System.out.println(image.getHeight());
            if(image.getWidth() > maxX || image.getHeight() > maxY){
                do{
                    //scale image by .75, .5, .25 to get it to fit within the screen
                    scaleImage(scaleX,scaleY);
                    scaleX -= .25;
                    scaleY -= .25;
                    System.out.println("did it");
                } while((image.getWidth() > maxX || image.getHeight() > maxY) && scaleX != 0);
                
            }
            System.out.println(image.getWidth());
            System.out.println(image.getHeight());
            this.setSize(new Dimension(image.getWidth(), image.getHeight()));
        } catch (IOException ex) {
            Logger.getLogger(ImageProcessingPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(drawRandom)
            setRandomPos();
    }
    
    public Dimension getImageDimension(){
        return new Dimension(image.getWidth(), image.getHeight());
    }

    public void loadImageAsResource(URL resourceImage, int panelWidth, int panelHeight){
        try {
            image = ImageIO.read(resourceImage);
            drawToFit = true;
            this.setSize(panelWidth, panelHeight);
        } catch (IOException e) {
        }
        if(drawRandom)
            setRandomPos();
    }
    
    public boolean scaleImage(double fWidth, double fHeight){
        boolean flag = false;
        
        image = scale(image, image.getType(), (int) (image.getWidth() * fWidth), (int) (image.getHeight() * fHeight), fWidth, fHeight);
        this.setSize((int)(getWidth()*fWidth), (int)(getHeight()*fHeight));
        
        flag = true;
        return flag;
    }
    
    public static BufferedImage scale(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth, double fHeight) {
    BufferedImage dbi = null;
    if(sbi != null) {
        dbi = new BufferedImage(dWidth, dHeight, imageType);
        Graphics2D g = dbi.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
        g.drawRenderedImage(sbi, at);
    }
    return dbi;
}
  
  
  public void drawCentered (Graphics g) {
    if (image != null) {
      Dimension dPanel = getSize();
      g.drawImage(image,(dPanel.width - image.getWidth(null))/2, (dPanel.height - image.getHeight(null))/2,null);
//      System.out.println(dPanel.getSize() + " " + image.getWidth() + " " + image.getHeight() );
    }
  }
  

  public void drawToFit(Graphics g){
    if (image != null) {
      Dimension dPanel = getSize();
      //System.out.println(dPanel.width+", "+ dPanel.height + ", -- "+ image.getWidth(null) + ", " +  image.getHeight(null));
      g.drawImage(image,
                        0, 0, dPanel.width, dPanel.height,
                        0, 0, image.getWidth(null), image.getHeight(null), null);

    }
  }

  public String getCurrentImageName(){
      return currentImageName;
  }

  private void setRandomPos(){
      randCoord = new RandomIntGenerator(0, (DrawExpFrame.getScreenWidth()-image.getWidth()));
      currX = randCoord.draw();
      randCoord.setRange(0, (DrawExpFrame.getScreenHeight()-image.getHeight()));
      currY = randCoord.draw();
  }

  public void drawRandom (Graphics g){
    if (image != null) {
        g.drawImage (image, currX, currY, null);
    }
  }
      
  private void filter (BufferedImageOp op) {
    BufferedImage filteredImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
    op.filter (image, filteredImage);
    image = filteredImage;
    repaint ();
  }
   
  public void rotate (double angle) {
    AffineTransform transform = AffineTransform.getRotateInstance (Math.toRadians(angle),image.getWidth(null)/2,  image.getHeight(null)/2);
    AffineTransformOp op = new AffineTransformOp (transform, AffineTransformOp.TYPE_BILINEAR);
		filter (op);
	}

  public void flip () {
    AffineTransform transform = new AffineTransform (-1,0,0,1,image.getWidth (null),0);
    AffineTransformOp op = new AffineTransformOp (transform, AffineTransformOp.TYPE_BILINEAR);
		filter (op);
	}

    private BufferedImage image;
    private boolean drawRandom, drawToFit;
    private static RandomIntGenerator randCoord, randImageIdx;
    private int currX, currY;
    private String imagePath;
    private static ArrayList<String> images;
    private String currentImageName;
}

	