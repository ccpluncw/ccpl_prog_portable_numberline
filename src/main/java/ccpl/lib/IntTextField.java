package ccpl.lib;

import javax.swing.JTextField;
import javax.swing.text.PlainDocument;

public class IntTextField extends JTextField {
  public IntTextField() {
    ((PlainDocument) this.getDocument()).setDocumentFilter(new IntFilter());
  }
}
