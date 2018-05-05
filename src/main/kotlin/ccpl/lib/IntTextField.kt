package ccpl.lib

import javax.swing.JTextField
import javax.swing.text.PlainDocument

class IntTextField : JTextField() {
  init {
    (this.document as PlainDocument).documentFilter = IntFilter()
  }
}