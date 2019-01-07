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

package ccpl.lib.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UiUtil {
  private static Toolkit tk = Toolkit.getDefaultToolkit();

  public static int screenWidth() {
    return tk.getScreenSize().width;
  }

  public static int screenHeight() {
    return tk.getScreenSize().height;
  }

  public static Container createToggleablePanel(
      Window parent,
      Container wrapper,
      Component content,
      AbstractButton showBtn,
      AbstractButton hideBtn) {
    return createToggleablePanel(parent, wrapper, content, showBtn, hideBtn, true);
  }

  /**
   * Create a Panel, which has buttons to toggle the display a content container.
   *
   * @param parent Parent window
   * @param wrapper Wrapper container for adding the buttons and content
   * @param content Content that may or may not be hidden
   * @param showBtn Button for the show action
   * @param hideBtn Button for the hide action
   * @return Container with display toggles
   */
  public static Container createToggleablePanel(
      Window parent,
      Container wrapper,
      Component content,
      AbstractButton showBtn,
      AbstractButton hideBtn,
      boolean createNewBtnGrp) {
    wrapper.setLayout(new BorderLayout());


    showBtn.addItemListener(
        event -> {
          if (event.getStateChange() == ItemEvent.SELECTED) {
            wrapper.add(content, BorderLayout.CENTER);
            wrapper.revalidate();
            parent.pack();
          }
        });

    hideBtn.addItemListener(
        event -> {
          if (event.getStateChange() == ItemEvent.SELECTED) {
            wrapper.remove(content);
            wrapper.revalidate();
            parent.pack();
          }
        });


    if (createNewBtnGrp) {
      ButtonGroup radioBtns = new ButtonGroup();
      radioBtns.add(showBtn);
      radioBtns.add(hideBtn);
    }

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(showBtn);
    buttonPanel.add(hideBtn);

    wrapper.add(buttonPanel, BorderLayout.NORTH);

    return wrapper;
  }

  /**
   * Return a JPanel that has a border and title.
   *
   * @param title Title for panel
   * @return JPanel with a titled border.
   */
  public static JPanel createPanelWithBorderTitle(String title) {
    JPanel panel = new JPanel();

    panel.setBorder(BorderFactory.createTitledBorder(title));

    return panel;
  }

  /**
   * Add a component to a GridLayout and add a new row to accomodate it.
   *
   * @param panel Panel with GridLayout
   * @param label Label for the component
   * @param comp Component
   */
  public static void expandGridPanel(JPanel panel, String label, JComponent comp) {
    GridLayout gridLayout = (GridLayout) panel.getLayout();
    gridLayout.setRows(gridLayout.getRows() + 1);
    panel.add(new JLabel(label + ": "));
    panel.add(comp);
  }

  public static void addTrackedTxtField(
      String key, String label, JPanel panel, Map<String, JTextField> tracker, boolean expandGrid) {
    JTextField textField = new JTextField("0");
    addTrackedTxtField(textField, key, label, panel, tracker, expandGrid);
  }

  /**
   * Add a text field to a panel and register it with a tracker.
   *
   * <p>This utility method reduces the amount of boilerplate code. This is thrown into a lambda.
   *
   * @param txt Textfield to add.
   * @param key Key for the map
   * @param label Label to display prior to the textfield.
   * @param panel Panel the textfield is being added to.
   * @param tracker Tracker
   * @param expandGrid Is the panel a GridPanel and should it expand the panel.
   */
  public static void addTrackedTxtField(
      JTextField txt,
      String key,
      String label,
      JPanel panel,
      Map<String, JTextField> tracker,
      boolean expandGrid) {
    tracker.put(key, txt);
    txt.setText("0");

    if (expandGrid) {
      expandGridPanel(panel, label, txt);
    } else {
      panel.add(new JLabel(label + ": "));
      panel.add(txt);
    }
  }
}
