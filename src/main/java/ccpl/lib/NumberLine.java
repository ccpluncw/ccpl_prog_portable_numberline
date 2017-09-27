package ccpl.lib;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/*******************
 * Written by Kyle Holt
 * lkh3273@uncw.edu
 * October-November 2009
 *******************/

public class NumberLine implements MouseMotionListener, MouseListener {

  private static final boolean isDebugOn = false;

  NumberLinePanel linePanel;
  //---Pixel members-------
  private final int baseHeight; //Default overall baseHeight
  private final int lineThickness;
  private int baseWidth;
  private final int handleBounds;  //Padding size of region around the handle
  private int basePaddingLeft; //Amount of padding applied to experiment within the panel that contains it
  private final int basePaddingTop;
  private final int guideLinePadding; //Pixel amount used to extend the handle guideline beyond the handle

  //---Color members-----
  private Color baseColor;
  private Color dragColor;
  private Color bkgColor = Color.BLACK;
  private Color handleActiveColor;
  private Color guideColor = Color.RED;

  //---Unit members------
  private Fraction startUnitFract, endUnitFract, targetUnitFract;

  private Unit startUnit, endUnit, targetUnit;
  private int sUnitLabelW, sUnitLabelH, eUnitLabelW, eUnitLabelH;

  //----Control members-----
  private boolean atDragRegion; //Flag to note if mouse is moved to drag region and needs to respond to a drag
  private boolean shrinkBase = false; //Flag to support future feature of shrinking the base when the line is extended passed the panel boundary
  private boolean isHandleDragged; //Flag to determine if handle was dragged
  private boolean showFullBaseScale;
  private boolean showScale;


  //-----New Data Added By Oliver
  private boolean keepWithinBounds[] = new boolean[2]; //flag which determines if the handle can be dragged outside the bounds of the numberline
  private boolean showHideLabels[] = new boolean[4];
  private char locationChar; //this character 'l', 'r', or 'x' determines the location of the numberline on the screen
  private Point2D.Float startPoint, extendPoint2D, centerScreen, currentDragPoint, leftBoundHigh, rightBoundHigh, handleHigh, handleStartPoint,
      leftBoundLow, rightBoundLow, handleLow, guideLeftLow, guideLeftHigh, guideRightLow, guideRightHigh, guideHandleHigh, guideHandleLow;
  private int degreeOfLine, marginSize;
  private Line2D leftGuide, rightGuide, handleGuide;
  private float slope, perpSlope;
  private Dimension screen;
  private Point2D.Float startLoc, targetLoc, handleLoc, endLoc;
  private double actualBaseWidth; //this is the width of the line drawn from left bound to right bound
  private Color currentHandleColor;
  private Color fontColor = Color.LIGHT_GRAY;
  private Font dispfont;
  private int textBuff = 20;//distance text from the numline
  private String handleLabel;
  private boolean adjustChange = true;
  private boolean centerHandle = true;

  /*************************************************
   * Class Member Graphic Objects for holding graphics that change
   * -------------------------------------------------------------
   * handleLine is the line for the handle which can be moved
   * dragLine is the line which is drawn to the handle
   * extendLine is the line horizontal on the base of the number line
   * endLine is the line that is the right bound on the base
   * handleGuideLine is a the guideline on the handle
   * startLine is the line that is the left bound on the base
   *************************************************/
  private Line2D handleLine, dragLine, extendLine, endLine, handleGuideLine, startLine;
  private final int startX;  //startX is x coord where numberline is visually drawn
  private final int widthOffset;
  private int currentDragX; // Holds x coord of the drag handle
  private Point extendPoint;  //Holds x and y coord of point on base where the drag line appears
  private Stroke stroke; //holds the stroke to show line thickness
  private String fontName; //holds the name of the Font to use
  private final int linepad; //used in drawing base
  private String unitLabel;
  private Point2D.Float rightBoundPoint;
//    private boolean boundToExterior;


  private final boolean isEstimateTask;
  private float widthPercentage; //holds percentage of base width to display

  private Line2D fixationLine;

