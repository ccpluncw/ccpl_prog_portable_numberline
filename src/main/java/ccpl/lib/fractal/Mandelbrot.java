package ccpl.lib.fractal;

import java.awt.geom.Rectangle2D.Float;

/**
 *
 * @author Kyle
 */
public class Mandelbrot extends Fractal {

    private float shapeX, shapeY;
    private int boundsThreshold;

    public Mandelbrot(){
        super();
    }
    
    public Mandelbrot(int width, int height){
        this(width, height, new Float(-2.0f, -1.2f, 3.2f, 2.4f), 0.0f, 0.0f, 4);
    }

    public Mandelbrot(int width, int height, Float loc, float x, float y, int bounds){
        super(width, height, loc);
        shapeX = x;
        shapeY = y;
        boundsThreshold = bounds;
        this.generateImage();
    }

    @Override
    public byte[] generatePixelData(int w, int h, Float loc) {
        float xmin = loc.x;
        float ymin = loc.y;
        float xmax = loc.x+loc.width;
        float ymax = loc.y+loc.height;

        byte[] pixels = new byte[w * h];
        int pIx = 0;
        float[] p = new float[w];
        float q = ymin;
        float dp = (xmax-xmin)/w;
        float dq = (ymax-ymin)/h;

        p[0] = xmin;
        for (int i=1; i<w; i++) {
            p[i] = p[i-1] + dp;
        }

        for (int r=0; r<h; ++r) {
            for (int c=0; c<w; ++c) {
                int color = 1;
                float x = shapeX;
                float y = shapeY;
                float xsqr = 0.0f;
                float ysqr = 0.0f;
                do {
                    xsqr = x*x;
                    ysqr = y*y;
                    y = 2*x*y + q;
                    x = xsqr - ysqr + p[c];
                    ++color;
                } while (color < 200 && (xsqr + ysqr) < boundsThreshold);
                pixels[pIx++] = (byte)(color % 16);
            }
            q += dq;
        }
        return pixels;
    }
    
  
}
