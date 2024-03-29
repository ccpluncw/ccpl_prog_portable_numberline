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

package ccpl.lib.numberline;

import ccpl.lib.Handle;
import ccpl.lib.Unit;
import ccpl.lib.numberline.abs.AbstractHandleNumberLine;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * ***************** Written by Kyle Holt lkh3273@uncw.edu October-November 2009 *****************
 */
public class NumberLine implements AbstractHandleNumberLine, MouseMotionListener, MouseListener {

  final NumberLinePanel linePanel;
  // ---Pixel members-------
  private final int baseHeight; // Default overall baseHeight
  private final int lineThickness;
  private int baseWidth;
  private final int handlePadding; // Padding size of region around the handle
  private int basePaddingLeft; // Amount of padding applied to experiment within the panel

  // ---Color members-----
  private final Color baseColor;
  private Color dragColor;
  private Color dragInactiveColor;
  private Color dragActiveColor;

  private final Unit startUnit;
  private final Unit endUnit;
  private final Unit targetUnit;
  private final int startUnitLabelW;
  private final int startUnitLabelH;
  private final int endUnitLabelW;
  private final int endUnitLabelH;

  // ----Control members-----
  private boolean atDragRegion; // Flag to note if mouse is moved to drag region
  private boolean isHandleDragged; // Flag to determine if handle was dragged

  // -----New Data Added By Oliver
  // flag which determines if the handle can be dragged outside the bounds of the numberline
  private boolean[] keepWithinBounds;
  private boolean[] showHideLabels;
  private final Point2D.Float startPoint;
  private Point2D.Float extendPoint2D;
  private final Point2D.Float centerScreen;
  private Point2D.Float currentDragPoint;

  private final Point2D.Float leftBoundHigh;
  private final Point2D.Float rightBoundHigh;

  private Point2D.Float handleHigh;
  private Point2D.Float handleStartPoint;
  private Point2D.Float handleLow;
  private Point2D.Float guideHandleHigh;
  private Point2D.Float guideHandleLow;

  private final Line2D leftGuide;
  private final Line2D rightGuide;

  private final float slope;

  private final Dimension screen;

  private final Point2D.Float startLoc;
  private final Point2D.Float targetLoc;
  private final Point2D.Float handleLoc;
  private final Point2D.Float endLoc;

  private final Color fontColor = Color.LIGHT_GRAY;
  private final Font displayFont;
  private boolean adjustChange = true;

  private Line2D dragLine;
  private final Line2D extendLine;
  private final Line2D endLine;
  private final Line2D startLine;
  private final int startX; // startX is x coord where numberline is visually drawn
  private int currentDragX; // Holds x coord of the drag handle
  private final Point extendPoint; // (x, y) coord of point on base where the drag line appears
  private final Stroke stroke; // the stroke to show line thickness
  private final String fontName; // the name of the Font to use
  private final Point2D.Float rightBoundPoint;

  private Handle activeDragHandle = null; // Set to null since it's unknown at this point.

  private Handle leftDragHandle;
  private Handle rightDragHandle;

  private final boolean isEstimateTask;
  private final float widthPercentage; // holds percentage of base width to display

  private Line2D fixationLine;

  private final int unitSize;

  private boolean isOutsideBounds;

  private int guideWidth = 1; // Guides are 1px in size;
  private int rightShift;
  private int handleShift;

  private boolean targetInside = false;

  private Color leftBndColor;
  private Color rightBndColor;

