
package ccpl.lib;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;

public class Unit {

  public enum UnitType {
    INT, DECI, FRACT, ODDS
  }

  private final UnitType type;
  private final String value;
  private final double doubleValue;

  /**
   * Unit class.
   * @param unit  Unit stored in String format.
   */
  public Unit(String unit) {
    value = unit;
    unit = unit.trim();
    String intRegex = "^(\\d)+$";
    String deciRegex = "^(\\d)*(.(\\d)+)?$";
    String fractRegex = "^(\\d)+\\s*/\\s*(\\d)+$";
    String oddRegex = "^(\\d)+\\s*in\\s*(\\d)+$";

    if (unit.matches(intRegex)) {
      type = UnitType.INT;
      doubleValue = Double.parseDouble(unit);
    } else if (unit.matches(fractRegex)) {
      type = UnitType.FRACT;
      Fraction f = new Fraction(unit);
      doubleValue = f.toDouble();
    } else if (unit.matches(deciRegex)) {
      type = UnitType.DECI;
      doubleValue = Double.parseDouble(unit);
    } else if (unit.matches(oddRegex)) {
      type = UnitType.ODDS;
      String[] odds = unit.split("in");
      Fraction f = new Fraction(Integer.parseInt(odds[0].trim()), Integer.parseInt(odds[1].trim()));
      doubleValue = f.toDouble();
    } else {
      throw new IllegalArgumentException("Invalid Unit Format: " + unit);
    }
  }

  public String getValue() {
    return value;
  }

  public int toInteger() {
    return (int) doubleValue;
  }

  public double toDouble() {
    return doubleValue;
  }

  public UnitType getType() {
    return type;
  }

  /**
   * Get a random unit given a minimum, maximum, and interval.
   * @param low         Minimum
   * @param high        Maximum
   * @param interval    Interval
   * @return            Randomized number.
   */
  public static Unit getRandomUnit(Unit low, Unit high, Unit interval) {
    RandomIntGenerator randGen = new RandomIntGenerator();
    String targetUnit = "";

    UnitType unitLowType = low.getType();
    UnitType unitHighType = high.getType();
    UnitType unitIntervalType = interval.getType();

    if (unitLowType != unitHighType || unitLowType != unitIntervalType) {
      throw new IllegalArgumentException("Units not all of same type");
    }

    if (unitLowType == UnitType.INT) {
      randGen.setIntervalRange(Integer.parseInt(low.getValue()), Integer.parseInt(high.getValue()),
          Integer.parseInt(interval.getValue()));
      targetUnit = Integer.toString(randGen.drawWithInterval());
    } else if (unitLowType == UnitType.DECI) {
      randGen.setDoubleIntervalRange(Double.parseDouble(low.getValue()),
          Double.parseDouble(high.getValue()), Double.parseDouble(interval.getValue()));
      targetUnit = Double.toString(randGen.drawDoubleWithInterval());
    } else if (unitLowType == UnitType.FRACT || unitLowType == UnitType.ODDS) {
      String[] fract = {"", ""};

      Fraction[] targetFracts = new Fraction[3];

      boolean matched = false;

      Unit[] parseUnits = {low, high, interval};

      for (int i = 0; i < parseUnits.length; i++) {
        if (parseUnits[i].getType() == UnitType.FRACT) {
          fract = parseUnits[i].getValue().split("/");
          matched = true;
        } else if (parseUnits[i].getType() == UnitType.ODDS) {
          fract = parseUnits[i].getValue().split("in");
          matched = true;
        }

        if (matched) {
          try {
            targetFracts[i] = new Fraction(Integer.parseInt(fract[0].trim()),
                Integer.parseInt(fract[1].trim()));
          } catch (NumberFormatException e) {
            System.err.println("Could not parse target value " + parseUnits[i]);
            System.exit(1);
          }
        } else {
          System.err.println("Invalid target value " + parseUnits[i]);
          System.exit(1);
        }
        matched = false;
      }

      int commonDenom = Fraction.getCommonDenom(targetFracts);
      for (int i = 0; i < 3; i++) {
        if (targetFracts[i].getDenominator() != commonDenom) {
          int num = targetFracts[i].getNumerator() * commonDenom / targetFracts[i].getDenominator();
          targetFracts[i] = new Fraction(num, commonDenom);
        }
      }
      if (targetFracts[2].toDouble() > (targetFracts[1].toDouble() - targetFracts[0].toDouble())) {
        System.err.println("Target Unit Interval " + interval + " is too large.");
        System.exit(1);
      }

      randGen.setIntervalRange(targetFracts[0].getNumerator(), targetFracts[1].getNumerator(),
          targetFracts[2].getNumerator());
      int randTargetNum = randGen.drawWithInterval();

      if (unitLowType == UnitType.FRACT) {
        targetUnit = Integer.toString(randTargetNum) + "/" + commonDenom;
      } else {
        targetUnit = Integer.toString(randTargetNum) + " in " + commonDenom;
      }

    }
    return new Unit(targetUnit);
  }

  @Override
  public String toString() {
    return value;
  }

  private static JLabel getLabel(String val, Font labelFont) {
    JLabel unitLabel = new JLabel();
    unitLabel.setForeground(Color.LIGHT_GRAY);
    unitLabel.setFont(labelFont);
    unitLabel.setText(val);
    FontMetrics metrics = unitLabel.getFontMetrics(labelFont);
    int w = metrics.stringWidth(val);
    int h = metrics.getHeight();
    unitLabel.setSize(w, h);
    //setSize(getPreferredSize());
    return unitLabel;
  }

  public JLabel getLabel(Font labelFont) {
    return getLabel(value, labelFont);
  }
}
