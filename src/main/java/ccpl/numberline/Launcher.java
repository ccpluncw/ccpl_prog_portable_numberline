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

package ccpl.numberline;

import ccpl.lib.Bundle;
import ccpl.numberline.config.ConfigDialog;
import ccpl.numberline.config.Keys;
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
      String subject = bundle.getAsString(Keys.SUBJ);
      String condition = bundle.getAsString(Keys.CONDITION);
      String session = bundle.getAsString(Keys.SESSION);

      UniversalNumberLine exp =
          new UniversalNumberLine(expFile, subject, condition, session, bundle);
      exp.run();
    }

    popup.dispose();
    System.exit(0);
  }
}
