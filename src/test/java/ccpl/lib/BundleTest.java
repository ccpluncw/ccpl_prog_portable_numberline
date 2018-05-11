package ccpl.lib;

import org.junit.Test;

import static org.junit.Assert.*;

public class BundleTest {

  @Test
  public void getAsBoolean() {
    Bundle tempBundle = new Bundle();

    tempBundle.add("string", "true");
    tempBundle.add("boolean", true);

    assertTrue(tempBundle.getAsBoolean("string"));
    assertTrue(tempBundle.getAsBoolean("boolean"));
  }
}