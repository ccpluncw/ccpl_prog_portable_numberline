package ccpl.lib;

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
import java.awt.geom.Rectangle2D;

import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;

/*******************
 * Written by Kyle Holt
 * lkh3273@uncw.edu
 * October-November 2009
 *******************/
public class NumberLine implements MouseMotionListener, MouseListener {

  final NumberLinePanel linePanel;
  //---Pixel members-------
  private final int baseHeight; //Default overall baseHeight
  private final int lineThickness;
  private int baseWidth;
  private final int handlePadding;  //Padding size of region around the handle
  private int basePaddingLeft; // Amount of padding applied to experiment within the panel

  //---Color members-----
  private final Color baseColor;
  private final Color dragColor;
  private final Color handleActiveColor;

  private final Unit startUnit;
  private final Unit endUnit;
  private final Unit targetUnit;
  private final int startUnitLabelW;
  private final int startUnitLabelH;
  private final int endUnitLabelW;
  private final int endUnitLabelH;

  //----Control members-----
  private boolean atDragRegion; // Flag to note if mouse is moved to drag region
  private boolean isHandleDragged; //Flag to determine if handle was dragged


  //-----New Data Added By Oliver
  //flag which determines if the handle can be dragged outside the bounds of the numberline
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

  private Line2D handleGuide;

  private final float slope;

  private final Dimension screen;

  private final Point2D.Float startLoc;
  private final Point2D.Float targetLoc;
  private final Point2D.Float handleLoc;
  private final Point2D.Float endLoc;
  private Color currentHandleColor;
  private final Color fontColor = Color.LIGHT_GRAY;
  private final Font displayFont;
  private boolean adjustChange = true;

  /**
   * Class Member Graphic Objects for holding graphics that change.
   * -------------------------------------------------------------
   * handleLine is the line for the handle which can be moved
   * dragLine is the line which is drawn to the handle
   * extendLine is the line horizontal on the base of the number line
   * endLine is the line that is the right bound on the base
   * handleGuideLine is a the guideline on the handle
   * startLine is the line that is the left bound on the base
   */
  private Line2D handleLine;
  private Line2D dragLine;
  private final Line2D extendLine;
  private final Line2D endLine;
  private Line2D handleGuideLine;
  private final Line2D startLine;
  private final int startX;  //startX is x coord where numberline is visually drawn
  private int currentDragX; // Holds x coord of the drag handle
  private final Point extendPoint;  // (x, y) coord of point on base where the drag line appears
  private final Stroke stroke; // the stroke to show line thickness
  private final String fontName; // the name of the Font to use
  private final Point2D.Float rightBoundPoint;

  private final boolean isEstimateTask;
  private final float widthPercentage; //holds percentage of base width to display

