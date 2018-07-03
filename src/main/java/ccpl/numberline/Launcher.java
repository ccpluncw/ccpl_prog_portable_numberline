package ccpl.numberline;

import ccpl.lib.Bundle;
import ccpl.numberline.config.ConfigDialog;
import ccpl.numberline.config.PopupCallback;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Launcher {
  /**
   * Entry point to the program.
   *
   * @param args Command-line arguments.
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      Logger.getLogger(Launcher.class.getName()).log(Level.WARNING, "Could not set system theme");
    }

    PopupCallback listener = new PopupCallback();
    final ConfigDialog popup = new ConfigDialog(listener, "Configure");

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
