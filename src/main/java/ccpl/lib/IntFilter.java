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
