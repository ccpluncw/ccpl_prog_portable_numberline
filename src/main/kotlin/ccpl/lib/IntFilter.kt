package ccpl.lib

import javax.swing.text.AttributeSet
import javax.swing.text.BadLocationException
import javax.swing.text.DocumentFilter



class IntFilter : DocumentFilter() {
  @Throws(BadLocationException::class)
  override fun insertString(fb: DocumentFilter.FilterBypass, offset: Int, string: String,
                            attr: AttributeSet?) {
    val doc = fb.document
    val sb = StringBuilder()
    sb.append(doc.getText(0, doc.length))
    sb.insert(offset, string)

    if (test(sb.toString())) {
      super.insertString(fb, offset, string, attr)
    }
  }

  private fun test(text: String): Boolean {
    if (text.isEmpty()) {
      return true
    }

    return try {
      Integer.parseInt(text)
      true
    } catch (e: NumberFormatException) {
      false
    }
  }

  @Throws(BadLocationException::class)
  override fun replace(fb: DocumentFilter.FilterBypass, offset: Int, length: Int, text: String,
                       attrs: AttributeSet?) {
    val doc = fb.document
    val sb = StringBuilder()
    sb.append(doc.getText(0, doc.length))
    sb.replace(offset, offset + length, text)

    if (test(sb.toString())) {
      super.replace(fb, offset, length, text, attrs)
    }
  }

  @Throws(BadLocationException::class)
  override fun remove(fb: DocumentFilter.FilterBypass, offset: Int, length: Int) {
    val doc = fb.document
    val sb = StringBuilder()
    sb.append(doc.getText(0, doc.length))
    sb.delete(offset, offset + length)

    if (test(sb.toString())) {
      super.remove(fb, offset, length)
    }
  }
}