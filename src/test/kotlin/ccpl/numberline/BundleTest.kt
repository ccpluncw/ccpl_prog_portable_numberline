package ccpl.numberline

import ccpl.lib.Bundle
import org.junit.Test

import org.junit.Assert.*

class BundleTest {
  @Test
  fun getAsBoolean() {
    val tempBundle = Bundle()

    tempBundle.add("string", "true")
    tempBundle.add("boolean", true)

    assertTrue(tempBundle.getAsBoolean("string"))
    assertTrue(tempBundle.getAsBoolean("boolean"))
  }

}