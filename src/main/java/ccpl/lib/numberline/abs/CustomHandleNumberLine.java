package ccpl.lib.numberline.abs;

import java.awt.Color;

public interface CustomHandleNumberLine {
  void setLeftBoundColor(Color newColor);

  void setRightBoundColor(Color newColor);

  void setLeftDragHandleColor(Color newColor);

  void setLeftDragActiveColor(Color newColor);

  void setRightDragHandleColor(Color newColor);

  void setRightDragActiveColor(Color newColor);

  void setDragActiveColor(Color newColor);

  void disableLeftHandle();
}
