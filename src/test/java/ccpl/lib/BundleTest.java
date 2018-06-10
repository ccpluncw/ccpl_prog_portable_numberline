package ccpl.lib;

import static junit.framework.TestCase.assertTrue;

import org.junit.Test;

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