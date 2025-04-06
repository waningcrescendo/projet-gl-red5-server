package org.red5.server.net.rtmp;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestRTMPUtils {

  @Test
  public void testWriteMediumInt() {
    fail("Not yet implemented");
  }

  @Test
  public void testReadUnsignedMediumInt() {
    fail("Not yet implemented");
  }

  @Test
  public void testReadMediumInt() {
    fail("Not yet implemented");
  }

  @Test
  public void testCompareTimestamps() {
    assertEquals(1, RTMPUtils.compareTimestamps(10, 5));
    assertEquals(-1, RTMPUtils.compareTimestamps(5, 10));
    assertEquals(0, RTMPUtils.compareTimestamps(7, 7));
  }

  @Test
  public void testDiffTimestamps() {
    int a = 10;
    int b = 5;
    long expected = 5L;
    long result = RTMPUtils.diffTimestamps(a, b);
    assertEquals(expected, result);
  }
}
