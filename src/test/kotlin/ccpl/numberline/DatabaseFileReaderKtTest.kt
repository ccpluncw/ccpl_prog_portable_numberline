package ccpl.numberline

import org.junit.Test

import org.junit.Assert.*

class DatabaseFileReaderKtTest {
  @Test
  fun readDbFile() {
    val url = this.javaClass.classLoader.getResource("dbfile_new_layout.txt")

    val bundle = ccpl.numberline.readDbFile(url)

    assertEquals(14, bundle.size)
  }
}