  public NumberLine(int width, int height, int thickness, Unit startU, Unit endU, Unit targetU,
                    Color baseColor, Color dragColor, Color handleColor, String font,
                    boolean estimateTask, int m, boolean[] kwb, char handleAlignment,
                    boolean[] shLabels) {

    //----- added by Oliver
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

    screen.height -= (m * 2); //take margins out of either side
    screen.width -= (m * 2); //take margins out of either side

    startPoint = getStartPoint();
    rightBoundPoint = getRightBound();
    slope = getNumLineSlope();

    setRandBaseWidth(); // Set base width as the older version of numberline did
    setExtendPoint();

    leftBoundHigh = getHighPoint(baseHeight, startPoint);
    rightBoundHigh = getHighPoint(baseHeight, extendPoint2D);

    Point2D.Float guideLeftLow = getLowPoint(baseHeight / 2, startPoint);
    Point2D.Float guideLeftHigh = getHighPoint(baseHeight + lineThickness, startPoint);

    Point2D.Float guideRightLow = getLowPoint(baseHeight / 2, extendPoint2D);
    Point2D.Float guideRightHigh = getHighPoint(baseHeight + lineThickness, extendPoint2D);

    leftGuide = new Line2D.Float(guideLeftLow.x,  guideLeftLow.y,
                                 guideLeftHigh.x, guideLeftHigh.y);

    rightGuide = new Line2D.Float(guideRightLow.x,  guideRightLow.y,
                                  guideRightHigh.x, guideLeftHigh.y);

    extendLine = new Line2D.Float(startPoint, extendPoint2D);

    startLine = new Line2D.Float(leftBoundHigh.x, leftBoundHigh.y + lineThickness * 3,
                                 startPoint.x,    startPoint.y);

    endLine = new Line2D.Float(rightBoundHigh.x, rightBoundHigh.y + lineThickness * 3,
                               extendPoint2D.x,  extendPoint2D.y);

    if (handleAlignment == 'L') {
      handleStartPoint = startPoint;
    } else if (handleAlignment == 'R') {
      handleStartPoint = extendPoint2D;
    } else if (handleAlignment == 'X') {
      //randomly choose a side for the handle to start on
      Random gen = new Random();
      int temp = gen.nextInt(10);
      //the random number was less than five the handle starts under the startPoint
      if (temp <= 5) {
        handleStartPoint = startPoint;
      } else {
        //random number greater than five handle starts under extendPoint
        handleStartPoint = extendPoint2D;
      }
    }

    currentDragPoint = handleStartPoint;

    handleHigh = getHighPoint(baseHeight, handleStartPoint);
    handleLow = handleStartPoint;
    handleLine = new Line2D.Float(handleLow, handleHigh);
    handleLine.setLine(handleHigh.x,  handleHigh.y + lineThickness * 3,
                       handleLow.x,   handleLow.y);

    guideHandleHigh = getHighPoint(baseHeight - lineThickness, handleStartPoint);
    guideHandleLow = getLowPoint(baseHeight / 2, handleStartPoint);
    handleGuide = new Line2D.Float(guideHandleLow, guideHandleHigh);

    extendPoint = new Point();
    extendPoint.x = (int) guideRightLow.getX();
    extendPoint.y = (int) guideRightLow.getY();

    endLoc = new Point2D.Float();
    endLoc.x = (float) guideRightLow.getX();
    endLoc.y = (float) guideRightLow.getY();

    startLoc = new Point2D.Float(); //holds the position for the label under startPoint
    startLoc.x = (float) guideLeftLow.getX();
    startLoc.y = (float) guideLeftLow.getY();

    targetLoc = new Point2D.Float(); //holds the position for the label of the target
    targetLoc.x = startLoc.x;
    targetLoc.y = startLoc.y;

    startUnitLabelW = 0;
    startUnitLabelH = 0;
    endUnitLabelW = 0;
    endUnitLabelH = 0;

    startUnit = startU;
    endUnit = endU;
    targetUnit = targetU;


    stroke = new BasicStroke(lineThickness);  //create a stroke object from the line thickness
    handlePadding = lineThickness + 15;        //Padding size of region around the handle

    atDragRegion = false;

    setBaseLeftPadding();

    this.baseColor = baseColor;
    this.dragColor = dragColor;
    currentHandleColor = this.baseColor;

    handleActiveColor = handleColor;
    fontName = font;
    displayFont = new Font(fontName, Font.BOLD, 12);

    startX = basePaddingLeft;

    if (isEstimateTask) {
      isHandleDragged = true; //handle not used in estimation
      currentDragPoint = getTargetPoint();

      handleHigh = getHighPoint(baseHeight / 2, currentDragPoint);
      handleLow = currentDragPoint;
      handleLine.setLine(handleHigh.x, handleHigh.y + lineThickness * 3,
          handleLow.x, handleLow.y);

      guideHandleHigh = getHighPoint(baseHeight - lineThickness, currentDragPoint);
      guideHandleLow = getLowPoint(baseHeight / 2, currentDragPoint);
      handleGuide = new Line2D.Float(guideHandleLow, guideHandleHigh);
    } else {
      isHandleDragged = false;
    }

    handleLoc = new Point2D.Float(); //holds the position for the label under the handle
    handleLoc.x = (float) guideHandleLow.getX();
    handleLoc.y = (float) guideHandleLow.getY();

    linePanel = new NumberLinePanel();
    linePanel.addMouseMotionListener(this);
    linePanel.addMouseListener(this);

    if (isEstimateTask) {
      handleHigh = getHighPoint(baseHeight, currentDragPoint);
      handleLow = currentDragPoint;
      guideHandleLow = getLowPoint(baseHeight / 2, currentDragPoint);
      guideHandleHigh = getHighPoint(baseHeight + lineThickness, currentDragPoint);
      handleLoc.x = (float) guideHandleLow.getX();
      handleLoc.y = (float) guideHandleLow.getY();
      linePanel.updateDragLine();
      currentHandleColor = handleActiveColor;
    }
  }

  private void setExtendPoint() {
    extendPoint2D = new Point2D.Float((float) startPoint.getX() + baseWidth,
        (float) (startPoint.getY()));
  }