  /**
   * Create a number line.
   *
   * @param width Width of the panel
   * @param height Height of the panel
   * @param thickness Thickness of the line
   * @param startU Start unit
   * @param endU End unit
   * @param targetU Target unit
   * @param baseColor Number line's base color
   * @param dragColor Color of the number line when dragged
   * @param handleColor Color of the handle
   * @param font Font for the labels
   * @param estimateTask Is it an estimation task?
   * @param m Message
   * @param kwb Keep with bounds?
   * @param handleAlignment Handle alignment
   * @param shLabels Show labels?
   * @param unitSize Unit size
   */
  public NumberLine(
      int width,
      int height,
      int thickness,
      Unit startU,
      Unit endU,
      Unit targetU,
      Color baseColor,
      Color dragColor,
      Color handleColor,
      String font,
      boolean estimateTask,
      int m,
      boolean[] kwb,
      char handleAlignment,
      boolean[] shLabels,
      int unitSize) {

    guideWidth = thickness;

    isOutsideBounds = checkBounds(targetU, endU);
    targetInside = !isOutsideBounds;
    boolean onBounds = startU.equals(targetU) || endU.equals(targetU);

    if (onBounds || isOutsideBounds) {
      rightShift = guideWidth;
      handleShift = guideWidth;
    } else {
      rightShift += 2 * guideWidth;
      handleShift = guideWidth;
    }

    this.unitSize = unitSize;

    // ----- added by Oliver
    keepWithinBounds = kwb;
    showHideLabels = shLabels;
    baseWidth = width;
    widthPercentage = 1;
    baseHeight = height;
    lineThickness = thickness;
    isEstimateTask = estimateTask;

    Toolkit tk = Toolkit.getDefaultToolkit();
    screen = tk.getScreenSize();
    centerScreen = new Point2D.Float(screen.width / 2, screen.height / 2);

    screen.height -= (m * 2); // take margins out of either side
    screen.width -= (m * 2); // take margins out of either side

    startPoint = getStartPoint();
    rightBoundPoint = getRightBound();
    slope = getNumLineSlope();

    extendPoint2D =
        new Point2D.Float(
            (float) startPoint.getX() + baseWidth + rightShift, (float) (startPoint.getY()));

    leftBoundHigh = getHighPoint(baseHeight, startPoint);
    rightBoundHigh = getHighPoint(baseHeight, extendPoint2D);

    Point2D.Float guideLeftLow = getLowPoint(baseHeight / 2, startPoint);
    Point2D.Float guideLeftHigh = getHighPoint(baseHeight + lineThickness, startPoint);

    Point2D.Float guideRightLow = getLowPoint(baseHeight / 2, extendPoint2D);
    Point2D.Float guideRightHigh = getHighPoint(baseHeight + lineThickness, extendPoint2D);

    leftGuide =
        new Line2D.Float(
            guideLeftLow.x, guideLeftLow.y,
            guideLeftHigh.x, guideLeftHigh.y);

    rightGuide =
        new Line2D.Float(
            guideRightLow.x, guideRightLow.y,
            guideRightHigh.x, guideLeftHigh.y);

    extendLine = new Line2D.Float(startPoint, extendPoint2D);

    startLine =
        new Line2D.Float(
            leftBoundHigh.x, leftBoundHigh.y + lineThickness * 3, startPoint.x, startPoint.y);

    endLine =
        new Line2D.Float(
            rightBoundHigh.x,
            rightBoundHigh.y + lineThickness * 3,
            extendPoint2D.x,
            extendPoint2D.y);

    leftDragHandle = new Handle(startLine, leftGuide, baseColor, lineThickness);
    rightDragHandle = new Handle(endLine, rightGuide, baseColor, lineThickness);

    fixationLine = startLine;

    if (handleAlignment == 'L') {
      handleStartPoint = startPoint;
    } else if (handleAlignment == 'R') {
      handleStartPoint = extendPoint2D;
    } else if (handleAlignment == 'X') {
      // randomly choose a side for the handle to start on
      Random gen = new Random();
      int temp = gen.nextInt(10);
      // the random number was less than five the handle starts under the startPoint
      if (temp <= 5) {
        handleStartPoint = startPoint;
      } else {
        // random number greater than five handle starts under extendPoint
        handleStartPoint = extendPoint2D;
      }
    }

    currentDragPoint = handleStartPoint;

    handleHigh = getHighPoint(baseHeight, handleStartPoint);
    handleLow = handleStartPoint;

    guideHandleHigh = getHighPoint(baseHeight - lineThickness, handleStartPoint);
    guideHandleLow = getLowPoint(baseHeight / 2, handleStartPoint);

    extendPoint = new Point();
    extendPoint.x = (int) guideRightLow.getX();
    extendPoint.y = (int) guideRightLow.getY();

    endLoc = new Point2D.Float();
    endLoc.x = (float) guideRightLow.getX();
    endLoc.y = (float) guideRightLow.getY();

    startLoc = new Point2D.Float(); // holds the position for the label under startPoint
    startLoc.x = (float) guideLeftLow.getX();
    startLoc.y = (float) guideLeftLow.getY();

    targetLoc = new Point2D.Float(); // holds the position for the label of the target
    targetLoc.x = startLoc.x;
    targetLoc.y = startLoc.y;

    startUnitLabelW = 0;
    startUnitLabelH = 0;
    endUnitLabelW = 0;
    endUnitLabelH = 0;

    startUnit = startU;
    endUnit = endU;
    targetUnit = targetU;

    stroke = new BasicStroke(lineThickness); // create a stroke object from the line thickness
    handlePadding = lineThickness + 15; // Padding size of region around the handle

    atDragRegion = false;

    setBaseLeftPadding();

    this.baseColor = baseColor;
    this.dragColor = dragColor;
    this.dragInactiveColor = dragColor;
    this.dragActiveColor = dragColor;

    this.leftBndColor = baseColor;
    this.rightBndColor = baseColor;

    Color handleActiveColor = handleColor;
    fontName = font;
    displayFont = new Font(fontName, Font.BOLD, 12);

    startX = basePaddingLeft;

    if (isEstimateTask) {
      isHandleDragged = true; // handle not used in estimation
      currentDragPoint = getTargetPoint();

      handleHigh = getHighPoint(baseHeight / 2, currentDragPoint);
      handleLow = currentDragPoint;

      guideHandleHigh = getHighPoint(baseHeight - lineThickness, currentDragPoint);
      guideHandleLow = getLowPoint(baseHeight / 2, currentDragPoint);
    } else {
      isHandleDragged = false;
    }

    handleLoc = new Point2D.Float(); // holds the position for the label under the handle
    handleLoc.x = (float) guideHandleLow.getX();
    handleLoc.y = (float) guideHandleLow.getY();

    linePanel = new NumberLinePanel();
    linePanel.addMouseMotionListener(this);
    linePanel.addMouseListener(this);

    if (isEstimateTask) {
      handleHigh = getHighPoint(baseHeight, currentDragPoint);
      handleLow = currentDragPoint;

      if (targetUnit.toDouble() > endUnit.toDouble()) {
        currentDragPoint.x += 2 * guideWidth;
        handleHigh.x += 2 * guideWidth;
      } else if (targetUnit.toDouble() != startUnit.toDouble()) {
        currentDragPoint.x += guideWidth;
        handleHigh.x += guideWidth;
      } else {
        ((Line2D.Float) endLine).x1 -= guideWidth;
        ((Line2D.Float) endLine).x2 -= guideWidth;
        ((Line2D.Float) rightGuide).x1 -= guideWidth;
        ((Line2D.Float) rightGuide).x2 -= guideWidth;
        ((Line2D.Float) extendLine).x2 -= guideWidth;
        extendPoint2D.x -= guideWidth;
      }

      guideHandleLow = getLowPoint(baseHeight / 2, currentDragPoint);
      guideHandleHigh = getHighPoint(baseHeight + lineThickness, currentDragPoint);

      handleLoc.x = (float) guideHandleLow.getX();
      handleLoc.y = (float) guideHandleLow.getY();

      activeDragHandle = new Handle(endLine, rightGuide, handleActiveColor, lineThickness);
      this.dragColor = handleActiveColor;
      linePanel.updateDragLine();
    }
  }

