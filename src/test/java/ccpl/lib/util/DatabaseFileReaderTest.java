package ccpl.lib.util;

import static org.junit.Assert.assertEquals;

import ccpl.lib.Bundle;
import java.net.URL;
import java.util.Objects;
import org.junit.Test;

public class DatabaseFileReaderTest {

  @Test
  public void readDbFile() {
    URL url = this.getClass().getClassLoader().getResource("dbfile_new_layout.txt");

    Bundle bundle = ccpl.lib.util.DatabaseFileReader.readDbFile(Objects.requireNonNull(url));

    assertEquals(14, bundle.getSize());
    assertEquals(new Integer(211), bundle.getAsInt("handle_red"));
    assertEquals("prac_bounded_prod.txt", bundle.getAsString("prac_trial"));
  }
}