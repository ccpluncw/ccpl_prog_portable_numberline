package ccpl.lib.numberline.abs;

import java.awt.geom.Line2D;
import javax.swing.JPanel;

public interface AbstractNumberLine {
  /**
   * Return state of handle.
   *
   * @return State of handle.
   */
  boolean isHandleDragged();

  /**
   * Return the pixel length of a single unit on the number line.
   *
   * @return Length of 1 unit.
   */
  double getUnitLength();

  /**
   * Convert a response into a decimal value.
   *
   * @param resp User's response
   * @return Decimal value
   */
  double getUnitLength(String resp);

  double getUnitError(boolean inPercent);

  double getUnitError(boolean inPercent, String userResp);

  int getBaseWidth();

  double getUserResponse();

  /**
   * Return the number line panel.
   *
   * @return Number Line Panel
   */
  JPanel getPanel();

  /**
   * Line that is displayed during fixation periods.
   *
   * @return Fixation line
   */
  Line2D getFixationLine();
}