  private boolean checkBounds(Unit unit1, Unit unit2) {
    return unit1.toDouble() > unit2.toDouble();
  }

  private Point2D.Float getTargetPoint() {
    return getTargetSpecial();
  }

  private Point2D.Float getTargetSpecial() {
    double startToTarget;
    Point2D.Float p;

    if (startUnit.toDouble() > endUnit.toDouble()) {
      isOutsideBounds = checkBounds(endUnit, targetUnit) || checkBounds(targetUnit, startUnit);

      startToTarget = targetUnit.toDouble() - endUnit.toDouble();
      startToTarget *= unitSize;

      p =
          new Point2D.Float(
              (float) extendPoint2D.getX() - (float) startToTarget, (float) extendPoint2D.getY());
    } else if (startUnit.toDouble() < endUnit.toDouble()) {
      isOutsideBounds = checkBounds(targetUnit, endUnit) || checkBounds(startUnit, targetUnit);

      startToTarget = targetUnit.toDouble() - startUnit.toDouble();
      startToTarget *= unitSize;

      p =
          new Point2D.Float(
              (float) startPoint.getX() + (float) startToTarget, (float) startPoint.getY());
    } else {
      isOutsideBounds = false;
      p = new Float((float) extendPoint2D.getX(), (float) extendPoint2D.getY());
      dragLine = new Line2D.Float(extendPoint2D, extendPoint2D);
    }

    if (isOutsideBounds) {
      dragLine = new Line2D.Float(extendPoint2D, p);
    }

    return p;
  }

