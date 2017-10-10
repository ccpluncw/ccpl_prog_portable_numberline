
package ccpl.lib;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;

public class Unit {

  public enum UNITTYPE {
    INT, DECI, FRACT, ODDS
  }

  private final UNITTYPE TYPE;
  private final String VALUE;
  private final double DOUBLE_VALUE;

  public Unit(String unit) {
    VALUE = unit;
    unit = unit.trim();
    String intRegex = "^(\\d)+$";
    String deciRegex = "^(\\d)*(.(\\d)+)?$";
    String fractRegex = "^(\\d)+\\s*/\\s*(\\d)+$";
    String oddRegex = "^(\\d)+\\s*in\\s*(\\d)+$";
    if (unit.matches(intRegex)) {
      TYPE = UNITTYPE.INT;
      DOUBLE_VALUE = Double.parseDouble(unit);
    } else if (unit.matches(fractRegex)) {
      TYPE = UNITTYPE.FRACT;
      Fraction f = new Fraction(unit);
      DOUBLE_VALUE = f.toDouble();
    } else if (unit.matches(deciRegex)) {
      TYPE = UNITTYPE.DECI;
      DOUBLE_VALUE = Double.parseDouble(unit);
    } else if (unit.matches(oddRegex)) {
      TYPE = UNITTYPE.ODDS;
      String[] odds = unit.split("in");
      Fraction f = new Fraction(Integer.parseInt(odds[0].trim()), Integer.parseInt(odds[1].trim()));
      DOUBLE_VALUE = f.toDouble();
    } else {
      throw new IllegalArgumentException("Invalid Unit Format: " + unit);
    }
  }

  public String getValue() {
    return VALUE;
  }

  public int toInteger() {
    return (int) DOUBLE_VALUE;
  }

  public double toDouble() {
    return DOUBLE_VALUE;
  }

  public UNITTYPE getType() {
    return TYPE;
  }

  public static Unit getRandomUnit(Unit low, Unit high, Unit interval) {
    RandomIntGenerator randGen = new RandomIntGenerator();
    String targetUnit = "";

    UNITTYPE uLowType = low.getType();
    UNITTYPE uHighType = high.getType();
    UNITTYPE uIntervalType = interval.getType();

    if (uLowType != uHighType || uLowType != uIntervalType || uHighType != uIntervalType) {
      throw new IllegalArgumentException("Units not all of same type");
    }

    if (uLowType == UNITTYPE.INT) {
      randGen.setIntervalRange(Integer.parseInt(low.getValue()), Integer.parseInt(high.getValue()), Integer.parseInt(interval.getValue()));
      targetUnit = Integer.toString(randGen.drawWithInterval());
    } else if (uLowType == UNITTYPE.DECI) {
      randGen.setDoubleIntervalRange(Double.parseDouble(low.getValue()), Double.parseDouble(high.getValue()), Double.parseDouble(interval.getValue()));
      targetUnit = Double.toString(randGen.drawDoubleWithInterval());
    } else if (uLowType == UNITTYPE.FRACT || uLowType == UNITTYPE.ODDS) {
      String fract[] = {"", ""};

      Fraction targetFracts[] = new Fraction[3];

      boolean matched = false;
      int randTargetNum = 0;

      Unit parseUnits[] = {low, high, interval};

      for (int i = 0; i < parseUnits.length; i++) {
        if (parseUnits[i].getType() == UNITTYPE.FRACT) {
          fract = parseUnits[i].getValue().split("/");
          matched = true;
        } else if (parseUnits[i].getType() == UNITTYPE.ODDS) {
          fract = parseUnits[i].getValue().split("in");
          matched = true;
        }
        if (matched) {
          try {
            targetFracts[i] = new Fraction(Integer.parseInt(fract[0].trim()), Integer.parseInt(fract[1].trim()));
          } catch (NumberFormatException e) {
            System.err.println("Could not parse target value " + parseUnits[i]);
            System.exit(1);
          }
        } else {
          System.err.println("Invalid target value " + parseUnits[i]);
          System.exit(1);
        }
        matched = false;
      }// end for

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

      //System.err.println(fractArray[0][0] + ", " + fractArray[1][0]+ ", " + fractArray[2][0]);
      randGen.setIntervalRange(targetFracts[0].getNumerator(), targetFracts[1].getNumerator(), targetFracts[2].getNumerator());
      randTargetNum = randGen.drawWithInterval();
      //System.err.println(""+randTargetNum + "/" + commonDenom);
      if (uLowType == UNITTYPE.FRACT) {
        targetUnit = Integer.toString(randTargetNum) + "/" + commonDenom;
      } else if (uLowType == UNITTYPE.ODDS) {
        targetUnit = Integer.toString(randTargetNum) + " in " + commonDenom;
      }

    }
    return new Unit(targetUnit);
  }

  @Override
  public String toString() {
    return VALUE;
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
    return getLabel(VALUE, labelFont);
  }
}
