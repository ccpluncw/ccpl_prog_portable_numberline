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

package ccpl.numberline.config;

import static ccpl.lib.util.UiUtil.screenHeight;
import static ccpl.lib.util.UiUtil.screenWidth;
import static ccpl.numberline.config.ConfigValidator.generateConfigErrors;

import ccpl.lib.Bundle;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Dialog which appears when the experimenter clicks configure.
 *
 * <p>This class does not handle the the actual layout, that is delegated to ConfigPanel
 *
 * @see DetailedConfigDialog
 */
public class DetailedConfigDialog extends JDialog {

  private DetailedConfigPanel panel = new DetailedConfigPanel(this);

  /** Dialog which holds the detailed configuration options. */
  public DetailedConfigDialog() {
    super();
    this.setLayout(new BorderLayout());

    JButton saveBtn = new JButton("OK");
    saveBtn.addActionListener(
        actionEvent -> {
          if (isValidConfig()) {
            this.setVisible(false);
          }
        });

    JPanel botPanel = new JPanel();
    botPanel.add(saveBtn);

    // Add panels to the JDialog
    this.add(panel, BorderLayout.CENTER);
    this.add(botPanel, BorderLayout.SOUTH);

    this.pack();
    this.setLocation(
        (screenWidth() - this.getWidth()) / 2, (screenHeight() - this.getHeight()) / 2);
    this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
  }

  public Bundle getBundle() {
    return panel.getBundle();
  }

  public void setDefaults(Bundle defs) {
    panel.applyDefaults(defs);
  }

  public void setBaseBundle(Bundle bun) {
    panel.setBaseBundle(bun);
  }

  private boolean isValidConfig() {
    StringBuilder err = generateConfigErrors(panel.getBundle());

    if (!err.toString().isEmpty()) {
      JOptionPane.showMessageDialog(this, err.toString());
      return false;
    }

    return true;
  }
}