  /** Return the number line panel. */
  @Override
  public JPanel getPanel() {
    if (isEstimateTask) {
      int w = startX + startUnitLabelW + endUnitLabelW + lineThickness * 2 + 10;
      int h = extendPoint.y + Math.max(startUnitLabelH, startUnitLabelW) + endUnitLabelH + 20;
      w += Math.max(baseWidth, currentDragX);
      linePanel.setSize(w, h);
    }

    return linePanel;
  }

  private void setBaseLeftPadding() {
    final int pad = 5;
    Font font = new Font(fontName, Font.BOLD, 12);
    JLabel label = startUnit.getLabel(font);
    basePaddingLeft = label.getWidth() + (lineThickness * 2) + pad;
  }

  /** Return the unit length of the number line. */
  @Override
  public double getUnitLength() {
    return unitSize;
  }

  @Override
  public double getUnitLength(String userResp) {
    return parseUnitString(userResp);
  }

  /**
   * Return the user's response, corrected for any number line size adjustments.
   * @return User response.
   */
  public double getUserResponse() {
    int totalShift = isOutsideBounds ? handleShift + rightShift : handleShift;

    if (targetInside && isOutsideBounds) {
      totalShift += guideWidth;
    }

    // There is one edge case where the drag handle is on the left bound.
    // The shift will make the answer negative.
    if (currentDragPoint.x == leftGuide.getX1()) {
      totalShift = 0;
    }

    return (currentDragPoint.x - leftGuide.getX1() - totalShift) / unitSize + startUnit.toInteger();
  }

  @Override
  public Line2D getFixationLine() {
    return fixationLine;
  }

  private double parseUnitString(String unitLength) {
    double unitLen;
    String[] temp;
    if (startUnit.getType() == Unit.UnitType.FRACT) {
      temp = unitLength.split("/");
      unitLen = (Double.parseDouble(temp[0].trim())) / (Integer.parseInt(temp[1].trim()));
    } else if (startUnit.getType() == Unit.UnitType.ODDS) {
      temp = unitLength.split("in");
      unitLen = (Double.parseDouble(temp[0].trim())) / (Integer.parseInt(temp[1].trim()));
    } else {
      try {
        unitLen = Double.parseDouble(unitLength);
      } catch (Exception e) {
        unitLen = 0.0;
      }
    }
    return unitLen;
  }