  private boolean checkBounds(Unit unit1, Unit unit2) {
    return unit1.toDouble() > unit2.toDouble();
  }

  private Point2D.Float getTargetPoint() {
    currentHandleColor = dragColor;
    return getTargetSpecial();
  }

  private Point2D.Float getTargetSpecial() {
    boolean outsideBounds;
    double startToTarget;
    Point2D.Float p;

    if (startUnit.toDouble() > endUnit.toDouble()) {
      outsideBounds = checkBounds(endUnit, targetUnit) || checkBounds(targetUnit, startUnit);

      startToTarget = targetUnit.toDouble() - endUnit.toDouble();
      startToTarget /= getLengthPerUnit();


      p = new Point2D.Float((float) extendPoint2D.getX() - (float) startToTarget,
          (float) extendPoint2D.getY());
    } else {
      outsideBounds = checkBounds(targetUnit, endUnit) || checkBounds(startUnit, targetUnit);

      startToTarget = targetUnit.toDouble() - startUnit.toDouble();
      startToTarget /= getLengthPerUnit();

      p = new Point2D.Float((float) startPoint.getX() + (float) startToTarget,
          (float) startPoint.getY());
    }

    if (outsideBounds) {
      dragLine = new Line2D.Float(extendPoint2D, p);
    }

    return p;
  }

  /**
   * Return the number line panel.
   */
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

  public double getTargetUnit() {
    return targetUnit.toDouble();
  }


  public double getEndUnit() {
    return endUnit.toDouble();
  }

  public double getStartUnit() {
    return startUnit.toDouble();
  }

  private double getLengthPerUnit() {
    double lpUnit;
    double s = startUnit.toDouble();
    double e = endUnit.toDouble();
    double diff = Math.abs(s - e);

    lpUnit = diff / baseWidth;
    return lpUnit;
  }

  /**
   * Return the unit length of the number line.
   */
  public double getUnitLength() {

    /* CHANGE THIS TO: a
     *
     * pixelLow: pixels between end unit and start unit
     * pixelHigh:
     *
     */
    double length;
    if (startUnit.toDouble() < endUnit.toDouble()) {
      length = currentDragPoint.getX() - startPoint.getX();
      length *= getLengthPerUnit();

      return startUnit.toDouble() + length;
    } else {
      length = currentDragPoint.getX() - extendPoint2D.getX();
      length *= getLengthPerUnit();
      length = -length;

      return endUnit.toDouble() + length;
    }
  }

