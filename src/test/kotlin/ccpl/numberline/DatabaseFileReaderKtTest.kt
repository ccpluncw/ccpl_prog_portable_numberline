package ccpl.numberline

import org.junit.Test

import org.junit.Assert.*

class DatabaseFileReaderKtTest {
  @Test
  fun readDbFile() {
    val url = this.javaClass.classLoader.getResource("dbfile_new_layout.txt")

    val bundle = ccpl.lib.util.readDbFile(url)

    assertEquals(14, bundle.size)
    assertEquals(211, bundle.getAsInt("handle_red"))
    assertEquals("prac_bounded_prod.txt", bundle.getAsString("prac_trial"))
  }
}