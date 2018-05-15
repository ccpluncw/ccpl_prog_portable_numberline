package ccpl.numberline.config;

import ccpl.lib.Bundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;

import static ccpl.lib.util.UiUtil.screenHeight;
import static ccpl.lib.util.UiUtil.screenWidth;
import static ccpl.numberline.config.ConfigValidator.generateConfigErrors;

/**
 * Dialog which appears when the experimenter clicks configure.
 *
 * This class does not handle the the actual layout, that is delegated to ConfigPanel
 *
 * @see DetailedConfigDialog
 */
public class DetailedConfigDialog extends JDialog {

    private DetailedConfigPanel panel = new DetailedConfigPanel();

    public DetailedConfigDialog() {
        super();
        this.setLayout(new BorderLayout());

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(actionEvent -> {
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
        this.setLocation((screenWidth() - this.getWidth()) / 2, (screenHeight() - this.getHeight()) / 2);
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