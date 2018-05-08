package ccpl.lib.util;

import ccpl.lib.Bundle;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public class DatabaseFileReaderTest {

  @Test
  public void readDbFile() {
    URL url = this.getClass().getClassLoader().getResource("dbfile_new_layout.txt");

    Bundle bundle = ccpl.lib.util.DatabaseFileReader.readDbFile(url);

    assertEquals(14, bundle.getSize());
    assertEquals(211, bundle.getAsInt("handle_red"));
    assertEquals("prac_bounded_prod.txt", bundle.getAsString("prac_trial"));
  }
}