  public NumberLine(int width, int height, int thickness, Unit startU, Unit endU, Unit targetU,
                    Color bColor, Color dColor, Color hColor, String font, boolean estimateTask, int d,
                    int m, boolean[] kwb, char _c, boolean[] shLabels, String hl, String ul) { //add font color here

    //----- added by Oliver
    degreeOfLine = d;
    marginSize = m;
    keepWithinBounds = kwb;
    showHideLabels = shLabels;
    locationChar = _c;
    baseWidth = width;
    widthPercentage = 1;
    baseHeight = height;
    lineThickness = thickness;
    handleLabel = hl;
    unitLabel = ul;

    Toolkit tk = Toolkit.getDefaultToolkit();
    screen = tk.getScreenSize();
    centerScreen = new Point2D.Float(screen.width / 2, screen.height / 2);
    screen.height -= (marginSize * 2); //take margins out of either side
    screen.width -= (marginSize * 2); //take margins out of either side

    startPoint = getStartPoint();
    rightBoundPoint = getRightBound();
    slope = getNumLineSlope();

    setRandBaseWidth(); //this method is called to set base width as the older version of numberline did
    setExtendPoint();

    leftBoundHigh = getHighPoint(baseHeight, startPoint);
    rightBoundHigh = getHighPoint(baseHeight, extendPoint2D);
    leftBoundLow = getLowPoint(10, startPoint);
    rightBoundLow = getLowPoint(10, extendPoint2D);


    guideLeftLow = getLowPoint(baseHeight / 2, startPoint);
    guideLeftHigh = getHighPoint(baseHeight + lineThickness, startPoint);

    guideRightLow = getLowPoint(baseHeight / 2, extendPoint2D);
    guideRightHigh = getHighPoint(baseHeight + lineThickness, extendPoint2D);

    leftGuide = new Line2D.Float(guideLeftLow, guideLeftHigh);
    rightGuide = new Line2D.Float(guideRightLow, guideRightHigh);


    extendLine = new Line2D.Float(startPoint, extendPoint2D);
    startLine = new Line2D.Float(leftBoundHigh, startPoint);
    endLine = new Line2D.Float(rightBoundHigh, extendPoint2D);
    fixationLine = new Line2D.Float(leftBoundHigh, startPoint);

    //the handle is on the left side of the numberline or underneath the startPoint located on the ellispe
    if (locationChar == 'L') {
      handleStartPoint = startPoint;
    }

    else if (locationChar == 'R') {
      handleStartPoint = extendPoint2D;
    }

    //randomly choose a side for the handle to start on
    else if (locationChar == 'X') {
      Random gen = new Random();
      int temp = gen.nextInt(10);
      //the random number was less than five the handle starts under the startPoint
      if (temp <= 5) {
        handleStartPoint = startPoint;
      }
      //random number greater than five handle starts under extendPoint
      else {
        handleStartPoint = extendPoint2D;
      }
    }

    currentDragPoint = handleStartPoint;
    handleHigh = getHighPoint(baseHeight, handleStartPoint);
    handleLow = handleStartPoint;
    handleLine = new Line2D.Float(handleLow, handleHigh);

    guideHandleHigh = getHighPoint(baseHeight + lineThickness, handleStartPoint);
    guideHandleLow = getLowPoint(baseHeight / 2, handleStartPoint);
    handleGuide = new Line2D.Float(guideHandleLow, guideHandleHigh);

    extendPoint = new Point();
    //extendPoint.x = (int) rightBoundLow.getX();
    //extendPoint.y = (int) rightBoundLow.getY() + 15;
    extendPoint.x = (int) guideRightLow.getX();
    extendPoint.y = (int) guideRightLow.getY();

    endLoc = new Point2D.Float();
    endLoc.x = (float) guideRightLow.getX();
    endLoc.y = (float) guideRightLow.getY();

    startLoc = new Point2D.Float(); //holds the position for the label under startPoint
    //startLoc.x = (int) leftBoundLow.getX();
    //startLoc.y = (int) leftBoundLow.getY() + 15;
    startLoc.x = (float) guideLeftLow.getX();
    startLoc.y = (float) guideLeftLow.getY();

    targetLoc = new Point2D.Float(); //holds the position for the label of the target
    //targetLoc.x = startLoc.x;
    //targetLoc.y = startLoc.y + 25;
    targetLoc.x = startLoc.x;
    targetLoc.y = startLoc.y;


    String fract_temp[] = new String[2];
    sUnitLabelW = 0;
    sUnitLabelH = 0;
    eUnitLabelW = 0;
    eUnitLabelH = 0;

    startUnit = startU;
    endUnit = endU;
    targetUnit = targetU;

    isEstimateTask = estimateTask;

    stroke = new BasicStroke(lineThickness); //create a stroke object from the line thickness
    handleBounds = lineThickness + 15;  //Padding size of region around the handle

    basePaddingTop = lineThickness * 2;
    guideLinePadding = lineThickness * 2;
    atDragRegion = false;

    switch (startUnit.getType()) {
      case FRACT:
        fract_temp = startU.getValue().split("/");
        startUnitFract = new Fraction(Integer.parseInt(fract_temp[0].trim()), Integer.parseInt(fract_temp[1].trim()));
        break;
      case ODDS:
        fract_temp = startU.getValue().split("in");
        startUnitFract = new Fraction(Integer.parseInt(fract_temp[0].trim()), Integer.parseInt(fract_temp[1].trim()));
    }

    switch (endUnit.getType()) {
      case FRACT:
        //fract_temp = endU.getValue().split("/");
        endUnitFract = new Fraction(endU.getValue());
        break;
      case ODDS:
        fract_temp = endU.getValue().split("in");
        endUnitFract = new Fraction(Integer.parseInt(fract_temp[0].trim()), Integer.parseInt(fract_temp[1].trim()));
    }

    switch (targetUnit.getType()) {
      case FRACT:
        fract_temp = targetU.getValue().split("/");
        targetUnitFract = new Fraction(Integer.parseInt(fract_temp[0].trim()), Integer.parseInt(fract_temp[1].trim()));
        break;
      case ODDS:
        fract_temp = targetU.getValue().split("in");
        targetUnitFract = new Fraction(Integer.parseInt(fract_temp[0].trim()), Integer.parseInt(fract_temp[1].trim()));
    }

    setBaseLeftPadding();

    baseColor = bColor;
    dragColor = dColor;
    currentHandleColor = baseColor;

    handleActiveColor = hColor;
    fontName = font;
    dispfont = new Font(fontName, Font.BOLD, 12);


    //showFullBaseScale = fullBaseScale;

    //extendPoint.x = basePaddingLeft;

    //extendPoint.y = basePaddingTop+baseHeight;

    startX = basePaddingLeft;

        /*
        handleLine = new Line2D.Float(extendPoint.x, basePaddingTop+baseHeight, extendPoint.x, basePaddingTop);
        dragLine =  new Line2D.Float(extendPoint.x, extendPoint.y, extendPoint.x, extendPoint.y);
        handleGuideLine = new Line2D.Float(extendPoint.x, extendPoint.y-baseHeight-guideLinePadding, extendPoint.x, extendPoint.y+guideLinePadding);
*/

    linepad = lineThickness;
    widthOffset = (int) (basePaddingLeft - startX);
/*
        if(!isEstimateTask){
            isHandleDragged = false;
            currentDragX = extendPoint.x;
        }else{
            isHandleDragged = true; //handle is not to be dragged with an estimate task
            currentDragX = getTargetPixelLength();
        }
        *
        */

    if (isEstimateTask) {
      isHandleDragged = true; //handle not used in estimation
      currentDragPoint = getTargetPoint();
      handleHigh = getHighPoint(baseHeight, currentDragPoint);
      handleLow = currentDragPoint;
      handleLine = new Line2D.Float(handleLow, handleHigh);

      guideHandleHigh = getHighPoint(baseHeight + lineThickness, currentDragPoint);
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

    //if(dispScale)
    //displayUnitScale(fontName);
    //showScale = dispScale;

    // = new Line2D.Float(startX, basePaddingTop, startX, basePaddingTop+baseHeight-lineThickness);

    if (isDebugOn) {
      System.out.println("start unit: " + startUnit.toDouble());
      System.out.println("end unit: " + endUnit.toDouble());
      System.out.println("target unit: " + targetUnit.toDouble());
      System.out.println("degree: " + degreeOfLine);
      for (int i = 0; i < showHideLabels.length; i++) {
        if (!showHideLabels[i]) {
          System.out.println("label hidden number: " + i);
        }
      }
      for (int i = 0; i < keepWithinBounds.length; i++) {
        if (keepWithinBounds[i]) {
          System.out.println("bound by number: " + i);
        }
      }
      System.out.println("getX calc: " + getCorrespondingX(getCorrespondingY(50)));
      System.out.println("actualX: " + 50);
      System.out.println("-------------------");
    }
  }

  private void setExtendPoint() {
    if (degreeOfLine > 0 && degreeOfLine < 90) {
      double alpha = Math.toRadians(degreeOfLine);
      double _x;

      _x = Math.cos(alpha) * baseWidth;

      extendPoint2D = new Point2D.Float((float) (startPoint.getX() - _x), getCorrespondingY((float) (startPoint.getX() - _x)));
    } else if (degreeOfLine > 90 && degreeOfLine < 180) {
      double alpha = 180 - degreeOfLine;
      alpha = Math.toRadians(alpha);
      double _x, _y;
      _x = Math.cos(alpha) * baseWidth;

      extendPoint2D = new Point2D.Float((float) (startPoint.getX() + _x), getCorrespondingY((float) (startPoint.getX() + _x)));
    } else if (degreeOfLine > 180 && degreeOfLine < 270) {
      double alpha, theta = degreeOfLine - 180;
      alpha = 90 - theta;
      double _x;
      alpha = Math.toRadians(alpha);

      _x = Math.sin(alpha) * baseWidth;
      extendPoint2D = new Point2D.Float((float) (startPoint.getX() + _x), getCorrespondingY((float) (startPoint.getX() + _x)));
    } else if (degreeOfLine > 270 && degreeOfLine < 360) {
      double alpha = 180 - (degreeOfLine - 180);
      alpha = Math.toRadians(alpha);

      double _x = Math.cos(alpha) * baseWidth;
      extendPoint2D = new Point2D.Float((float) (startPoint.getX() - _x), getCorrespondingY((float) (startPoint.getX() - _x)));
    } else if (degreeOfLine == 90) {
      extendPoint2D = new Point2D.Float((float) startPoint.getX(), (float) (startPoint.getY() - baseWidth));
    } else if (degreeOfLine == 270) {
      extendPoint2D = new Point2D.Float((float) startPoint.getX(), (float) (startPoint.getY() + baseWidth));
    } else if (degreeOfLine == 180) {
      extendPoint2D = new Point2D.Float((float) startPoint.getX() + baseWidth, (float) (startPoint.getY()));
    } else if (degreeOfLine == 0 || degreeOfLine == 360) {
      extendPoint2D = new Point2D.Float((float) startPoint.getX() - baseWidth, (float) (startPoint.getY()));
    }
  }

  private Point2D.Float getTargetPoint() {
    currentHandleColor = dragColor;
    if (degreeOfLine > 0 && degreeOfLine < 90) {
      return getTargetPoint1();
    } else if (degreeOfLine > 90 && degreeOfLine < 180) {
      return getTargetPoint2();
    } else if (degreeOfLine > 180 && degreeOfLine < 270) {
      return getTargetPoint3();
    } else if (degreeOfLine > 270 && degreeOfLine < 360) {
      return getTargetPoint4();
    } else if (degreeOfLine == 0 || degreeOfLine == 360 || degreeOfLine == 180 || degreeOfLine == 90 || degreeOfLine == 270) {
      return getTargetSpecial();
    } else {
      System.out.println("error getTargetPoint()");
      return new Point2D.Float();
    }

  }

  private Point2D.Float getTargetPoint1() {
    Point2D.Float p = new Point2D.Float();
    double alpha;
    double startToTarget;
    double _x;
    boolean outsideBounds = false; //indicates if target is outside bounds of original numberline

    if (startUnit.toDouble() < endUnit.toDouble()) {
      if (targetUnit.toDouble() > startUnit.toDouble()) {

        if (targetUnit.toDouble() > endUnit.toDouble()) {
          outsideBounds = true;
          //    currentHandleColor = dragColor;
        }
        //else currentHandleColor = baseColor;

        alpha = degreeOfLine;
        alpha = Math.toRadians(alpha);
        startToTarget = targetUnit.toDouble() - startUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = -(Math.cos(alpha) * startToTarget);
      } else if (targetUnit.toDouble() < startUnit.toDouble()) {
        outsideBounds = true;
        //currentHandleColor = dragColor;
        alpha = degreeOfLine;
        alpha = Math.toRadians(alpha);
        startToTarget = startUnit.toDouble() - targetUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = (Math.cos(alpha) * startToTarget);
      } else {
        return startPoint;//target and start are equal
      }

      p = new Point2D.Float((float) (startPoint.getX() + _x), getCorrespondingY((float) (startPoint.getX() + _x)));
    } else if (startUnit.toDouble() > endUnit.toDouble()) {
      if (targetUnit.toDouble() > endUnit.toDouble()) {
        if (targetUnit.toDouble() > startUnit.toDouble()) {
          outsideBounds = true;
          //    currentHandleColor = dragColor;
        }
        //else currentHandleColor = baseColor;
        alpha = degreeOfLine;
        alpha = Math.toRadians(alpha);
        startToTarget = targetUnit.toDouble() - endUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = (Math.cos(alpha) * startToTarget);
      } else if (targetUnit.toDouble() < endUnit.toDouble()) {
        outsideBounds = true;
        //currentHandleColor = dragColor;
        alpha = degreeOfLine;
        alpha = Math.toRadians(alpha);
        startToTarget = endUnit.toDouble() - targetUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = -(Math.cos(alpha) * startToTarget);
      } else {
        return extendPoint2D; //target and extend are equal
      }

      p = new Point2D.Float((float) (extendPoint2D.getX() + _x), getCorrespondingY((float) (extendPoint2D.getX() + _x)));

    }

    if (outsideBounds) {
      dragLine = new Line2D.Float(extendPoint2D, p);
    }
    return p;
  }//method 1

  private Point2D.Float getTargetPoint2() {
    Point2D.Float p = new Point2D.Float();
    double alpha;
    double startToTarget;
    double _x;
    boolean outsideBounds = false; //indicates if target is outside bounds of original numberline

    if (startUnit.toDouble() < endUnit.toDouble()) {
      if (targetUnit.toDouble() > startUnit.toDouble()) {
        if (targetUnit.toDouble() > endUnit.toDouble()) {
          outsideBounds = true;
          //currentHandleColor = dragColor;
        }
        //else currentHandleColor = baseColor;
        alpha = 180 - degreeOfLine;
        alpha = Math.toRadians(alpha);
        startToTarget = targetUnit.toDouble() - startUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = Math.cos(alpha) * startToTarget;
      } else if (targetUnit.toDouble() < startUnit.toDouble()) {
        outsideBounds = true;
        //currentHandleColor = dragColor;
        alpha = 180 - degreeOfLine;
        alpha = Math.toRadians(alpha);
        startToTarget = startUnit.toDouble() - targetUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = -(Math.cos(alpha) * startToTarget);
      } else {
        return startPoint;//target and start are equal
      }

      p = new Point2D.Float((float) (startPoint.getX() + _x), getCorrespondingY((float) (startPoint.getX() + _x)));
    } else if (startUnit.toDouble() > endUnit.toDouble()) {
      if (targetUnit.toDouble() > endUnit.toDouble()) {
        if (targetUnit.toDouble() > startUnit.toDouble()) {
          outsideBounds = true;
          //currentHandleColor = dragColor;
        }
        //else currentHandleColor = baseColor;
        alpha = 180 - degreeOfLine;
        alpha = Math.toRadians(alpha);
        startToTarget = targetUnit.toDouble() - endUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = -(Math.cos(alpha) * startToTarget);
      } else if (targetUnit.toDouble() < endUnit.toDouble()) {
        outsideBounds = true;
        //currentHandleColor = dragColor;
        alpha = 180 - degreeOfLine;
        alpha = Math.toRadians(alpha);
        startToTarget = endUnit.toDouble() - targetUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = Math.cos(alpha) * startToTarget;
      } else {
        return extendPoint2D; //target and extend are equal
      }

      p = new Point2D.Float((float) (extendPoint2D.getX() + _x), getCorrespondingY((float) (extendPoint2D.getX() + _x)));

    }//method 2

    if (outsideBounds) {
      dragLine = new Line2D.Float(extendPoint2D, p);
    }
    return p;
  }//method 2

  private Point2D.Float getTargetPoint3() {
    Point2D.Float p = new Point2D.Float();
    double alpha;
    double startToTarget;
    double _x;
    boolean outsideBounds = false; //indicates if target is outside bounds of original numberline

    if (startUnit.toDouble() < endUnit.toDouble()) {
      if (targetUnit.toDouble() > startUnit.toDouble()) {
        if (targetUnit.toDouble() > endUnit.toDouble()) {
          outsideBounds = true;
          //currentHandleColor = dragColor;
        }
        //else currentHandleColor = baseColor;
        alpha = degreeOfLine - 180;
        alpha = Math.toRadians(alpha);
        startToTarget = targetUnit.toDouble() - startUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = Math.cos(alpha) * startToTarget;
      } else if (targetUnit.toDouble() < startUnit.toDouble()) {
        outsideBounds = true;
        //currentHandleColor = dragColor;
        alpha = degreeOfLine - 180;
        alpha = Math.toRadians(alpha);
        startToTarget = startUnit.toDouble() - targetUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = -(Math.cos(alpha) * startToTarget);
      } else {
        return startPoint;//target and start are equal
      }

      p = new Point2D.Float((float) (startPoint.getX() + _x), getCorrespondingY((float) (startPoint.getX() + _x)));
    } else if (startUnit.toDouble() > endUnit.toDouble()) {
      if (targetUnit.toDouble() > endUnit.toDouble()) {
        if (targetUnit.toDouble() > startUnit.toDouble()) {
          outsideBounds = true;
          //currentHandleColor = dragColor;
        }
        //else currentHandleColor = baseColor;
        alpha = degreeOfLine - 180;
        alpha = Math.toRadians(alpha);
        startToTarget = targetUnit.toDouble() - endUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = -(Math.cos(alpha) * startToTarget);
      } else if (targetUnit.toDouble() < endUnit.toDouble()) {
        outsideBounds = true;
        //currentHandleColor = dragColor;
        alpha = degreeOfLine - 180;
        alpha = Math.toRadians(alpha);
        startToTarget = endUnit.toDouble() - targetUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = Math.cos(alpha) * startToTarget;
      } else {
        return extendPoint2D; //target and extend are equal
      }

      p = new Point2D.Float((float) (extendPoint2D.getX() + _x), getCorrespondingY((float) (extendPoint2D.getX() + _x)));

    }//method 3

    if (outsideBounds) {
      dragLine = new Line2D.Float(extendPoint2D, p);
    }
    return p;
  }//method 3

  private Point2D.Float getTargetPoint4() {
    Point2D.Float p = new Point2D.Float();
    double alpha, theta;
    double startToTarget;
    double _x;
    boolean outsideBounds = false; //indicates if target is outside bounds of original numberline

    if (startUnit.toDouble() < endUnit.toDouble()) {
      if (targetUnit.toDouble() > startUnit.toDouble()) {
        if (targetUnit.toDouble() > endUnit.toDouble()) {
          outsideBounds = true;
          //currentHandleColor = dragColor;
        }
        //else currentHandleColor = baseColor;
        theta = degreeOfLine - 180;
        alpha = 180 - theta;
        alpha = Math.toRadians(alpha);
        startToTarget = targetUnit.toDouble() - startUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = -(Math.cos(alpha) * startToTarget);
      } else if (targetUnit.toDouble() < startUnit.toDouble()) {
        outsideBounds = true;
        //currentHandleColor = dragColor;
        theta = degreeOfLine - 180;
        alpha = 180 - theta;
        alpha = Math.toRadians(alpha);
        startToTarget = startUnit.toDouble() - targetUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = (Math.cos(alpha) * startToTarget);
      } else {
        return startPoint;//target and start are equal
      }

      p = new Point2D.Float((float) (startPoint.getX() + _x), getCorrespondingY((float) (startPoint.getX() + _x)));
    } else if (startUnit.toDouble() > endUnit.toDouble()) {
      if (targetUnit.toDouble() > endUnit.toDouble()) {
        if (targetUnit.toDouble() > startUnit.toDouble()) {
          outsideBounds = true;
          //currentHandleColor = dragColor;
        }
        //else currentHandleColor = baseColor;
        theta = degreeOfLine - 180;
        alpha = 180 - theta;
        alpha = Math.toRadians(alpha);
        startToTarget = targetUnit.toDouble() - endUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = (Math.cos(alpha) * startToTarget);
      } else if (targetUnit.toDouble() < endUnit.toDouble()) {
        outsideBounds = true;
        //currentHandleColor = dragColor;
        theta = degreeOfLine - 180;
        alpha = 180 - theta;
        alpha = Math.toRadians(alpha);
        startToTarget = endUnit.toDouble() - targetUnit.toDouble();
        startToTarget /= getLengthPerUnit();
        _x = -(Math.cos(alpha) * startToTarget);
      } else {
        return extendPoint2D; //target and extend are equal
      }

      p = new Point2D.Float((float) (extendPoint2D.getX() + _x), getCorrespondingY((float) (extendPoint2D.getX() + _x)));

    }//

    if (outsideBounds) {
      dragLine = new Line2D.Float(extendPoint2D, p);
    }
    return p;
  }//method 4

  private Point2D.Float getTargetSpecial() {
    boolean outsideBounds = false;
    double startToTarget;
    double _x, _y;
    Point2D.Float p = new Point2D.Float();
    if (startUnit.toDouble() > endUnit.toDouble()) {
      if (targetUnit.toDouble() < endUnit.toDouble() || targetUnit.toDouble() > startUnit.toDouble()) {
        outsideBounds = true;
        //currentHandleColor = dragColor;
      }
      //else currentHandleColor = baseColor;

      startToTarget = targetUnit.toDouble() - endUnit.toDouble();
      startToTarget /= getLengthPerUnit();

      if (degreeOfLine == 0 || degreeOfLine == 360) {
        p = new Point2D.Float((float) extendPoint2D.getX() + (float) startToTarget, (float) extendPoint2D.getY());
      } else if (degreeOfLine == 180) {
        p = new Point2D.Float((float) extendPoint2D.getX() - (float) startToTarget, (float) extendPoint2D.getY());
      } else if (degreeOfLine == 90) {
        p = new Point2D.Float((float) extendPoint2D.getX(), (float) (extendPoint2D.getY() + startToTarget));
      } else if (degreeOfLine == 270) {
        p = new Point2D.Float((float) extendPoint2D.getX(), (float) (extendPoint2D.getY() - startToTarget));
      } else {
        System.out.println("error in targetSpecial");
        p = new Point2D.Float();
      }

    } else {
      if (targetUnit.toDouble() > endUnit.toDouble() || targetUnit.toDouble() < startUnit.toDouble()) {
        outsideBounds = true;
        //currentHandleColor = dragColor;
      }
      //else currentHandleColor = baseColor;

      startToTarget = targetUnit.toDouble() - startUnit.toDouble();
      startToTarget /= getLengthPerUnit();

      if (degreeOfLine == 0 || degreeOfLine == 360) {
        p = new Point2D.Float((float) (startPoint.getX() - startToTarget), (float) startPoint.getY());
      } else if (degreeOfLine == 180) {
        p = new Point2D.Float((float) startPoint.getX() + (float) startToTarget, (float) startPoint.getY());
      } else if (degreeOfLine == 90) {
        p = new Point2D.Float((float) startPoint.getX(), (float) (startPoint.getY() - startToTarget));
      } else if (degreeOfLine == 270) {
        p = new Point2D.Float((float) startPoint.getX(), (float) (startPoint.getY() + startToTarget));
      } else {
        System.out.println("error in targetSpecial");
        p = new Point2D.Float();
      }
    }
    if (outsideBounds) {
      dragLine = new Line2D.Float(extendPoint2D, p);
    }
    return p;
  }

  public JPanel getPanel() {
    if (isEstimateTask) {
      int w = startX + sUnitLabelW + eUnitLabelW + lineThickness * 2 + 10;
      int h = extendPoint.y + Math.max(sUnitLabelH, sUnitLabelW) + eUnitLabelH + 20;
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


  public String getTargetUnitString() {
    return targetUnit.getValue();
  }

  public double getEndUnit() {
    return endUnit.toDouble();
  }

  public String getEndUnitString() {
    return endUnit.getValue();
  }

  public double getStartUnit() {
    return startUnit.toDouble();
  }

  public String getStartUnitString() {
    return startUnit.getValue();
  }

  public int getBaseWidth() {
    return baseWidth;
  }

  private int getBoundaryX() {
    int rightBoundaryX;
    rightBoundaryX = linePanel.getWidth() - lineThickness;
    return rightBoundaryX;
  }

  private int getTargetPixelLength() {
    int length;
    double sUnit = getStartUnit(), target = getTargetUnit();
    length = (int) ((getPixelsPerUnit() * (target - sUnit)) + startX + widthOffset);
    return length;
  }

  private int getPixelLength() {
    int length;
    if (currentDragX - startX - widthOffset <= 0) {
      length = 0;
    } else {
      length = currentDragX - startX - widthOffset;
    }
    return length;
  }

  private double getPixelsPerUnit() {
    double pixelsPerUnit;
    double sUnit = 0.0, eUnit = 0.0;

    sUnit = startUnit.toDouble();
    eUnit = endUnit.toDouble();
    pixelsPerUnit = baseWidth / (eUnit - sUnit);

    return pixelsPerUnit;
  }

  private double getDistance(Point2D.Float x, Point2D.Float y) {
    if (degreeOfLine > 0 && degreeOfLine < 90) {
      return getDistance1(x, y);
    } else if (degreeOfLine > 90 && degreeOfLine < 180) {
      return getDistance2(x, y);
    } else if (degreeOfLine > 180 && degreeOfLine < 270) {
      return getDistance3(x, y);
    } else if (degreeOfLine > 270 && degreeOfLine < 360) {
      return getDistance4(x, y);
    } else {
      return -1;
    }
  }

  private double getDistance1(Point2D.Float x, Point2D.Float y) {
    double distance = -1.0;
    double alpha = degreeOfLine;
    alpha = Math.toRadians(alpha);
    double _x = Math.abs(y.getX() - x.getX());
    distance = _x / Math.cos(alpha);

    return distance;
  }

  private double getDistance2(Point2D.Float x, Point2D.Float y) {
    double distance = -1.0;
    double alpha = 180 - degreeOfLine;
    alpha = Math.toRadians(alpha);
    double _x = Math.abs(y.getX() - x.getX()); //abs to ensure positive length
    distance = _x / (Math.cos(alpha));

    return distance;
  }

  private double getDistance3(Point2D.Float x, Point2D.Float y) {
    double distance = -1.0;
    double alpha = degreeOfLine - 180;
    alpha = Math.toRadians(alpha);
    double _x = Math.abs(y.getX() - x.getX());
    distance = _x / Math.cos(alpha);

    return distance;
  }

  private double getDistance4(Point2D.Float x, Point2D.Float y) {
    double distance = -1.0;
    double alpha, theta = degreeOfLine - 180;
    alpha = 180 - theta;
    alpha = Math.toRadians(alpha);
    double _x = Math.abs(y.getX() - x.getX());
    distance = _x / Math.cos(alpha);

    return distance;
  }

  private double getLengthPerUnit() {
    double lpUnit = 0.0;
    double s = startUnit.toDouble();
    double e = endUnit.toDouble();
    double diff = Math.abs(s - e); //because either left or right can be the high end of the numberline abs insures a positive length

    lpUnit = diff / baseWidth;
    return lpUnit;
  }

  public double getUnitLength() {
    if (degreeOfLine > 0 && degreeOfLine < 90) {
      return getUnitLength1();
    } else if (degreeOfLine > 90 && degreeOfLine < 180) {
      return getUnitLength2();
    } else if (degreeOfLine > 180 && degreeOfLine < 270) {
      return getUnitLength3();
    } else if (degreeOfLine > 270 && degreeOfLine < 360) {
      return getUnitLength4();
    } else if (degreeOfLine == 0 || degreeOfLine == 360 || degreeOfLine == 180) {
      double length;
      if (startUnit.toDouble() < endUnit.toDouble()) {
        length = currentDragPoint.getX() - startPoint.getX();
        length *= getLengthPerUnit();
        if ((degreeOfLine == 0 || degreeOfLine == 360)) {
          length = -length;
        }

        return startUnit.toDouble() + length;
      } else {
        length = currentDragPoint.getX() - extendPoint2D.getX();
        length *= getLengthPerUnit();
        //if(extendPoint2D.getX() > currentDragPoint.getX() && (degreeOfLine == 0 || degreeOfLine == 360))
        //   length = - length;
        if (degreeOfLine == 180) {
          length = -length;
        }

        return endUnit.toDouble() + length;
      }
    } else if (degreeOfLine == 90 || degreeOfLine == 270) {
      double length;
      if (startUnit.toDouble() < endUnit.toDouble()) {
        length = currentDragPoint.getY() - startPoint.getY();
        length *= getLengthPerUnit();
        if (degreeOfLine == 90) {
          length = -length;
        }

        return startUnit.toDouble() + length;
      } else {
        length = currentDragPoint.getY() - extendPoint2D.getY();
        length *= getLengthPerUnit();
        if (degreeOfLine == 270) {
          length = -length;
        }

        return endUnit.toDouble() + length;
      }
    } else {
      return -1;
    }
  }

  private double getUnitLength1() {
    double length = 0.0;

    if (startUnit.toDouble() < endUnit.toDouble()) {
      length = getDistance(startPoint, currentDragPoint);
      length = length * getLengthPerUnit();
      if (startPoint.getX() < currentDragPoint.getX()) {
        length = -length;
      }

      return startUnit.toDouble() + length;
    } else if (startUnit.toDouble() > endUnit.toDouble()) {
      length = getDistance(extendPoint2D, currentDragPoint);
      length *= getLengthPerUnit();
      if (extendPoint2D.getX() > currentDragPoint.getX()) {
        length = -length;
      }

      return endUnit.toDouble() + length;
    } else {
      return -1.0;
    }
  }

  private double getUnitLength2() {
    double length = 0.0;

    if (startUnit.toDouble() < endUnit.toDouble()) {
      length = getDistance(startPoint, currentDragPoint);
      length = length * getLengthPerUnit();
      if (startPoint.getX() > currentDragPoint.getX()) //quad II specific
      {
        length = -length;
      }

      return startUnit.toDouble() + length;
    } else if (startUnit.toDouble() > endUnit.toDouble()) {
      length = getDistance(extendPoint2D, currentDragPoint);
      length *= getLengthPerUnit();
      if (extendPoint2D.getX() < currentDragPoint.getX()) //quad III specific
      {
        length = -length;
      }

      return endUnit.toDouble() + length;
    } else {
      return -1.0;
    }
  }

  private double getUnitLength3() {
    double length = 0.0;

    if (startUnit.toDouble() < endUnit.toDouble()) {
      length = getDistance(startPoint, currentDragPoint);
      length = length * getLengthPerUnit();
      if (startPoint.getX() > currentDragPoint.getX()) //quad II specific
      {
        length = -length;
      }

      return startUnit.toDouble() + length;
    } else if (startUnit.toDouble() > endUnit.toDouble()) {
      length = getDistance(extendPoint2D, currentDragPoint);
      length *= getLengthPerUnit();
      if (extendPoint2D.getX() < currentDragPoint.getX()) //quad III specific
      {
        length = -length;
      }

      return endUnit.toDouble() + length;
    } else {
      return -1.0;
    }
  }//method 3

  private double getUnitLength4() {
    double length = 0.0;

    if (startUnit.toDouble() < endUnit.toDouble()) {
      length = getDistance(startPoint, currentDragPoint);
      length = length * getLengthPerUnit();
      if (startPoint.getX() < currentDragPoint.getX()) //quad II specific
      {
        length = -length;
      }

      return startUnit.toDouble() + length;
    } else if (startUnit.toDouble() > endUnit.toDouble()) {
      length = getDistance(extendPoint2D, currentDragPoint);
      length *= getLengthPerUnit();
      if (extendPoint2D.getX() > currentDragPoint.getX()) //quad III specific
      {
        length = -length;
      }

      return endUnit.toDouble() + length;
    } else {
      return -1.0;
    }
  }
    /*
    public double getUnitLength(){
        double sUnit = startUnit.toDouble(), units = 0.0;
        if(!isEstimateTask){
            try{
                units = (getPixelLength()/getPixelsPerUnit()) + sUnit;
            }catch(ArithmeticException arithmeticException){
                System.out.println("Cannot divide by zero");
                units = 0.0;
            }
        }else
            units = getTargetUnit();
        return units;
    }
    * used in old version
    */

  public double getUnitLength(String userResp) {
    return parseUnitString(userResp);
  }

  public double getUnitError(boolean inPercent) {
    double error = 0.0;
    if (inPercent == true) {
      error = getUnitLength() / getTargetUnit();
    } else {
      error = getUnitLength() - getTargetUnit();
    }
    return error;
  }

  public double getUnitError(boolean inPercent, String userRespUnitLength) {
    double error = 0.0;
    if (inPercent == true) {
      error = parseUnitString(userRespUnitLength) / getTargetUnit();
    } else {
      error = parseUnitString(userRespUnitLength) - getTargetUnit();
    }
    return error;
  }

  public Line2D getFixationLine() {
    return fixationLine;
  }

  private double parseUnitString(String unitLength) {
    double unitLen = 0.0;
    String[] temp;
    if (startUnit.getType() == Unit.UNITTYPE.FRACT) {
      temp = unitLength.split("/");
      unitLen = (Double.parseDouble(temp[0].trim())) / (Integer.parseInt(temp[1].trim()));
    } else if (startUnit.getType() == Unit.UNITTYPE.ODDS) {
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


  /*****************************
   * First two are supporting methods used with shrink base
   * Currently not used
   ****************************
   private int getVirtualX(){
   int virtualX = this.getParent().getWidth() - (this.getX()+1);
   return virtualX;
   }

   private float getPercentageWidth(int currX){
   int boundaryX = getBoundaryX();
   if(boundaryX < currX){
   float shrinkFactor = (float)(currX - boundaryX)/baseWidth;
   //System.out.println("CurrX: "+ currX + "BX : "+ boundaryX +" Shrink Factor: " + shrinkFactor);
   return 1 - shrinkFactor;
   }else{
   return 1;
   }
   }
   ********************************
   *---Incomplete Method---
   * Purpose to shrink the base according to the line's
   * expansion past the boundary of the panel
   ******************************************
   private void shrinkBase(Graphics2D g2, int cursorX){
   int virtualX = getVirtualX();
   if(virtualX > cursorX){
   widthPercentage = getPercentageWidth(cursorX);
   //System.out.println("WP: " +widthPercentage);
   drawBase(g2, widthPercentage);
   currentDragX = cursorX;
   }else{ //Farthest point user can drag
   //System.out.println("VWidth: " +virtualX);
   currentDragX = cursorX;
   }
   }
   ****************************************************/


  private void drawBase(Graphics2D g2, float widthPercentage) {
    int newWidth = (int) (baseWidth * widthPercentage);

    if (newWidth != baseWidth && newWidth >= lineThickness) {
      baseWidth = newWidth;
      extendPoint.x = basePaddingLeft + newWidth;
    }

    g2.setStroke(stroke);
    g2.setColor(baseColor);

    Line2D verticalLeft = new Line2D.Float(startX, basePaddingTop, startX, basePaddingTop + baseHeight - lineThickness);
    //Line2D horizontal = new Line2D.Float(startX, basePaddingTop+baseHeight, basePaddingLeft+baseWidth+offset, basePaddingTop+baseHeight);
    Line2D horizontal = new Line2D.Float(startX, basePaddingTop + baseHeight, startX + baseWidth, basePaddingTop + baseHeight);
    //Line2D verticalRight = new Line2D.Float(basePaddingLeft+baseWidth+offset, basePaddingTop+baseHeight-lineThickness, basePaddingLeft+baseWidth+offset, basePaddingTop);
    Line2D verticalRight = new Line2D.Float(startX + baseWidth, basePaddingTop + baseHeight - lineThickness, startX + baseWidth, basePaddingTop);


    extendLine = verticalLeft;
    endLine = verticalRight;


    g2.draw(verticalLeft);
    g2.draw(horizontal);
    g2.draw(verticalRight);

    if (lineThickness > 2) {
      g2.setColor(guideColor);
      if (lineThickness % 2 == 1) //ODDS Thickness
      {
        g2.setStroke(new BasicStroke(1));
      } else if (lineThickness % 2 == 0) //EVEN Thickness
      {
        g2.setStroke(new BasicStroke(2));
      }

      Line2D verticalGuideLeft = new Line2D.Float(basePaddingLeft, basePaddingTop - linepad, basePaddingLeft, basePaddingTop + baseHeight + linepad);
      //Line2D verticalGuideRight = new Line2D.Float(basePaddingLeft+baseWidth+offset, basePaddingTop+baseHeight+linepad, basePaddingLeft+baseWidth+offset, basePaddingTop-linepad);
      Line2D verticalGuideRight = new Line2D.Float(startX + baseWidth, basePaddingTop + baseHeight + linepad, startX + baseWidth, basePaddingTop - linepad);

      g2.draw(verticalGuideLeft);
      g2.draw(verticalGuideRight);
    }
  }

  private boolean cursorInBoundingBox(int cursorX, int cursorY) {
        /*
      Rectangle box = handleLine.getBounds();
      box.setBounds((int)box.getMinX()-(handleBounds/2), (int)box.getMinY()-(handleBounds/2), (int)box.getWidth()+handleBounds , (int)box.getHeight()+handleBounds);

      if((box.getMaxX() > cursorX && box.getMinX() < cursorX)
              && (box.getMaxY() > cursorY && box.getMinY() < cursorY)){
          return true;
      }else{
          return false;
      }
      *
      */
    java.awt.geom.Rectangle2D box = handleLine.getBounds2D();
    box.setRect((int) (box.getMinX() - (handleBounds / 2)), (int) (box.getMinY() - (handleBounds / 2)), (int) (box.getWidth() + handleBounds), (int) (box.getHeight() + handleBounds));
    if (box.contains(cursorX, cursorY)) {
      return true;
    }
    return false;
  }

  private boolean cursorInBoundingBigBox(int cursorX, int cursorY) {
    java.awt.geom.Rectangle2D box = handleLine.getBounds2D();
    box.setRect((int) (box.getMinX() - (handleBounds)), (int) (box.getMinY() - (handleBounds)), (int) (box.getWidth() + handleBounds * 2), (int) (box.getHeight() + handleBounds * 2));
    if (box.contains(cursorX, cursorY)) {
      return true;
    }
    return false;
  }

  private void displayUnitScale(String fontName) {

    Color foregroundColor = Color.LIGHT_GRAY;
    RotateLabel baseUnitLabel, startUnitLabel, endUnitLabel, targetUnitLabel;

    Font font = new Font(fontName, Font.BOLD, 12);

    int yPad = lineThickness + 20;

    boolean regular;//regular if numline is on left side of screen + and - are changed accordingly
    if (degreeOfLine > 90 && degreeOfLine < 270) {
      regular = true;
    } else {
      regular = false;
    }
        /*
            //if(!showFullBaseScale){
                baseUnitLabel = endUnit.getLabel(font);
                eUnitLabelW = baseUnitLabel.getWidth();
                eUnitLabelH = baseUnitLabel.getHeight();
                //--------End Unit Number---------------
                if(endUnit.getType() == Unit.UNITTYPE.DECI){
                    baseUnitLabel.setLocation(extendPoint);
    //this is the old location                //baseUnitLabel.setLocation((extendPoint.x+(baseWidth/2)-(baseUnitLabel.getWidth()/2)), (extendPoint.y+yPad));
                    linePanel.add(baseUnitLabel);
                }else if(endUnit.getType() == Unit.UNITTYPE.FRACT){
                    JPanel fractPanel = endUnitFract.getFractionPanel(font, foregroundColor);
                    fractPanel.setLocation(extendPoint);
         //this is the old location           //fractPanel.setLocation((extendPoint.x+(baseWidth/2)-(fractPanel.getWidth()/2)), (extendPoint.y+yPad-3));
                    linePanel.add(fractPanel);
                }else if(endUnit.getType() == Unit.UNITTYPE.ODDS){
                    baseUnitLabel.setLocation(extendPoint);
     //this is the old location               //baseUnitLabel.setLocation((extendPoint.x+(baseWidth/2)-(baseUnitLabel.getWidth()/2)), (extendPoint.y+yPad));
                    linePanel.add(baseUnitLabel);
                }else{
                    baseUnitLabel.setLocation(extendPoint);
      //this is the old location              //baseUnitLabel.setLocation((extendPoint.x+(baseWidth/2)-(baseUnitLabel.getWidth()/2)), (extendPoint.y+yPad));
                    linePanel.add(baseUnitLabel);
                }
          //  }else{
          *
          */
    //startUnitLabel = startUnit.getLabel(font);
    //endUnitLabel = endUnit.getLabel(font);
    startUnitLabel = new RotateLabel(startUnit.toString(), degreeOfLine, font);
    endUnitLabel = new RotateLabel(endUnit.toString(), degreeOfLine, font);

    sUnitLabelW = startUnitLabel.getWidth();
    sUnitLabelH = startUnitLabel.getHeight();
    eUnitLabelW = endUnitLabel.getWidth();
    eUnitLabelH = endUnitLabel.getHeight();
    //--------Full Base Unit (includes start unit and base unit)---------------
    int endUnitX = extendPoint.x + baseWidth;

    //Case Unit is a decimal
    if (endUnit.getType() == Unit.UNITTYPE.DECI) {
      if (showHideLabels[0]) {
        //startUnitLabel.setLocation(startLoc.x - startUnitLabel.getWidth(), startLoc.y);
        //startUnitLabel.setLocation((startX-startUnitLabel.getWidth()), extendPoint.y+yPad); old location
        linePanel.add(startUnitLabel);
      } //the start label is displayed
      if (showHideLabels[1]) {
        endUnitLabel.setLocation(extendPoint.x + endUnitLabel.getWidth(), extendPoint.y);
        //endUnitLabel.setLocation(endUnitX, extendPoint.y+yPad); old location
        linePanel.add(endUnitLabel);
      }//teh end label is displayed

      //Case Unit is a fraction
    } else if (endUnit.getType() == Unit.UNITTYPE.FRACT) {
      if (showHideLabels[0]) {
        JPanel startFractPanel = startUnitFract.getFractionPanel(font, foregroundColor);
        //startFractPanel.setLocation(startLoc.x - startFractPanel.getWidth(), startLoc.y);
        //startFractPanel.setLocation(startX-(startFractPanel.getWidth()), (extendPoint.y+yPad-3)); old location
        linePanel.add(startFractPanel);
      }//the start label is displayed
      if (showHideLabels[1]) {
        JPanel endFractPanel = endUnitFract.getFractionPanel(font, foregroundColor);
        endFractPanel.setLocation(extendPoint.x + endUnitLabel.getWidth(), extendPoint.y);
        //endFractPanel.setLocation(endUnitX, (extendPoint.y+yPad-3)); old location
        linePanel.add(endFractPanel);
      }// the end unit is displayed
    }

    //Case Unit is ODDS
    else if (endUnit.getType() == Unit.UNITTYPE.ODDS) {
      if (showHideLabels[0]) {
        //startUnitLabel.setLocation(startLoc.x - startUnitLabel.getWidth(), startLoc.y);
        //startUnitLabel.setLocation((startX-startUnitLabel.getWidth()), extendPoint.y+yPad); old location
        linePanel.add(startUnitLabel);
      } //the start label is displayed
      if (showHideLabels[1]) {
        endUnitLabel.setLocation(extendPoint.x + endUnitLabel.getWidth(), extendPoint.y);
        //endUnitLabel.setLocation(endUnitX, extendPoint.y+yPad); old location
        linePanel.add(endUnitLabel);
      }//the end label is displayed

      //Case Default
    } else {
      if (showHideLabels[0]) {
        //if(regular)
        //startUnitLabel.setLocation(startLoc);//startLoc.x - startUnitLabel.getWidth(), startLoc.y);
        //else
        //startUnitLabel.setLocation(startLoc);//startLoc.x + startUnitLabel.getWidth(), startLoc.y);
        //startUnitLabel.setLocation((startX-startUnitLabel.getWidth()), extendPoint.y+yPad); old location
        linePanel.add(startUnitLabel);
      }//start unit is displayed
      if (showHideLabels[1]) {
        if (regular) {
          endUnitLabel.setLocation(extendPoint);//extendPoint.x + endUnitLabel.getWidth(), extendPoint.y);
        } else {
          endUnitLabel.setLocation(extendPoint);//extendPoint.x - endUnitLabel.getWidth(), extendPoint.y);
        }
        //endUnitLabel.setLocation(endUnitX, extendPoint.y+yPad); old location
        linePanel.add(endUnitLabel);
      }//the end unit is displayed
    }
    // }

    //Display target only if the task is not estimate
    if (!isEstimateTask) {
      yPad = lineThickness + 70;
      //targetUnitLabel = targetUnit.getLabel(font);
      targetUnitLabel = new RotateLabel(targetUnit.toString(), degreeOfLine, font);

      //---------Target Unit Number-------------
      if (showHideLabels[2]) {
        if (targetUnit.getType() == Unit.UNITTYPE.DECI) {
          //targetUnitLabel.setLocation(targetLoc.x - targetUnitLabel.getWidth(), targetLoc.y);
          //targetUnitLabel.setLocation((extendPoint.x+(baseWidth/2)-(targetUnitLabel.getWidth()/2)), (extendPoint.y+yPad));
          linePanel.add(targetUnitLabel);
        } else if (targetUnit.getType() == Unit.UNITTYPE.FRACT) {
          JPanel targetFractPanel = targetUnitFract.getFractionPanel(font, foregroundColor);
          //targetFractPanel.setLocation(targetLoc.x - targetFractPanel.getWidth(), targetLoc.y);
          //targetFractPanel.setLocation((extendPoint.x+(baseWidth/2)-(targetFractPanel.getWidth()/2)), (extendPoint.y+yPad));
          linePanel.add(targetFractPanel);
        } else if (targetUnit.getType() == Unit.UNITTYPE.ODDS) {
          //targetUnitLabel.setLocation(targetLoc.x - targetUnitLabel.getWidth(), targetLoc.y);
          //targetUnitLabel.setLocation((extendPoint.x+(baseWidth/2)-(targetUnitLabel.getWidth()/2)), (extendPoint.y+yPad));
          linePanel.add(targetUnitLabel);
        } else {
          if (regular)
            //targetUnitLabel.setLocation(targetLoc);//targetLoc.x - targetUnitLabel.getWidth(), targetLoc.y);
            //else
            //targetUnitLabel.setLocation(targetLoc);//targetLoc.x + targetUnitLabel.getWidth(), targetLoc.y);
            //targetUnitLabel.setLocation((extendPoint.x+(baseWidth/2)-(targetUnitLabel.getWidth()/2)), (extendPoint.y+yPad));
          {
            linePanel.add(targetUnitLabel);
          }
        }
      }//the target unit is displayed
    }
    linePanel.addMouseMotionListener(this);
  }//display unit scale

  public void mouseClicked(MouseEvent e) {
    //lfet blank
  }

  public void mousePressed(MouseEvent e) {
    if (!isEstimateTask && atDragRegion) {
      try {
        Robot robot = new Robot();
        //               robot.mouseMove((int)currentDragPoint.x, (int)currentDragPoint.y);
      } catch (AWTException ex) {
        Logger.getLogger(NumberLine.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
    lastMouse = null;
  }

  public void mouseEntered(MouseEvent e) {
    //left blank
  }

  public void mouseExited(MouseEvent e) {
    //left blank
  }

  private Point2D.Float lastMouse;

  public void mouseDragged2(MouseEvent e) {
    if (!isEstimateTask && atDragRegion && cursorInBoundingBigBox(e.getX(), e.getY())) {//should only be implemented in non - estimate tasks

      double deltaX, deltaY, theta = Math.atan(slope);
      if (lastMouse == null) {
        lastMouse = new Point2D.Float(e.getX(), e.getY());
      } else {
        deltaX = lastMouse.x - e.getX();
        deltaY = lastMouse.y - e.getY();
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
          deltaY = Math.tan(theta) * deltaX;
        } else {
          deltaX = deltaY / Math.tan(theta);
        }
        currentDragPoint.x -= deltaX;
        currentDragPoint.y -= deltaY;
        lastMouse.x = e.getX();
        lastMouse.y = e.getY();

      }

      handleHigh = getHighPoint(baseHeight, currentDragPoint);
      handleLow = currentDragPoint;
      guideHandleLow = getLowPoint(baseHeight / 2, currentDragPoint);
      guideHandleHigh = getHighPoint(baseHeight + lineThickness, currentDragPoint);
      handleLoc.x = (float) guideHandleLow.getX();
      handleLoc.y = (float) guideHandleLow.getY();
      centerHandle = true;
      isHandleDragged = true;

      if (!cursorInBoundingBox(e.getX(), e.getY())) {
        try {
          Robot robot = new Robot();
          //                   robot.mouseMove((int)currentDragPoint.x, (int)currentDragPoint.y);
        } catch (AWTException ex) {
          Logger.getLogger(NumberLine.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

      linePanel.updateDragLine();
    }
  }

  public void thisWillBeMouseDraggedWhenIAmFinished(MouseEvent e) {
    Float outBound = null, inBound = null;
    boolean vertical, regularX;

    if (degreeOfLine == 90 || degreeOfLine == 270) {
      vertical = true;
    } else {
      vertical = false;
    }

    if (degreeOfLine >= 90 && degreeOfLine < 270) {
      regularX = true;
    } else {
      regularX = false;
    }

    if (keepWithinBounds[0]) {
      outBound = new Float(startPoint.getX());
    }
    if (keepWithinBounds[1]) {
      inBound = new Float(extendPoint2D.getX());
    }

    if (!vertical) {


    } else if (vertical) {
      moveLineVertically(e.getX(), e.getY(), regularX);
    }
  }

  public void mouseDragged(MouseEvent e) {
    boolean regularX, regularY = false;
    boolean vertical;
    boolean flag = false; // if in flagged degrees area it is more accurate to move line according to change in y
    double _y;

    if ((degreeOfLine > 45 && degreeOfLine < 135) || (degreeOfLine > 225 && degreeOfLine < 315)) {
      flag = true;
    }
    if (degreeOfLine > 0 && degreeOfLine < 180) {
      regularY = true;
    }
    if (degreeOfLine >= 90 && degreeOfLine < 270) {
      regularX = true;
    } else {
      regularX = false;
    }
    if (degreeOfLine == 90 || degreeOfLine == 270) {
      vertical = true;
    } else {
      vertical = false;
    }
    if (!isEstimateTask) {
      if (atDragRegion) { //&& cursorInBoundingBigBox(e.getX(), e.getY())){
        if (!vertical) {
          if (keepWithinBounds[0] == false && keepWithinBounds[1] == false) {
            if (flag) {
              currentDragPoint = new Point2D.Float(getCorrespondingX(e.getY()), e.getY());
            } else {
              currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
            }
          }//both are false allow handle to move freely


          else if (keepWithinBounds[0] == true && keepWithinBounds[1] == true) {
            if (regularX) {
              if ((!flag && e.getX() > extendPoint2D.getX()) || (regularY && flag && e.getY() < extendPoint2D.getY())
                  || (!regularY && flag && e.getY() > extendPoint2D.getY())) {
                currentDragPoint = new Point2D.Float((float) extendPoint2D.getX(), (float) extendPoint2D.getY());
              } else if ((!flag && e.getX() < startPoint.getX()) || (regularY && flag && e.getY() > startPoint.getY())
                  || (!regularY && flag && e.getY() < startPoint.getY())) {
                currentDragPoint = new Point2D.Float((float) startPoint.getX(), (float) startPoint.getY());
              } else {
                if (flag) {
                  currentDragPoint = new Point2D.Float(getCorrespondingX(e.getY()), e.getY());
                } else {
                  currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
                }
              }
            } else {
              if ((!flag && e.getX() < extendPoint2D.getX()) || (regularY && flag && e.getY() < extendPoint2D.getY())
                  || (!regularY && flag && e.getY() > extendPoint2D.getY())) {
                currentDragPoint = new Point2D.Float((float) extendPoint2D.getX(), (float) extendPoint2D.getY());
              } else if ((!flag && e.getX() > startPoint.getX()) || (regularY && flag && e.getY() > startPoint.getY())
                  || (!regularY && flag && e.getY() < startPoint.getY())) {
                currentDragPoint = new Point2D.Float((float) startPoint.getX(), (float) startPoint.getY());
              } else {
                if (flag) {
                  currentDragPoint = new Point2D.Float(getCorrespondingX(e.getY()), e.getY());
                } else {
                  currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
                }
              }
            }
          }//both are true handle is bound by both bounds


          else if (keepWithinBounds[0] == true && keepWithinBounds[1] == false) {
            if (regularX) {
              if ((!flag && e.getX() < startPoint.getX()) || (regularY && flag && e.getY() > startPoint.getY())
                  || (!regularY && flag && e.getY() < startPoint.getY())) {
                currentDragPoint = new Point2D.Float((float) startPoint.getX(), (float) startPoint.getY());
              } else {
                if (flag) {
                  currentDragPoint = new Point2D.Float(getCorrespondingX(e.getY()), e.getY());
                } else {
                  currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
                }
              }
            } else {
              if ((!flag && e.getX() > startPoint.getX()) || (regularY && flag && e.getY() > startPoint.getY())
                  || (!regularY && flag && e.getY() < startPoint.getY())) {
                currentDragPoint = new Point2D.Float((float) startPoint.getX(), (float) startPoint.getY());
              } else {
                if (flag) {
                  currentDragPoint = new Point2D.Float(getCorrespondingX(e.getY()), e.getY());
                } else {
                  currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
                }
              }
            }
          }//bound by left handle and not the right


          else {
            if (regularX) {
              if ((!flag && e.getX() > extendPoint2D.getX()) || (regularY && flag && e.getY() < extendPoint2D.getY())
                  || (!regularY && flag && e.getY() > extendPoint2D.getY())) {
                currentDragPoint = new Point2D.Float((float) extendPoint2D.getX(), (float) extendPoint2D.getY());
              } else {
                if (flag) {
                  currentDragPoint = new Point2D.Float(getCorrespondingX(e.getY()), e.getY());
                } else {
                  currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
                }
              }
            } else {
              if ((!flag && e.getX() < extendPoint2D.getX()) || (regularY && flag && e.getY() < extendPoint2D.getY())
                  || (!regularY && flag && e.getY() > extendPoint2D.getY())) {
                currentDragPoint = new Point2D.Float((float) extendPoint2D.getX(), (float) extendPoint2D.getY());
              } else {
                if (flag) {
                  currentDragPoint = new Point2D.Float(getCorrespondingX(e.getY()), e.getY());
                } else {
                  currentDragPoint = new Point2D.Float(e.getX(), getCorrespondingY(e.getX()));
                }
              }
            }
          }//bound by the right and not the left

          //check to make sure line does not extend past right bound
          if (regularX && currentDragPoint.getX() > rightBoundPoint.getX()) {
            currentDragPoint = rightBoundPoint;
          } else if (!regularX && currentDragPoint.getX() < rightBoundPoint.getX()) {
            currentDragPoint = rightBoundPoint;
          }

          handleHigh = getHighPoint(baseHeight, currentDragPoint);
          handleLow = currentDragPoint;
          guideHandleLow = getLowPoint(baseHeight / 2, currentDragPoint);
          guideHandleHigh = getHighPoint(baseHeight + lineThickness, currentDragPoint);
          handleLoc.x = (float) guideHandleLow.getX();
          handleLoc.y = (float) guideHandleLow.getY();
          centerHandle = true;
          isHandleDragged = true;
          if (!cursorInBoundingBox(e.getX(), e.getY())) {
            try {
              Robot robot = new Robot();
//                           robot.mouseMove((int)currentDragPoint.x, (int)currentDragPoint.y);
            } catch (AWTException ex) {
              Logger.getLogger(NumberLine.class.getName()).log(Level.SEVERE, null, ex);
            }
          }

          linePanel.updateDragLine();
        } else { // line is vertical line must move vertically
          moveLineVertically(e.getX(), e.getY(), regularX);
        }

      }
    }
  }


  private void moveLineVertically(float x, float y, boolean regular) {
    if (keepWithinBounds[0] == false && keepWithinBounds[1] == false) {
      currentDragPoint = new Point2D.Float(startPoint.x, y);
    }
    if (keepWithinBounds[0] == true && keepWithinBounds[1] == true) {
      if (regular) {
        if (y <= startPoint.getY() && y >= extendPoint2D.getY()) {
          currentDragPoint = new Point2D.Float(startPoint.x, y);
        } else if (y > startPoint.getY()) {
          currentDragPoint = new Point2D.Float(startPoint.x, startPoint.y);
        } else if (y < extendPoint2D.getY()) {
          currentDragPoint = new Point2D.Float(extendPoint2D.x, extendPoint2D.y);
        }
      } else {
        if (y >= startPoint.getY() && y <= extendPoint2D.getY()) {
          currentDragPoint = new Point2D.Float(startPoint.x, y);
        } else if (y < startPoint.getY()) {
          currentDragPoint = new Point2D.Float(startPoint.x, startPoint.y);
        } else if (y > extendPoint2D.getY()) {
          currentDragPoint = new Point2D.Float(extendPoint2D.x, extendPoint2D.y);
        }
      }
    }//bound by both sides

    if (keepWithinBounds[0] == true && keepWithinBounds[1] == false) {
      if (regular) {
        if (y <= startPoint.getY()) {
          currentDragPoint = new Point2D.Float(startPoint.x, y);
        } else if (y > startPoint.getY()) {
          currentDragPoint = new Point2D.Float(startPoint.x, startPoint.y);
        }
      } else {
        if (y >= startPoint.getY()) {
          currentDragPoint = new Point2D.Float(startPoint.x, y);
        } else if (y < startPoint.getY()) {
          currentDragPoint = new Point2D.Float(startPoint.x, startPoint.y);
        }
      }
    }//bound by left only

    if (keepWithinBounds[0] == false && keepWithinBounds[1] == true) {
      if (regular) {
        if (y >= extendPoint2D.getY()) {
          currentDragPoint = new Point2D.Float(startPoint.x, y);
        } else if (y < extendPoint2D.getY()) {
          currentDragPoint = new Point2D.Float(extendPoint2D.x, extendPoint2D.y);
        }
      } else {
        if (y <= extendPoint2D.getY()) {
          currentDragPoint = new Point2D.Float(startPoint.x, y);
        } else if (y > extendPoint2D.getY()) {
          currentDragPoint = new Point2D.Float(extendPoint2D.x, extendPoint2D.y);
        }
      }
    }//bound by right only
    handleHigh = getHighPoint(baseHeight, currentDragPoint);
    handleLow = currentDragPoint;
    guideHandleLow = getLowPoint(baseHeight / 2, currentDragPoint);
    guideHandleHigh = getHighPoint(baseHeight + lineThickness, currentDragPoint);
    handleLoc.x = (float) guideHandleLow.getX();
    handleLoc.y = (float) guideHandleLow.getY();
    isHandleDragged = true;
    centerHandle = true;
    linePanel.updateDragLine();
  }

  public void mouseMoved(MouseEvent e) {
    if (!isEstimateTask) {
      int cX, cY;
      cX = e.getX();
      cY = e.getY();
       /*
       //Determine if mouse cursor is within the handle's region
       if(cursorInBoundingBox(cX, cY) == true){
          if((currentDragX <= extendPoint.x || currentDragX <= startX)  && extendLine != null){
                linePanel.setLineColor(extendLine, handleActiveColor);
          }else if(currentDragX == endLine.getX1()){
                linePanel.setLineColor(endLine, handleActiveColor);
          }else{
              if(atDragRegion == false)
                linePanel.drawHandle(handleActiveColor);
                //drawHandle(currG,currentDragX,handleActiveColor);
          }
          atDragRegion = true;
       }else{
          if(atDragRegion == true){
                linePanel.drawHandle(dragColor);
                //drawHandle(currG,currentDragX,dragColor);
          }
          atDragRegion = false;
       }
       }
       *
       */
      if (cursorInBoundingBox(cX, cY)) {
        linePanel.setLineColor(handleLine, handleActiveColor);
        currentHandleColor = handleActiveColor;
        atDragRegion = true;

      } else if (handleHigh.getX() == leftBoundHigh.getX() || handleHigh.getX() == rightBoundHigh.getX()) {
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
  }

  private Point2D.Float getHighPoint(int d, Point2D.Float p) {
    if (degreeOfLine > 0 && degreeOfLine < 90) {
      return getHighPointOne(d, p);
    }//case one
    else if (degreeOfLine > 90 && degreeOfLine < 180) {
      return getHighPointTwo(d, p);
    }//case two
    else if (degreeOfLine > 180 && degreeOfLine < 270) {
      return getHighPointThree(d, p);
    } else if (degreeOfLine > 270 && degreeOfLine < 360) {
      return getHighPointFour(d, p);
    } else if (degreeOfLine == 0 || degreeOfLine == 360) {
      return new Point2D.Float((float) p.getX(), (float) p.getY() - d);
    } else if (degreeOfLine == 90) {
      return new Point2D.Float((float) p.getX() + d, (float) p.getY());
    } else if (degreeOfLine == 180) {
      return new Point2D.Float((float) p.getX(), (float) p.getY() - d);
    } else if (degreeOfLine == 270) {
      return new Point2D.Float((float) p.getX() - d, (float) p.getY());
    } else {
      return new Point2D.Float(0, 0);
    }
  }

  private Point2D.Float getHighPointOne(int d, Point2D.Float p) {
    double _x, _y, r1;
    double alpha, theta;

    double y1 = Math.abs(centerScreen.getY() - startPoint.getY());
    double x1 = Math.abs(centerScreen.getX() - startPoint.getX());

    r1 = Math.sqrt(Math.pow(y1, 2) + Math.pow(x1, 2));
    theta = Math.toDegrees(Math.asin(y1 / r1));
    alpha = 90.0 - theta;
    alpha = Math.toRadians(alpha);

    _x = Math.cos(alpha) * d;
    _y = Math.sin(alpha) * d;


    return new Point2D.Float((float) (p.getX() + _x), (float) (p.getY() - _y));
  }

  private Point2D.Float getHighPointThree(int d, Point2D.Float p) {
    double x1, y1, r1, theta, alpha, _x, _y;
    x1 = Math.abs(centerScreen.getX() - startPoint.getX());
    y1 = Math.abs(centerScreen.getY() - startPoint.getY());
    r1 = Math.sqrt(Math.pow(y1, 2) + Math.pow(x1, 2));
    theta = Math.toDegrees(Math.asin(y1 / r1));
    alpha = 90 - theta;
    alpha = Math.toRadians(alpha);

    _x = Math.cos(alpha) * d;
    _y = Math.sin(alpha) * d;

    return new Point2D.Float((float) (p.getX() + _x), (float) (p.getY() - _y));
  }

  private Point2D.Float getHighPointFour(int d, Point2D.Float p) {
    double _x, _y;
    double alpha, theta;

    double y1 = Math.abs(centerScreen.getY() - extendPoint2D.getY());
    double x1 = Math.abs(centerScreen.getX() - extendPoint2D.getX());

    //alpha = degreeOfLine - 90;
    double r1 = Math.sqrt(Math.pow(y1, 2) + Math.pow(x1, 2));
    theta = Math.toDegrees(Math.asin(y1 / r1));
    alpha = 90.0 - theta;
    alpha = Math.toRadians(alpha);

    _x = Math.cos(alpha) * d;
    _y = Math.sin(alpha) * d;


    return new Point2D.Float((float) (p.getX() - _x), (float) (p.getY() - _y));
  }


  /*
     * Calculates the point when given a distance from the numberline such that a line can be drawn perpindicular to the numberline
     *
     */
  private Point2D.Float getHighPointTwo(int d, Point2D.Float p) {
    double x, y;
    double alpha;
    double r;
    int distance;

    double y1 = Math.abs(centerScreen.getY() - startPoint.getY());
    double x1 = Math.abs(centerScreen.getX() - startPoint.getX());

    //alpha = degreeOfLine - 90;
    double r1 = Math.sqrt(Math.pow(y1, 2) + Math.pow(x1, 2));
    double qw;
    qw = Math.toDegrees(Math.asin(y1 / r1));
    alpha = 180.0 - 90.0 - qw;
    r = Math.toRadians(alpha);

    distance = d;

    x = (Math.cos(r) * distance);
    y = (Math.sin(r) * distance);

    return new Point2D.Float((float) (p.getX() - x), (float) (p.getY() - y));

  }

  private Point2D.Float getLowPoint(int d, Point2D.Float p) {
    if (degreeOfLine > 0 && degreeOfLine < 90) {
      return getLowPointOne(d, p);
    }//case one
    else if (degreeOfLine > 90 && degreeOfLine < 180) {
      return getLowPointTwo(d, p);
    }//case two
    else if (degreeOfLine > 180 && degreeOfLine < 270) {
      return getLowPointThree(d, p);
    } else if (degreeOfLine > 270 && degreeOfLine < 360) {
      return getLowPointFour(d, p);
    } else if (degreeOfLine == 0 || degreeOfLine == 360) {
      return new Point2D.Float((float) p.getX(), (float) p.getY() + d);
    } else if (degreeOfLine == 90) {
      return new Point2D.Float((float) p.getX() - d, (float) p.getY());
    } else if (degreeOfLine == 180) {
      return new Point2D.Float((float) p.getX(), (float) p.getY() + d);
    } else if (degreeOfLine == 270) {
      return new Point2D.Float((float) p.getX() + d, (float) p.getY());
    } else {
      return new Point2D.Float(0, 0);
    }
  }

  private Point2D.Float getLowPointOne(int d, Point2D.Float p) {
    double _x, _y, r1;
    double alpha, theta;

    double y1 = Math.abs(centerScreen.getY() - startPoint.getY());
    double x1 = Math.abs(centerScreen.getX() - startPoint.getX());

    r1 = Math.sqrt(Math.pow(y1, 2) + Math.pow(x1, 2));
    theta = Math.toDegrees(Math.asin(y1 / r1));
    alpha = 180 - 90.0 - theta;
    alpha = Math.toRadians(alpha);

    _x = Math.cos(alpha) * d;
    _y = Math.sin(alpha) * d;


    return new Point2D.Float((float) (p.getX() - _x), (float) (p.getY() + _y));

  }

  private Point2D.Float getLowPointTwo(int d, Point2D.Float p) {
    double x, y;
    double alpha;
    double r;
    int distance;

    double y1 = Math.abs(centerScreen.getY() - startPoint.getY());
    double x1 = Math.abs(centerScreen.getX() - startPoint.getX());

    //alpha = degreeOfLine - 90;
    double r1 = Math.sqrt(Math.pow(y1, 2) + Math.pow(x1, 2));
    double qw;
    qw = Math.toDegrees(Math.asin(y1 / r1));
    alpha = 90.0 - qw;
    r = Math.toRadians(alpha);

    distance = d;

    x = (Math.cos(r) * distance);
    y = (Math.sin(r) * distance);

    return new Point2D.Float((float) (p.getX() + x), (float) (p.getY() + y));

  }

  private Point2D.Float getLowPointThree(int d, Point2D.Float p) {
    double _x, _y, r1, x1, y1;
    double alpha, theta;

    y1 = Math.abs(centerScreen.getY() - startPoint.getY());
    x1 = Math.abs(centerScreen.getX() - startPoint.getX());

    r1 = Math.sqrt(Math.pow(y1, 2) + Math.pow(x1, 2));

    theta = Math.toDegrees(Math.asin(y1 / r1));
    alpha = 90 - theta;
    alpha = Math.toRadians(alpha);

    _x = Math.cos(alpha) * d;
    _y = Math.sin(alpha) * d;

    return new Point2D.Float((float) (p.getX() - _x), (float) (p.getY() + _y));
  }

  private Point2D.Float getLowPointFour(int d, Point2D.Float p) {
    double _x, _y;
    double alpha, theta;

    double y1 = Math.abs(centerScreen.getY() - extendPoint2D.getY());
    double x1 = Math.abs(centerScreen.getX() - extendPoint2D.getX());

    //alpha = degreeOfLine - 90;
    double r1 = Math.sqrt(Math.pow(y1, 2) + Math.pow(x1, 2));
    theta = Math.toDegrees(Math.asin(y1 / r1));
    alpha = 90.0 - theta;
    alpha = Math.toRadians(alpha);

    _x = Math.cos(alpha) * d;
    _y = Math.sin(alpha) * d;


    return new Point2D.Float((float) (p.getX() + _x), (float) (p.getY() + _y));

  }

  /*
     * returns slope of lines perpindicular to the numberline
     */
  private float getPerpindicularSlope(float f) {
    return (float) (-Math.pow(f, -1));
  }

  /*
     * returns the slope of the numberline
     */
  private float getNumLineSlope() {
    return ((startPoint.y - centerScreen.y) / (startPoint.x - centerScreen.x));
  }//get slope

  /*This method when provided an x coordinate calculates the y value such that the point
     * (x,y) is on the numberline
     */
  private float getCorrespondingY(float x) {
    return ((slope * (x - startPoint.x)) + startPoint.y);
  }//correspondingX

  //this method does not work correctly
  private float getCorrespondingX(float y) {
    return ((y - startPoint.y) / slope) + startPoint.x;
  }

  private float parallelPoint(float x, Point2D.Float p) {
    return ((slope * (x - p.x)) + p.y);
  }

  private Point2D.Float getStartPoint() {
    float x, y;
    double r = Math.toRadians(degreeOfLine);

    x = (float) (centerScreen.x + ((screen.width / 2) * Math.cos(r))); //x = h + a * cos(t)
    y = (float) (centerScreen.y + ((screen.height / 2) * Math.sin(r))); //y = k + b * sin(t)


    return new Point2D.Float(x, y);
  }//getStartPoint

  private Point2D.Float getRightBound() {
    float x, y;
    double r = degreeOfLine + 180;
    r = Math.toRadians(r);

    x = (float) (centerScreen.x + ((screen.width / 2) * Math.cos(r))); //x = h + a * cos(t)
    y = (float) (centerScreen.y + ((screen.height / 2) * Math.sin(r))); //y = k + b * sin(t)
    return new Point2D.Float(x, y);
  }

  public class RotateLabel extends JLabel {
    private double rotateDegrees;
    private double correctedDegrees;
    private double alpha;

    public RotateLabel(int d) {
      super();
      rotateDegrees = d;
      rotateDegrees = Math.toRadians(rotateDegrees);
      setForeground(Color.LIGHT_GRAY);
    }

    public RotateLabel(String s, int d, Font f) {
      super(s);
      rotateDegrees = d;
      alpha = getRotation();
      setForeground(Color.LIGHT_GRAY);
      setFont(f);
      FontMetrics metrics = this.getFontMetrics(f);
      int w = metrics.stringWidth(s);
      int h = metrics.getHeight();
      setSize(w + 50, h + 50);
    }

    private double getRotation() {
      double a;
      if (rotateDegrees > 0 && rotateDegrees < 90) {
        correctedDegrees = rotateDegrees - 13;
        a = correctedDegrees;
        a = Math.toRadians(a);
      } else if (rotateDegrees > 90 && rotateDegrees < 180) {
        correctedDegrees = rotateDegrees + 15;
        a = 180 - correctedDegrees;
        a = -a;
        a = Math.toRadians(a);
      } else if (rotateDegrees > 180 && rotateDegrees < 270) {
        correctedDegrees = rotateDegrees - 15;
        a = correctedDegrees - 180;
        a = Math.toRadians(a);
      } else if (rotateDegrees > 270 && rotateDegrees < 360) {
        correctedDegrees = rotateDegrees + 15;
        a = 360 - correctedDegrees;
        a = -a;
        a = Math.toRadians(a);
      } else {
        a = -1.0;
        System.out.println("not supported yet");
      }

      return a;
    }

    @Override
    public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      g2.rotate(alpha, 25, 25);
      super.paintComponent(g2);

    }
  }

  public class NumberLinePanel extends JPanel {

    private boolean drawDragLine;
    private int dragLinePos;
    private Color currHandleColor;
    private Line2D currentLine;
    private Color currentLineColor;

    public NumberLinePanel() {
      super(null);
      setBackground(Color.BLACK);
      drawDragLine = false;
      dragLinePos = 0;
      currHandleColor = handleActiveColor;
    }

    public void setDragLine(int xPos) {
      drawDragLine = true;
      dragLinePos = xPos;
      this.repaint();
    }

    public void setLineColor(Line2D line, Color c) {
      currentLine = line;
      currentLineColor = c;
      this.repaint();
    }

    public void drawHandle(Color currCol) {
      currHandleColor = currCol;
      this.repaint();
    }

    private void drawHandle(Graphics2D g2, int endX, Color newColor) {
      //Erase the previous handleLine and handleGuideLine
      g2.setStroke(stroke);
      g2.setColor(bkgColor);
      g2.draw(handleLine);
      g2.draw(handleGuideLine);

      //Redraw drag Line
      //g2.setStroke(stroke);
      g2.setColor(dragColor);
      g2.draw(dragLine);

      g2.setColor(newColor);
      handleLine.setLine(endX, extendPoint.y - baseHeight, endX, extendPoint.y - lineThickness);
      g2.draw(handleLine);

      if (lineThickness > 2) {
        handleGuideLine.setLine(endX, extendPoint.y - baseHeight - guideLinePadding, endX, extendPoint.y + guideLinePadding);
        g2.setColor(guideColor);
        //Determine thickness of guideline based on overall lineThickness specified
        if (lineThickness % 2 == 1) { //ODDS Thickness
          g2.setStroke(new BasicStroke(1));
          g2.draw(handleGuideLine);
        } else { //EVEN Thickness
          g2.setStroke(new BasicStroke(2));
          g2.draw(handleGuideLine);
        }
      }
      drawBase(g2, widthPercentage);
    }

    private void updateDragLine() {
      dragLine = new Line2D.Float(extendPoint2D, currentDragPoint);
      handleGuide.setLine(guideHandleLow, guideHandleHigh);
      handleLine.setLine(handleHigh, handleLow);
      this.repaint();
    }

    private void drawTargetDragLine(Graphics2D g) {
      g.setColor(dragColor);
      dragLine.setLine(extendPoint.x + lineThickness, extendPoint.y, currentDragX, extendPoint.y);
      g.draw(dragLine);
    }

    private void drawDragLine(Graphics2D g, int endX) {
      g.setStroke(stroke);
      if (endX < currentDragX) {  //Handle is retracting
        //-----------Erase previous drag line-----------
        g.setColor(bkgColor);
        g.draw(dragLine);
      }
      g.setColor(dragColor);
      if (endX > (extendPoint.x + lineThickness)) {
        dragLine.setLine(extendPoint.x + lineThickness, extendPoint.y, endX, extendPoint.y);
        g.draw(dragLine);
      } else {
        dragLine.setLine(extendPoint.x, extendPoint.y, endX, extendPoint.y);
        g.draw(dragLine);
      }
      drawHandle(g, endX, handleActiveColor);
      currentDragX = endX;
    }

    //method that draws the base for the numberline that is drawn along a ellispe
    private void drawBaseCircle(Graphics2D g) {
      Graphics2D graphics = g;
      graphics.setColor(baseColor);

      graphics.draw(extendLine);
      graphics.draw(endLine);
      graphics.draw(startLine);

    }

    private void adjustChange(FontMetrics fm, String s, String e, String t) {
      int lengthSU = fm.stringWidth(s);
      int lengthTU = fm.stringWidth(t);
      int lengthEU = fm.stringWidth(e);
      int lengthHU = fm.stringWidth(handleLabel);
      int fontHeight = fm.getHeight();
      double theta;
      double deltaX, deltaY;
      Point2D.Float temp;


      if (degreeOfLine > 0 && degreeOfLine < 90) {
        theta = Math.atan(slope);
//                deltaX = Math.cos(theta) * (lengthEU/2);
//                deltaY = Math.sin(theta) * (lengthEU/2);
        deltaX = Math.cos(theta) * (lengthEU);
        deltaY = Math.sin(theta) * (lengthEU);

        endLoc = getLowPoint(fontHeight, guideRightLow);
        startLoc = getLowPoint(fontHeight, guideLeftLow);
        targetLoc = getLowPoint((fontHeight * 2), guideLeftLow);

        endLoc.x = (float) (endLoc.x - deltaX);
        endLoc.y = (float) (endLoc.y - deltaY);

//                deltaX = Math.cos(theta) * (lengthSU/2);
//                deltaY = Math.sin(theta) * (lengthSU/2);

//                startLoc.x = (float)(startLoc.x - deltaX);
//                startLoc.y = (float)(startLoc.y - deltaY);

//                deltaX = Math.cos(theta) * (lengthTU/2);
//                deltaY = Math.sin(theta) * (lengthTU/2);

//                targetLoc.x = (float)(targetLoc.x - deltaX);
//                targetLoc.y = (float)(targetLoc.y - deltaY);
      } else if (degreeOfLine > 90 && degreeOfLine < 180) {
        endLoc = getLowPoint(fontHeight, guideRightLow);
        startLoc = getLowPoint(fontHeight, guideLeftLow);
        targetLoc = getLowPoint((fontHeight * 2), guideLeftLow);

        theta = Math.atan(slope);
//                deltaX = Math.cos(theta) * (lengthSU/2);
//                deltaY = Math.sin(theta) * (lengthSU/2);
        deltaX = Math.cos(theta) * (lengthSU);
        deltaY = Math.sin(theta) * (lengthSU);

        startLoc.x = (float) (startLoc.x - deltaX);
        startLoc.y = (float) (startLoc.y - deltaY);

//                deltaX = Math.cos(theta) * (lengthTU/2);
//                deltaY = Math.sin(theta) * (lengthTU/2);
        deltaX = Math.cos(theta) * (lengthTU);
        deltaY = Math.sin(theta) * (lengthTU);

        targetLoc.x = (float) (targetLoc.x - deltaX);
        targetLoc.y = (float) (targetLoc.y - deltaY);

//                deltaX = Math.cos(theta) * (lengthEU/2);
//                deltaY = Math.sin(theta) * (lengthEU/2);
//
//                endLoc.x = (float)(endLoc.x - deltaX);
//                endLoc.y = (float)(endLoc.y - deltaY);
      } else if (degreeOfLine > 180 && degreeOfLine < 270) {
        endLoc = getLowPoint(fontHeight, guideRightLow);
        startLoc = getLowPoint(fontHeight, guideLeftLow);
        targetLoc = getLowPoint((fontHeight * 2), guideLeftLow);

        theta = Math.atan(slope);
//                deltaX = Math.cos(theta) * (lengthSU/2);
//                deltaY = Math.sin(theta) * (lengthSU/2);
        deltaX = Math.cos(theta) * (lengthSU);
        deltaY = Math.sin(theta) * (lengthSU);

        startLoc.x = (float) (startLoc.x - deltaX);
        startLoc.y = (float) (startLoc.y - deltaY);

//                deltaX = Math.cos(theta) * (lengthTU/2);
//                deltaY = Math.sin(theta) * (lengthTU/2);
        deltaX = Math.cos(theta) * (lengthTU);
        deltaY = Math.sin(theta) * (lengthTU);

        targetLoc.x = (float) (targetLoc.x - deltaX);
        targetLoc.y = (float) (targetLoc.y - deltaY);

//                deltaX = Math.cos(theta) * (lengthEU/2);
//                deltaY = Math.sin(theta) * (lengthEU/2);
//
//                endLoc.x = (float)(endLoc.x - deltaX);
//                endLoc.y = (float)(endLoc.y - deltaY);
      } else if (degreeOfLine > 270 && degreeOfLine < 360) {
        endLoc = getLowPoint(fontHeight, guideRightLow);
        startLoc = getLowPoint(fontHeight, guideLeftLow);
        targetLoc = getLowPoint((fontHeight * 2), guideLeftLow);

        theta = Math.atan(slope);
        theta = Math.toDegrees(theta);
        theta = 180 - theta;
        theta -= 90;
        theta = Math.toRadians(theta);

//                deltaX = Math.sin(theta) * (lengthEU/2);
//                deltaY = Math.cos(theta) * (lengthEU/2);
        deltaX = Math.sin(theta) * (lengthEU);
        deltaY = Math.cos(theta) * (lengthEU);
        temp = endLoc;
        endLoc.x = (float) (temp.x - deltaX);
        endLoc.y = (float) (temp.y - deltaY);

//                deltaX = Math.sin(theta) * (lengthSU/2);
//                deltaY = Math.cos(theta) * (lengthSU/2);
//
//                startLoc.x = (float)(startLoc.x - deltaX);
//                startLoc.y = (float)(startLoc.y - deltaY);

//                deltaX = Math.sin(theta) * (lengthTU/2);
//                deltaY = Math.cos(theta) * (lengthTU/2);
//
//                targetLoc.x = (float)(targetLoc.x - deltaX);
//                targetLoc.y = (float)(targetLoc.y - deltaY);

      } else if (degreeOfLine == 0 || degreeOfLine == 360) {
//                endLoc.x = endLoc.x - (lengthEU/2);
//                startLoc.x = startLoc.x - (lengthSU/2);
//                targetLoc.x = targetLoc.x - (lengthTU/2);
        endLoc.x = endLoc.x - (lengthEU);
        endLoc.y = endLoc.y + fontHeight;
        startLoc.y = startLoc.y + fontHeight;
        targetLoc.y = targetLoc.y + (fontHeight * 2);
      } else if (degreeOfLine == 180) {
//                startLoc.x = startLoc.x - (lengthSU/2);
//                targetLoc.x = targetLoc.x - (lengthTU/2);
//                endLoc.x = endLoc.x - (lengthEU/2);
        startLoc.x = startLoc.x - (lengthSU);
        targetLoc.x = targetLoc.x - (lengthTU);
        startLoc.y = startLoc.y + fontHeight;
        targetLoc.y = targetLoc.y + (fontHeight * 2);
        endLoc.y = endLoc.y + fontHeight;
      } else if (degreeOfLine == 90) {
        startLoc.x = startLoc.x - fontHeight;
        endLoc.x = endLoc.x - fontHeight;
        targetLoc.x = targetLoc.x - (fontHeight * 2);
//                endLoc.y = endLoc.y - (lengthEU/2);
//                startLoc.y = startLoc.y - (lengthSU/2);
//                targetLoc.y = targetLoc.y - (lengthTU/2);
        endLoc.y = endLoc.y - (lengthEU);
      } else if (degreeOfLine == 270) {
        startLoc.x = startLoc.x + fontHeight;
        endLoc.x = endLoc.x + fontHeight;
        targetLoc.x = targetLoc.x + (fontHeight * 2);
//                endLoc.y = endLoc.y + (lengthEU/2);
//                startLoc.y = startLoc.y + (lengthSU/2);
//                targetLoc.y = targetLoc.y + (lengthTU/2);
        endLoc.y = endLoc.y + (lengthEU);
      } else {
        System.out.println("not supported yet adjust Change");
      }
    }

    private void centerHandleLabel(FontMetrics fm) {
      int lengthSU = fm.stringWidth("" + startUnit.toDouble());
      int lengthTU = fm.stringWidth("" + targetUnit.toDouble());
      int lengthEU = fm.stringWidth("" + endUnit.toDouble());
      int lengthHU = fm.stringWidth(handleLabel);
      int fontHeight = fm.getHeight();
      double theta;
      double _x, _y;
      double deltaX, deltaY;
      Point2D.Float temp;
      if (degreeOfLine == 0 || degreeOfLine == 360) {
        handleLoc.x = handleLoc.x - (lengthHU / 2);
        handleLoc.y = handleLoc.y + (fontHeight * 3);
      } else if (degreeOfLine == 180) {
        handleLoc.x = handleLoc.x - (lengthHU / 2);
        handleLoc.y = handleLoc.y + (fontHeight * 3);
      } else if (degreeOfLine == 90) {
        handleLoc.y = handleLoc.y - (lengthHU / 2);
        handleLoc.x = handleLoc.x - (fontHeight * 3);
      } else if (degreeOfLine == 270) {
        handleLoc.x = handleLoc.x + (fontHeight * 3);
        handleLoc.y = handleLoc.y + (lengthHU / 2);
      } else if (degreeOfLine > 0 && degreeOfLine < 90) {
        handleLoc = getLowPoint((fontHeight * 3), guideHandleLow);
        theta = Math.atan(slope);
        deltaX = Math.cos(theta) * (lengthHU / 2);
        deltaY = Math.sin(theta) * (lengthHU / 2);

        handleLoc.x = handleLoc.x - (float) deltaX;
        handleLoc.y = handleLoc.y - (float) deltaY;
      } else if (degreeOfLine > 90 && degreeOfLine < 180) {
        handleLoc = getLowPoint((fontHeight * 3), guideHandleLow);
        theta = Math.atan(slope);
        deltaX = Math.cos(theta) * (lengthHU / 2);
        deltaY = Math.sin(theta) * (lengthHU / 2);

        handleLoc.x = handleLoc.x - (float) deltaX;
        handleLoc.y = handleLoc.y - (float) deltaY;
      } else if (degreeOfLine > 180 && degreeOfLine < 270) {
        handleLoc = getLowPoint((fontHeight * 3), guideHandleLow);
        theta = Math.atan(slope);
        deltaX = Math.cos(theta) * (lengthHU / 2);
        deltaY = Math.sin(theta) * (lengthHU / 2);

        handleLoc.x = handleLoc.x - (float) deltaX;
        handleLoc.y = handleLoc.y - (float) deltaY;
      } else if (degreeOfLine > 270 && degreeOfLine < 360) {
        handleLoc = getLowPoint((fontHeight * 3), guideHandleLow);
        theta = Math.atan(slope);
        theta = Math.toDegrees(theta);
        theta = 180 - theta;
        theta -= 90;
        deltaX = Math.sin(Math.toRadians(theta)) * (lengthHU / 2);
        deltaY = Math.cos(Math.toRadians(theta)) * (lengthHU / 2);
        handleLoc.x = handleLoc.x - (float) deltaX;
        handleLoc.y = handleLoc.y - (float) deltaY;
      } else {
        System.out.println("not supported centerHandleLabel");
      }
    }

    Point2D.Float rotateStart = new Point2D.Float(), rotateEnd = new Point2D.Float(), rotateHandle = new Point2D.Float(), rotateTarget = new Point2D.Float();

    private void displayLabels(Graphics2D g) {
      Graphics2D graphics = g;

      double degrees = Math.toDegrees(Math.atan(slope));
      double theta = Math.toRadians(degrees);

      FontMetrics fm = graphics.getFontMetrics(dispfont);

      graphics.setFont(dispfont);
      graphics.setColor(fontColor);

      String start, end, target;
      start = getStartLabel();
      end = getEndLabel();
      target = getTargetLabel();

      if (!"none".equals(unitLabel)) {
        if (startUnit.toDouble() == 1.0) {
          start = start + " " + unitLabel;
        } else {
          start = start + " " + unitLabel + "s";
        }

        if (endUnit.toDouble() == 1.0) {
          end = end + " " + unitLabel;
        } else {
          end = end + " " + unitLabel + "s";
        }

        if (targetUnit.toDouble() == 1.0) {
          target = target + " " + unitLabel;
        } else {
          target = target + " " + unitLabel + "s";
        }
      }

      if (adjustChange) {
        adjustChange(fm, start, end, target);
        adjustChange = false;
      }
      if (centerHandle) {
        centerHandleLabel(fm);
        centerHandle = false;
      }

      if (showHideLabels[0]) {
        graphics.rotate(theta, startLoc.x, startLoc.y);
        graphics.drawString(start, startLoc.x, startLoc.y);
        graphics.rotate(-theta, startLoc.x, startLoc.y);
      }
      if (showHideLabels[1]) {
        graphics.rotate(theta, endLoc.x, endLoc.y);
        graphics.drawString(end, endLoc.x, endLoc.y);
        graphics.rotate(-theta, endLoc.x, endLoc.y);
      }
      if (!isEstimateTask && showHideLabels[2]) {
        graphics.rotate(theta, targetLoc.x, targetLoc.y);
        graphics.drawString(target, targetLoc.x, targetLoc.y);
        graphics.rotate(-theta, targetLoc.x, targetLoc.y);
      }
      if (showHideLabels[3]) {
        graphics.rotate(theta, handleLoc.x, handleLoc.y);
        graphics.drawString(handleLabel, handleLoc.x, handleLoc.y);
        graphics.rotate(-theta, handleLoc.x, handleLoc.y);
      }
    }

    private String getStartLabel() {
      String start = "";
      String[] fract_temp = new String[2];

      StringBuilder sb = new StringBuilder();
      String html1 = "<html><body><sup>";
      String html2 = "</sup><font size=+1>/<font size=-1><sub>";
      String html3 = "</sub></body></html>";
      switch (startUnit.getType()) {
        case FRACT:
          fract_temp = startUnit.getValue().split("/");
          //sb.append(html1);
          sb.append(fract_temp[0]);
          //sb.append(html2);
          sb.append("/");
          sb.append(fract_temp[1]);
          //sb.append(html3);
          break;
        case ODDS:
          fract_temp = startUnit.getValue().split("in");
          sb.append(fract_temp[0]);
          sb.append(" in ");
          sb.append(fract_temp[1]);
          break;
        case DECI:
          sb.append(startUnit.toDouble());
          break;
        case INT:
          sb.append(startUnit.toInteger());
          break;
        default:
          System.out.println("Unsupported Unit type");
          System.exit(0);
      }
      start = sb.toString();
      return start;
    }

    private String getEndLabel() {
      String end = "";

      String[] fract_temp = new String[2];
      StringBuilder sb = new StringBuilder();
//            String html1 = "<html><body><sup>";
//            String html2 = "</sup><font size=+1>/<font size=-1><sub>";
//            String html3 = "</sub></body></html>";

      switch (endUnit.getType()) {
        case FRACT:
          fract_temp = endUnit.getValue().split("/");
          //sb.append(html1);
          sb.append(fract_temp[0]);
          //sb.append(html2);
          sb.append("/");
          sb.append(fract_temp[1]);
          //sb.append(html3);
          break;
        case ODDS:
          fract_temp = endUnit.getValue().split("in");
          sb.append(fract_temp[0]);
          sb.append(" in ");
          sb.append(fract_temp[1]);
          break;
        case DECI:
          sb.append(endUnit.toDouble());
          break;
        case INT:
          sb.append(endUnit.toInteger());
          break;
        default:
          System.out.println("Unsupported Unit type");
          System.exit(0);
      }
      end = sb.toString();
      return end;
    }

    private String getTargetLabel() {
      String target = "";

      String[] fract_temp = new String[2];
      StringBuilder sb = new StringBuilder();
//            String html1 = "<html><body><sup>";
//            String html2 = "</sup><font size=+1>/<font size=-1><sub>";
//            String html3 = "</sub></body></html>";
      switch (targetUnit.getType()) {
        case FRACT:
          fract_temp = targetUnit.getValue().split("/");
          //sb.append(html1);
          sb.append(fract_temp[0]);
          //sb.append(html2);
          sb.append("/");
          sb.append(fract_temp[1]);
          //sb.append(html3);
          break;
        case ODDS:
          fract_temp = targetUnit.getValue().split("in");
          sb.append(fract_temp[0]);
          sb.append(" in ");
          sb.append(fract_temp[1]);
          break;
        case DECI:
          sb.append(targetUnit.toDouble());
          break;
        case INT:
          sb.append(targetUnit.toInteger());
          break;
        default:
          System.out.println("Unsupported Unit type");
          System.exit(0);
      }
      target = sb.toString();
      return target;
    }

    /*********************************
     * Overrides this panel's paintComponent method
     * NOTE: The class is a subclass of JPanel
     * Necessary to ensure current graphics are
     * redrawn on panel when panel is refreshed
     *********************************/
    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D graphics = (Graphics2D) g;
      graphics.setStroke(stroke);
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


      if (dragLine != null) {
        graphics.setColor(dragColor);
        graphics.draw(dragLine);
      }

      drawBaseCircle(graphics);

//            if(isHandleDragged)
//                currentHandleColor = dragColor;
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

      if (isDebugOn) {
        graphics.setColor(Color.CYAN);
        graphics.setStroke(new BasicStroke(1));

      }
            /*
            if(drawDragLine){
                drawDragLine(graphics, dragLinePos);
                drawDragLine = false;
            }
            if(isEstimateTask)
                drawTargetDragLine(graphics);
            drawHandle(graphics, currentDragX, currHandleColor);
            if(currentLine != null){
                graphics.setStroke(stroke);
                graphics.setColor(currentLineColor);
                graphics.draw(currentLine);
                currentLine = null;
            }
            *
            */
    }
  }
}  