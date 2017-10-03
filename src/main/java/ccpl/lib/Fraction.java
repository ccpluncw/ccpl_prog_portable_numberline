package ccpl.lib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Kyle
 */
public class Fraction {
    
    private final int numerator;
    private final int denominator;

    public Fraction() {
        numerator = 0;
        denominator = 1;
    }

    public Fraction(int whole) {
        this(whole, 1);
    }

    public Fraction(int num, int denom){
        if(denom == 0){
            throw new IllegalArgumentException("Denominator can not be zero!");
        }
        numerator = num;
        denominator = denom;
    }

    public Fraction(String frac){
        String[] fraction = frac.split("/");
        numerator = Integer.parseInt(fraction[0]);
        denominator = Integer.parseInt(fraction[1]);
    }

    public int getNumerator(){
        return numerator;
    }

    public int getDenominator(){
        return denominator;
    }

    //Allows you to print a fraction to System.out directly
    @Override
    public String toString(){
        //Fraction f = reduceFract(this);
        return numerator+"/"+denominator;
    }

    public double toDouble(){
        return (double)numerator/denominator;
    }
    
    public static Fraction subtract(Fraction f1, Fraction f2){
        int num = (f1.numerator * f2.denominator) - (f2.numerator * f1.denominator);
        int denom = f1.denominator * f2.denominator;
        return new Fraction(num, denom);
    }

    public static int getCommonDenom(Fraction f1, Fraction f2){
        int gcd1;
        if(f1.denominator != f2.denominator)
            gcd1 = f1.denominator * f2.denominator;
        else
            gcd1 = f1.denominator;
        return gcd1;
    }

    public static int getCommonDenom(Fraction[] fractions){
        Fraction currFract = new Fraction();
        for (Fraction fraction : fractions) {
            currFract = new Fraction(1, getCommonDenom(currFract, fraction));
        }
        return currFract.denominator;
    }

    private static int gcd(int a, int b) {
        if (b == 0)
            return a;
        else
            return gcd(b, a % b);
    }

    public static Fraction reduceFract(Fraction f){
        int gcd = gcd(f.numerator, f.denominator);
        int num = f.numerator/gcd;
        int denom =  f.denominator/gcd;
        return new Fraction(num, denom);
    }

    public FractionPanel getFractionPanel(Font aFont, Color aColor){
        return new FractionPanel(aFont, aColor);
    }

    public class FractionPanel extends JPanel {
        private final int padding = 4;
        private final int num, denom;
        private final JLabel numLabel, denomLabel;
        private final Color color;
        private BasicStroke stroke = new BasicStroke(1);

        public FractionPanel(Font font, Color fractionColor){
            setLayout(null);
            num = numerator;
            denom = denominator;
            numLabel = new JLabel(Integer.toString(num));
            denomLabel = new JLabel(Integer.toString(denom));

            
            if(font != null){
                numLabel.setFont(font);
                denomLabel.setFont(font);
            }

            this.color = fractionColor;

            numLabel.setForeground(fractionColor);
            denomLabel.setForeground(fractionColor);

            numLabel.setSize(numLabel.getPreferredSize());
            denomLabel.setSize(denomLabel.getPreferredSize());
            
            int numLabelW = numLabel.getWidth();
            int denomLabelW = denomLabel.getWidth();
            int panelWidth = (numLabelW > denomLabelW) ? numLabelW : denomLabelW;
            int numXLoc = (numLabelW >= denomLabelW) ? 0 : (panelWidth/2)-(numLabelW/2);
            int denomXLoc = (numLabelW <= denomLabelW) ? 0 : (panelWidth/2)-(denomLabelW/2);

            numLabel.setLocation(numXLoc,0);
            denomLabel.setLocation(denomXLoc,numLabel.getHeight()+padding);

            setSize(panelWidth, numLabel.getHeight()+denomLabel.getHeight()+padding);
            setBackground(Color.BLACK);
            
            add(numLabel);
            add(denomLabel);
                    
            this.repaint();
        }

        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setColor(color);
            g2.setStroke(stroke);
            g2.drawLine(0, numLabel.getHeight()+(padding/2), getWidth(), numLabel.getHeight()+(padding/2));
        }

    }

    public static void main(String[] argv) {

        /* Test all three contructors and toString. */
        Fraction f0 = new Fraction();
        Fraction f1 = new Fraction(3);
        Fraction f2 = new Fraction(12, 20);
        Fraction f3 = new Fraction(6, 63);
        Fraction f4 = new Fraction(3, 5);
        Fraction f5 = new Fraction(5, 12);
        Fraction[] fractArr = new Fraction[3];
        fractArr[0] = f1;
        fractArr[1] = f2;
        fractArr[2] = f3;

        System.out.println("\nTesting constructors (and toString):");
        System.out.println("The fraction f0 is " + f0.toString());
        System.out.println("The fraction f1 is " + f1); // toString is implicit
        System.out.println("The fraction f2 is " + f2);
        System.out.println("The fraction f3 is " + f3);

        System.out.println("\nTesting gcd:");
        System.out.println("The gcd of 2 and 10 is: " + gcd(2, 10));
        System.out.println("The gcd of 15 and 5 is: " + gcd(15, 5));
        System.out.println("The gcd of 24 and 18 is: " + gcd(24, 18));
        System.out.println("The gcd of 10 and 10 is: " + gcd(10, 10));
        System.out.println("The gcd of 21 and 400 is: " + gcd(21, 400));

        System.out.println("Subtraction: " + Fraction.subtract(f4, f5));
        
        System.out.println(""+getCommonDenom(fractArr));
    }

}