  public double getUnitLength(String userResp) {
    return parseUnitString(userResp);
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

  public boolean isHandleDragged() {
    return isHandleDragged;
  }

  private void setRandBaseWidth() {
    int newWidth = (int) (baseWidth * widthPercentage);

    if (newWidth != baseWidth && newWidth >= lineThickness) {
      baseWidth = newWidth;
    }
  }

  private boolean cursorInBoundingBox(Line2D handle, int cursorX, int cursorY) {
    Rectangle2D box = handle.getBounds2D();

    box.setRect((int) (box.getMinX()   - (handlePadding / 2)),
                (int) (box.getMinY()   - (handlePadding / 2)),
                (int) (box.getWidth()  + handlePadding),
                (int) (box.getHeight() + handlePadding));

    return box.contains(cursorX, cursorY);
  }

  public void mouseClicked(MouseEvent e) { }

  public void mousePressed(MouseEvent e) { }

  public void mouseReleased(MouseEvent e) { }

  public void mouseEntered(MouseEvent e) { }

  public void mouseExited(MouseEvent e) { }

  /**
   * Update the location of the drag handle when the mouse is moved if it is a production task.
   * @param e   MouseEvent
   */
  public void mouseDragged(MouseEvent e) {
    if (isEstimateTask || !atDragRegion) {
      return;
    }

    if (keepWithinBounds[0] && keepWithinBounds[1]) {
      //both are true handle is bound by both bounds
      if (e.getX() > extendPoint2D.getX()) {
        currentDragPoint = new Point2D.Float((float) extendPoint2D.getX(),
            (float) extendPoint2D.getY());
      } else if (e.getX() < startPoint.getX()) {
        currentDragPoint = new Point2D.Float((float) startPoint.getX(),
            (float) startPoint.getY());
      } else {
        currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
      }
    } else if (keepWithinBounds[0]) {
      if (e.getX() < startPoint.getX()) {
        currentDragPoint = new Point2D.Float((float) startPoint.getX(),
            (float) startPoint.getY());
      } else {
        currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
      }
    } else {
      if (e.getX() > extendPoint2D.getX()) {
        currentDragPoint = new Point2D.Float((float) extendPoint2D.getX(),
            (float) extendPoint2D.getY());
      } else {
        currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
      }
    }

    //check to make sure line does not extend past right bound
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
   * @param e   Mouse Event.
   */
  public void mouseMoved(MouseEvent e) {
    if (isEstimateTask) {
      return;
    }

    if (cursorInBoundingBox(handleLine, e.getX(), e.getY())) {
      linePanel.setLineColor(handleLine, handleActiveColor);
      currentHandleColor = handleActiveColor;
      atDragRegion = true;
    } else if (handleHigh.getX() == leftBoundHigh.getX()
        || handleHigh.getX() == rightBoundHigh.getX()) {
      linePanel.setLineColor(handleLine, baseColor);
      currentHandleColor = baseColor;
      atDragRegion = false;
    } else {
      linePanel.setLineColor(handleLine, dragColor);
      linePanel.setLineColor(handleGuideLine, dragColor);
      currentHandleColor = dragColor;
      atDragRegion = false;
    }
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

    float x = (float) (centerScreen.x + ((screen.width / 2) * Math.cos(r))); //x = h + a * cos(t)
    float y = (float) (centerScreen.y + ((screen.height / 2) * Math.sin(r))); //y = k + b * sin(t)

    return new Point2D.Float(x, y);
  }

  private Point2D.Float getRightBound() {
    float x;
    float y;

    // TODO: Fix this to avoid hardcoded degrees.
    double r = 360;
    r = Math.toRadians(r);

    x = (float) (centerScreen.x + ((screen.width / 2) * Math.cos(r))); //x = h + a * cos(t)
    y = (float) (centerScreen.y + ((screen.height / 2) * Math.sin(r))); //y = k + b * sin(t)
    return new Point2D.Float(x, y);
  }

  class NumberLinePanel extends JPanel {
    private Line2D currentLine;
    private Color currentLineColor;

    NumberLinePanel() {
      super(null);
      setBackground(Color.BLACK);
    }

    void setLineColor(Line2D line, Color c) {
      currentLine = line;
      currentLineColor = c;
      this.repaint();
    }

    void updateDragLine() {
      dragLine = new Line2D.Float(extendPoint2D, currentDragPoint);

      handleGuide.setLine(guideHandleLow, guideHandleHigh);

      handleLine.setLine(handleHigh.x,  handleHigh.y + lineThickness * 3,
                         handleLow.x,   handleLow.y);

      this.repaint();
    }

    //method that draws the base for the numberline that is drawn along a ellispe
    private void drawBaseCircle(Graphics2D g) {
      g.setColor(baseColor);
      g.setStroke(stroke);

      g.draw(extendLine);
      g.draw(endLine);
      g.draw(startLine);
    }

    private void adjustChange(FontMetrics fm, String s, String t) {
      int startUnitLength = fm.stringWidth(s);
      int targetUnitLength = fm.stringWidth(t);
      int fontHeight = fm.getHeight();

      startLoc.x  = startLoc.x  - startUnitLength;
      targetLoc.x = targetLoc.x - targetUnitLength;
      startLoc.y  = startLoc.y  + fontHeight;
      targetLoc.y = targetLoc.y + (fontHeight * 2);
      endLoc.y    = endLoc.y    + fontHeight;
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
     * Overrides this panel's paintComponent method.
     * NOTE: The class is a subclass of JPanel
     * Necessary to ensure current graphics are
     * redrawn on panel when panel is refreshed
     */
    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D graphics = (Graphics2D) g;
      graphics.setStroke(stroke);
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


      if (dragLine != null) {
        graphics.setColor(dragColor);
        graphics.draw(dragLine);
      }

      drawBaseCircle(graphics);

      graphics.setColor(currentHandleColor);
      graphics.draw(handleLine);

      if (currentLine != null) {
        graphics.setStroke(stroke);
        graphics.setColor(currentLineColor);
        graphics.draw(currentLine);
        currentLine = null;
      }

      graphics.setStroke(new BasicStroke(1));
      graphics.setColor(Color.RED);
      graphics.draw(leftGuide);
      graphics.draw(rightGuide);
      graphics.setColor(currentHandleColor);
      graphics.draw(handleGuide);

      displayLabels(graphics);
    }
  }
}