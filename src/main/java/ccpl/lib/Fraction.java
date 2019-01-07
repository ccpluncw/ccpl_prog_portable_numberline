/*
 * This file is part of the Cohen Ray Number Line.
 *
 * Latesco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Latesco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Latesco.  If not, see <http://www.gnu.org/licenses/>.
 */

package ccpl.lib;

/**
 * Fraction class.
 *
 * @author Kyle
 */
public class Fraction {

  private final int numerator;
  private final int denominator;

  private Fraction() {
    numerator = 0;
    denominator = 1;
  }

  /**
   * Initialize a Fraction with a numerator and denominator.
   *
   * @param num Numerator
   * @param denom Denominator
   */
  public Fraction(int num, int denom) {
    if (denom == 0) {
      throw new IllegalArgumentException("Denominator can not be zero!");
    }
    numerator = num;
    denominator = denom;
  }

  /**
   * Initialize a fraction based on a string.
   *
   * @param frac Fraction stored as a String
   */
  public Fraction(String frac) {
    String[] fraction = frac.split("/");
    numerator = Integer.parseInt(fraction[0]);
    denominator = Integer.parseInt(fraction[1]);
  }

  /** Return the numerator of the Fraction. */
  public int getNumerator() {
    return numerator;
  }

  /** Return the denominator of the fraction. */
  public int getDenominator() {
    return denominator;
  }

  @Override
  public String toString() {
    return numerator + "/" + denominator;
  }

  double toDouble() {
    return (double) numerator / denominator;
  }

  private static int getCommonDenom(Fraction f1, Fraction f2) {
    int gcd1;
    if (f1.denominator != f2.denominator) {
      gcd1 = f1.denominator * f2.denominator;
    } else {
      gcd1 = f1.denominator;
    }
    return gcd1;
  }

  /**
   * Determine the common denomination of an array of fractions.
   *
   * @param fractions Input fractions.
   * @return Common denominator
   */
  static int getCommonDenom(Fraction[] fractions) {
    Fraction currFract = new Fraction();
    for (Fraction fraction : fractions) {
      currFract = new Fraction(1, getCommonDenom(currFract, fraction));
    }
    return currFract.denominator;
  }

  private static int gcd(int a, int b) {
    if (b == 0) {
      return a;
    } else {
      return gcd(b, a % b);
    }
  }

  /**
   * Reduce a fraction if possible.
   *
   * @param f Fraction to be reduced.
   * @return Reduced fraction.
   */
  public static Fraction reduceFract(Fraction f) {
    int gcd = gcd(f.numerator, f.denominator);
    int num = f.numerator / gcd;
    int denom = f.denominator / gcd;
    return new Fraction(num, denom);
  }
}
