package ccpl.numberline;

import ccpl.lib.Bundle;
import ccpl.numberline.config.ConfigFrame;
import ccpl.numberline.config.PopupCallback;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Launcher {
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
      Logger.getLogger(Launcher.class.getName()).log(Level.WARNING, "Could not set system theme");
    }

    PopupCallback listener = new PopupCallback();
    ConfigFrame popup = new ConfigFrame(listener, "Configure");

    while (popup.isVisible()) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    Bundle bundle = listener.getBundle();

    if (bundle.getSize() != 0) {
      String expFile = "exp";
      String subject = bundle.getAsString("subject");
      String condition = bundle.getAsString("condition");
      String session = bundle.getAsString("session");

      UniversalNumberLine exp =
          new UniversalNumberLine(expFile, subject, condition, session, bundle);
      exp.run();
    }
  }
}
