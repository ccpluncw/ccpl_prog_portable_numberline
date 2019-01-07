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

import java.security.SecureRandom;
import java.util.Random;

/**
 * An improved random number generator based on Algorithm B in Knuth Vol 2 p32. Gives a set of
 * random integers that does not exhibit as much correlation as the method used by the Java random
 * number generator.
 *
 * @author Cay Horstmann
 * @version 1.01 15 Feb 1996
 */
public class RandomIntGenerator {

  private int low;
  private int high;
  private int interval;
  private double doubleLow;
  private double doubleHigh;
  private double doubleInterval;

  private static final Random randomObj = new SecureRandom();

  /** Constructs an object that generates random integers in a given range. */
  public RandomIntGenerator() {
    low = 0;
    high = 1;
  }

  RandomIntGenerator(int l, int h) {
    this(l, h, 1);
  }

  /**
   * Initialize a RandomIntGenerator with a minimum, maximum, and interval.
   *
   * @param low Minimum value
   * @param high Maximum value
   * @param interval Interval
   */
  public RandomIntGenerator(int low, int high, int interval) {
    this.low = low;
    this.high = high;
    this.interval = interval;
  }

  /**
   * Set the interval range.
   *
   * @param low Minimum value
   * @param high Maximum value
   * @param interval Interval
   */
  public void setIntervalRange(int low, int high, int interval) {
    this.low = low;
    this.high = high;
    this.interval = interval;
  }

  /**
   * Set the interval for a floating point number.
   *
   * @param low Minimum value
   * @param high Maximum value
   * @param interval Interval in the range
   */
  void setDoubleIntervalRange(double low, double high, double interval) {
    doubleLow = low;
    doubleHigh = high;
    doubleInterval = interval;
  }

  /**
   * Generates a random integer in a range of integers.
   *
   * @return a random integer
   */
  int draw() {
    return low + (int) ((high - low + 1) * nextRandom());
  }

  /**
   * Select a random value in the interval with a set interval distance.
   *
   * @return Random value between minimum and maximum.
   */
  public int drawWithInterval() {
    int r1 = (high - low + interval) / interval;
    int r2 = (int) (r1 * nextRandom());
    int r3 = r2 * interval;

    return (r3 + low);
  }

  /**
   * Randomize a double value within the interval.
   *
   * @return Randomized double value
   */
  double drawDoubleWithInterval() {
    double r = doubleLow;
    double numPosValues = (doubleHigh - doubleLow) / doubleInterval + 1.0;
    double randLimit = (int) (nextRandom() * numPosValues);
    r += (doubleInterval * randLimit);
    return r;
  }

  private static double nextRandom() {
    double random = randomObj.nextDouble();

    int pos = (int) (random * BUFFER_SIZE);
    if (pos == BUFFER_SIZE) {
      pos = BUFFER_SIZE - 1;
    }
    double r = buffer[pos];
    buffer[pos] = random;
    return r;
  }

  private static final int BUFFER_SIZE = 101;
  private static final double[] buffer = new double[BUFFER_SIZE];

  static {
    int i;
    for (i = 0; i < BUFFER_SIZE; i++) {
      buffer[i] = Math.random();
    }
  }
}
