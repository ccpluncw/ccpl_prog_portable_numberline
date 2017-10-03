package ccpl.lib;

/**
   An improved random number generator based on Algorithm B
   in Knuth Vol 2 p32.
   Gives a set of random integers that does not exhibit
   as much correlation as the method used by the Java random number generator.

   @version 1.01 15 Feb 1996 
   @author Cay Horstmann
*/

public class RandomIntGenerator
{  /**
    *Constructs an object that generates random integers in a given range
    */
   public RandomIntGenerator(){
      low = 0;
      high = 1;
   }

   public RandomIntGenerator(int l, int h){
      this(l,h,1);
   }

   public RandomIntGenerator(int l, int h, int intv){
      low = l;
      high = h;
      interval = intv;
   }

   public RandomIntGenerator(double l, double h, double intv){
       dLow = l;
       dHigh = h;
       dInterval = intv;
   }

  public void setIntervalRange(int l ,int h, int intv){
       low = l;
       high = h;
       interval = intv;
   }

   public void setDoubleIntervalRange(double l, double h, double intv){
       dLow = l;
       dHigh = h;
       dInterval = intv;
   }
 
   /**
      Generates a random integer in a range of integers
      @return a random integer
   */
   public int draw(){
     return low + (int)((high - low + 1) * nextRandom());
   }

   public int drawWithInterval(){
       int r1 = (high-low + interval)/interval;
       int r2 = (int) (r1 * nextRandom());
       int r3 = r2*interval;
     //System.out.println(r1 + " " + r2 + " " + r3 + " " + r4);
       return (r3+low);
   }

   public double drawDoubleWithInterval(){
       double r = dLow;
       double numPosValues = (dHigh-dLow)/dInterval + 1.0;
       double randLimit = (int)(nextRandom()*numPosValues);
       r += (dInterval*randLimit);
       return r;
   }


   private static double nextRandom(){
      double random = randomObj.nextDouble();

      int pos =
         (int)(random * BUFFER_SIZE);
      if (pos == BUFFER_SIZE) pos = BUFFER_SIZE - 1;
      double r = buffer[pos];
      buffer[pos] = random;
      return r;
   }
   
   private static final int BUFFER_SIZE = 101;
   private static double[] buffer  = new double[BUFFER_SIZE];
   static{
      int i;
      for (i = 0; i < BUFFER_SIZE; i++)
         buffer[i] = Math.random();
   }

   public static void main(String[] args){
      RandomIntGenerator r1
         = new RandomIntGenerator(1, 10);
      RandomIntGenerator r2
         = new RandomIntGenerator();
      RandomIntGenerator r3 = new RandomIntGenerator(1.25, 2.50, .25);
      int i;
      for (i = 1; i <= 100; i++)
         System.out.println(r1.draw() + " " + r2.draw() + " " + r3.drawDoubleWithInterval());
   }
   
   private int low;
   private int high;
   private int interval;
   private double dLow;
   private double dHigh;
   private double dInterval;
   private static java.util.Random randomObj = new java.util.Random(System.currentTimeMillis());
}