  @Override
  public boolean isHandleDragged() {
    return isHandleDragged;
  }

  private boolean cursorInBoundingBox(Line2D handle, int cursorX, int cursorY) {
    if (handle == null) {
      return false;
    }

    Rectangle2D box = handle.getBounds2D();

    box.setRect(
        (int) (box.getMinX() - (handlePadding / 2)),
        (int) (box.getMinY() - (handlePadding / 2)),
        (int) (box.getWidth() + handlePadding),
        (int) (box.getHeight() + handlePadding));

    return box.contains(cursorX, cursorY);
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  /**
   * Update the location of the drag handle when the mouse is moved if it is a production task.
   *
   * @param e MouseEvent
   */
  public void mouseDragged(MouseEvent e) {
    if (isEstimateTask || !atDragRegion) {
      return;
    }

    if (activeDragHandle == null) {
      activeDragHandle =
          cursorInBoundingBox(leftDragHandle, e.getX(), e.getY())
              ? leftDragHandle
              : rightDragHandle;
      Point2D activeP2 = activeDragHandle.getP2();
      currentDragPoint = new Point2D.Float((float) activeP2.getX(), (float) activeP2.getY());
    }

    if (keepWithinBounds[0] && keepWithinBounds[1]) {
      // both are true handle is bound by both bounds
      if (e.getX() > extendPoint2D.getX()) {
        currentDragPoint =
            new Point2D.Float((float) extendPoint2D.getX(), (float) extendPoint2D.getY());
      } else if (e.getX() < startPoint.getX()) {
        currentDragPoint = new Point2D.Float((float) startPoint.getX(), (float) startPoint.getY());
      } else {
        currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
      }
    } else if (keepWithinBounds[0]) {
      if (e.getX() < startPoint.getX()) {
        currentDragPoint = new Point2D.Float((float) startPoint.getX(), (float) startPoint.getY());
      } else {
        currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
      }
    } else {
      if (e.getX() > extendPoint2D.getX()) {
        currentDragPoint =
            new Point2D.Float((float) extendPoint2D.getX(), (float) extendPoint2D.getY());
      } else {
        currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
      }
    }

    // check to make sure line does not extend past right bound
    if (currentDragPoint.getX() > rightBoundPoint.getX()) {
      currentDragPoint = rightBoundPoint;
    }

    handleHigh = getHighPoint(baseHeight, currentDragPoint);
    handleLow = currentDragPoint;
    guideHandleLow = getLowPoint(baseHeight / 2, currentDragPoint);
    guideHandleHigh = getHighPoint(baseHeight + lineThickness, currentDragPoint);
    handleLoc.x = (float) guideHandleLow.getX();
    handleLoc.y = (float) guideHandleLow.getY();
    isHandleDragged = true;

    linePanel.updateDragLine();
  }

  /**
   * Update the number line is the mouse has been moved and it is a production task.
   *
   * @param e Mouse Event.
   */
  public void mouseMoved(MouseEvent e) {
    if (isEstimateTask) {
      return;
    }

    atDragRegion = false;

    if (activeDragHandle == null) {
      checkDragStatus(leftDragHandle, e.getX(), e.getY());
      checkDragStatus(rightDragHandle, e.getX(), e.getY());
    } else {
      checkDragStatus(activeDragHandle, e.getX(), e.getY());
    }

    dragColor = atDragRegion ? dragActiveColor : dragInactiveColor;

    linePanel.repaint();
  }

  private void checkDragStatus(Handle handle, int x, int y) {
    if (handle == null) {
      return;
    }

    if (cursorInBoundingBox(handle, x, y)) {
      atDragRegion = true;
      handle.useActiveColor();

      return;
    }

    handle.useBaseColor();
  }

  private Point2D.Float getHighPoint(int d, Point2D.Float p) {
    return new Point2D.Float((float) p.getX(), (float) p.getY() - d);
  }

  private Point2D.Float getLowPoint(int d, Point2D.Float p) {
    return new Point2D.Float((float) p.getX(), (float) p.getY() + d);
  }

  private float getNumLineSlope() {
    return ((startPoint.y - centerScreen.y) / (startPoint.x - centerScreen.x));
  }

  private float getCorrespondingY(float x) {
    return ((slope * (x - startPoint.x)) + startPoint.y);
  }

  private Point2D.Float getStartPoint() {
    // TODO: Fix this to avoid hardcoded degrees.
    double r = Math.toRadians(180);

    float x = (float) (centerScreen.x + ((screen.width / 2) * Math.cos(r))); // x = h + a * cos(t)
    float y = (float) (centerScreen.y + ((screen.height / 2) * Math.sin(r))); // y = k + b * sin(t)

    return new Point2D.Float(x, y);
  }

  private Point2D.Float getRightBound() {
    float x;
    float y;

    // TODO: Fix this to avoid hardcoded degrees.
    double r = 360;
    r = Math.toRadians(r);

    x = (float) (centerScreen.x + ((screen.width / 2) * Math.cos(r))); // x = h + a * cos(t)
    y = (float) (centerScreen.y + ((screen.height / 2) * Math.sin(r))); // y = k + b * sin(t)
    return new Point2D.Float(x, y);
  }

  class NumberLinePanel extends JPanel {
    private Line2D currentLine;

    NumberLinePanel() {
      super(null);
      setBackground(Color.BLACK);
    }

    void updateDragLine() {
      if (activeDragHandle == null) {
        return;
      }

      activeDragHandle.useActiveColor();

      dragLine = new Line2D.Float(extendPoint2D, currentDragPoint);

      activeDragHandle.getGuide().setLine(guideHandleLow, guideHandleHigh);
      activeDragHandle.setLine(
          handleHigh.x, handleHigh.y + lineThickness * 3, handleLow.x, handleLow.y);

      isOutsideBounds = activeDragHandle.x1 > rightGuide.getX1();

      this.repaint();
    }

    // method that draws the base for the numberline that is drawn along a ellispe
    private void drawBaseCircle(Graphics2D g) {
      g.setColor(baseColor);
      g.setStroke(stroke);

      g.draw(extendLine);

      g.setColor(leftBndColor);
      //g.draw(startLine);

      g.setColor(rightBndColor);
      //g.draw(endLine);

      if (activeDragHandle == null) {
        if (leftDragHandle != null) {
          leftDragHandle.draw(g);
        }

        rightDragHandle.draw(g);
        return;
      }

      activeDragHandle.draw(g);
    }

    private void adjustChange(FontMetrics fm, String s, String t) {
      int startUnitLength = fm.stringWidth(s);
      int targetUnitLength = fm.stringWidth(t);
      int fontHeight = fm.getHeight();

      startLoc.x = startLoc.x - startUnitLength;
      targetLoc.x = targetLoc.x - targetUnitLength;
      startLoc.y = startLoc.y + fontHeight;
      targetLoc.y = targetLoc.y + (fontHeight * 2);
      endLoc.y = endLoc.y + fontHeight;
    }

    private void displayLabels(Graphics2D g) {
      double degrees = Math.toDegrees(Math.atan(slope));
      double theta = Math.toRadians(degrees);

      FontMetrics fm = g.getFontMetrics(displayFont);

      g.setFont(displayFont);
      g.setColor(fontColor);

      String start = getStartLabel();
      String end = getEndLabel();
      String target = getTargetLabel();

      if (adjustChange) {
        adjustChange(fm, start, target);
        adjustChange = false;
      }

      if (showHideLabels[0]) {
        g.rotate(theta, startLoc.x, startLoc.y);
        g.drawString(start, startLoc.x, startLoc.y);
        g.rotate(-theta, startLoc.x, startLoc.y);
      }
      if (showHideLabels[1]) {
        g.rotate(theta, endLoc.x, endLoc.y);
        g.drawString(end, endLoc.x, endLoc.y);
        g.rotate(-theta, endLoc.x, endLoc.y);
      }
      if (!isEstimateTask && showHideLabels[2]) {
        g.rotate(theta, targetLoc.x, targetLoc.y);
        g.drawString(target, targetLoc.x, targetLoc.y);
        g.rotate(-theta, targetLoc.x, targetLoc.y);
      }
      if (showHideLabels[3]) {
        g.rotate(theta, handleLoc.x, handleLoc.y);
        g.rotate(-theta, handleLoc.x, handleLoc.y);
      }
    }

    private String getStartLabel() {
      return getLabel(startUnit);
    }

    private String getEndLabel() {
      return getLabel(endUnit);
    }

    private String getTargetLabel() {
      return getLabel(targetUnit);
    }

    private String getLabel(Unit unit) {
      String[] tempFraction;
      StringBuilder sb = new StringBuilder();
      switch (unit.getType()) {
        case FRACT:
          tempFraction = unit.getValue().split("/");
          sb.append(tempFraction[0]);
          sb.append("/");
          sb.append(tempFraction[1]);
          break;
        case ODDS:
          tempFraction = unit.getValue().split("in");
          sb.append(tempFraction[0]);
          sb.append(" in ");
          sb.append(tempFraction[1]);
          break;
        case DECI:
          sb.append(unit.toDouble());
          break;
        case INT:
          sb.append(unit.toInteger());
          break;
        default:
          System.out.println("Unsupported Unit type");
          System.exit(0);
      }

      return sb.toString();
    }

    /**
     * Overrides this panel's paintComponent method. NOTE: The class is a subclass of JPanel
     * Necessary to ensure current graphics are redrawn on panel when panel is refreshed
     */
    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D graphics = (Graphics2D) g;
      graphics.setStroke(stroke);
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      if (currentLine != null) {
        graphics.setStroke(stroke);
        graphics.draw(currentLine);
        currentLine = null;
      }

      if (dragLine != null) {
        graphics.setColor(dragColor);
        graphics.draw(dragLine);
      }

      graphics.setStroke(new BasicStroke(lineThickness));
      graphics.setColor(leftBndColor);
      graphics.draw(leftGuide);

      graphics.setColor(rightBndColor);
      graphics.draw(rightGuide);

      drawBaseCircle(graphics);

      displayLabels(graphics);
    }
  }

