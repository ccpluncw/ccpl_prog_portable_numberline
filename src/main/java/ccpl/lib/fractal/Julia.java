package ccpl.lib.fractal;

import java.awt.geom.Rectangle2D.Float;

/**
 *
 * @author Kyle
 */
public class Julia extends Fractal{

    //Variants: Threshold, iterations, complex num
    private int iterations; // The number of times that the algorithm recurses.
    private float blowup;
    private ComplexNumber cn;
        
    public Julia(){
        super();
    }

    public Julia(int width, int height){
        super(width, height, new Float(-2.0f, -4.0f, 6.5f, 6.5f));
        cn = new ComplexNumber(-1.123, .745);
        iterations = 50;
        blowup = 5.1f;
        this.generateImage();
    }
    
    public Julia(int width, int height, Float loc, double aCn, double bCn, int iter, float blowup){
        super(width, height, loc);
        cn = new ComplexNumber(aCn, bCn);
        iterations = iter;
        this.blowup = blowup;
        this.generateImage();
    }

    @Override
    public byte[] generatePixelData(int w, int h, Float loc) {
        // The bounds of the Complex Plane to graph
        float xmin = loc.x;
        float ymin = loc.y;
        float xmax = loc.x+loc.width;
        float ymax = loc.y+loc.height;

        byte[] pixels = new byte[w * h];
        int pIx = 0;

        for (int i=0; i<h; ++i) {
            for (int j=0; j<w; ++j) {
                int color = 0;
                boolean foundColor = false;

                double a = (double)i*(xmax-xmin)/(double)w + xmin;
		double b = (double)j*(ymax-ymin)/(double)h + ymin;

                ComplexNumber c = new ComplexNumber(a,b);

                for(int k=0;k<iterations;++k){
                    // The basic Julia Set Algorithm.
                    c = c.square().add(this.cn);

                    if(!foundColor){
                        if(c.normalized() > blowup)
                            foundColor = true;
                        else
                            ++color;
                    }
                }

                pixels[pIx++] = (byte)(color % 16);
            }
        }
        return pixels;
    }
}
