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

package ccpl.lib;

import static java.lang.Integer.parseInt;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class IntFilter extends DocumentFilter {
  @Override
  public void remove(FilterBypass filterBypass, int offset, int length)
      throws BadLocationException {
    Document doc = filterBypass.getDocument();
    StringBuilder sb = new StringBuilder();

    sb.append(doc.getText(0, doc.getLength()));
    sb.delete(offset, offset + length);

    if (test(sb.toString())) {
      super.remove(filterBypass, offset, length);
    }
  }

  @Override
  public void insertString(
      FilterBypass filterBypass, int offset, String s, AttributeSet attributeSet)
      throws BadLocationException {
    Document doc = filterBypass.getDocument();
    StringBuilder sb = new StringBuilder();

    sb.append(doc.getText(0, doc.getLength()));
    sb.insert(offset, offset);

    if (test(sb.toString())) {
      super.insertString(filterBypass, offset, s, attributeSet);
    }
  }

  @Override
  public void replace(
      FilterBypass filterBypass, int offset, int length, String s, AttributeSet attributeSet)
      throws BadLocationException {
    Document doc = filterBypass.getDocument();
    StringBuilder sb = new StringBuilder();
    sb.append(doc.getText(0, doc.getLength()));
    sb.replace(offset, offset + length, s);

    if (test(sb.toString())) {
      super.replace(filterBypass, offset, length, s, attributeSet);
    }
  }

  private boolean test(String text) {
    if (text.isEmpty()) {
      return true;
    }

    try {
      parseInt(text);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