  public void setLeftBoundColor(Color newColor) {
    this.leftBndColor = newColor;
  }

  public void setRightBoundColor(Color newColor) {
    this.rightBndColor = newColor;
  }

  public void setLeftDragHandleColor(Color newColor) {
    leftDragHandle.setBaseColor(newColor);
  }

  public void setLeftDragActiveColor(Color newColor) {
    leftDragHandle.setActiveColor(newColor);
  }

  public void setRightDragHandleColor(Color newColor) {
    rightDragHandle.setBaseColor(newColor);
  }

  public void setRightDragActiveColor(Color newColor) {
    rightDragHandle.setActiveColor(newColor);
  }

  public void setDragActiveColor(Color newColor) {
    dragActiveColor = newColor;
  }

  public void disableLeftHandle() {
    leftDragHandle = null;
  }

  @Override
  public double getUnitError(boolean inPercent) {
    if (inPercent) {
      return 1.0 * unitSize / targetUnit.toDouble();
    } else {
      return 1.0 * unitSize / targetUnit.toDouble();
    }
  }

  @Override
  public double getUnitError(boolean inPercent, String userRespLength) {
    if (inPercent) {
      return parseUnitString(userRespLength) / targetUnit.toDouble();
    } else {
      return parseUnitString(userRespLength) / targetUnit.toDouble();
    }
  }

  @Override
  public int getBaseWidth() {
    return baseWidth;
  }
}
