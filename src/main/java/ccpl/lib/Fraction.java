package ccpl.lib;


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

}
