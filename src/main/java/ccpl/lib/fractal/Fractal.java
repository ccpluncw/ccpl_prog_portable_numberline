package ccpl.lib.fractal;

import ccpl.lib.RandomIntGenerator;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

/**
 *
 * @author Kyle
 */
public abstract class Fractal {

    private int width, height;
    public Image image;
    private Rectangle2D.Float bounds;

    public Fractal(){
        this(200,200, new Rectangle2D.Float(-2.0f, -1.2f, 3.2f, 2.4f));
    }

    public Fractal(int fractalWidth, int fractalHeight){
        this(fractalWidth, fractalHeight, new Rectangle2D.Float(-2.0f, -1.2f, 3.2f, 2.4f));
    }

    public Fractal(int fractalWidth, int fractalHeight, Rectangle2D.Float theBounds) {
        width = fractalWidth;
        height = fractalHeight;
        bounds = theBounds;
    }
    
    protected void generateImage(){
        ColorModel colorModel = getColorModel();
        byte[] pixels = generatePixelData(width, height, bounds);

         // Create a data buffer using the byte buffer of pixel data.
        // The pixel data is not copied; the data buffer uses the byte buffer array.
        //DataBuffer dbuf = new DataBufferByte(pixels, width*height, 0);

        // Prepare a sample model that specifies a storage 4-bits of pixel data in an 8-bit data element
       // int bitMasks[] = new int[]{(byte)0xf};
        //SampleModel sampleModel = new SinglePixelPackedSampleModel(
        //    DataBuffer.TYPE_BYTE, width, height, bitMasks);

        // Create a raster using the sample model and data buffer
       //WritableRaster raster = Raster.createWritableRaster(sampleModel, dbuf, null);

        // Combine the color model and raster into a buffered image
        //image = new BufferedImage(colorModel, raster, false, null);

        image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height,colorModel, pixels, 0, width));
    }

    //public Image getImage(){
    //    return image;
    //}

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public abstract byte[] generatePixelData(int w, int h, Rectangle2D.Float loc);

    private static ColorModel getColorModel() {
        final int bytesPerComp = 48; // Generate 18 bit color model - 6 bits per component
        
        byte[] r = new byte[bytesPerComp];
        byte[] g = new byte[bytesPerComp];
        byte[] b = new byte[bytesPerComp];

        RandomIntGenerator randColorGen = new RandomIntGenerator(0, 255);
        for(int i=0;i<bytesPerComp;++i){
            r[i] = (byte)(randColorGen.draw());
            g[i] = (byte)(randColorGen.draw());
            b[i] = (byte)(randColorGen.draw());
        }
        return new IndexColorModel((int)(Math.log(bytesPerComp)/Math.log(2)), bytesPerComp, r, g, b);
    }

    protected class ComplexNumber{

        private double a, b;
        // Create a Complex Number with the given real numbers.
        public ComplexNumber(double a, double b){
            this.a = a;
            this.b = b;
        }

        // Method for squaring a ComplexNumber
        public ComplexNumber square(){
            return new ComplexNumber(this.a*this.a - this.b*this.b, 2*this.a*this.b);
        }

        // Method for adding 2 complex numbers
        public ComplexNumber add(ComplexNumber cn){
            return new ComplexNumber(this.a+cn.a, this.b+cn.b);
        }

        // Method for calculating magnitude^2 (how close the number is to infinity)
        public double magnitude(){
            return a*a+b*b;
        }

        public double normalized(){
            return Math.sqrt(magnitude());
        }
    }

}
