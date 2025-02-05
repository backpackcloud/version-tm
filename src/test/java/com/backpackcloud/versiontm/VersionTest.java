/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Marcelo "Ataxexe" Guimarães
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.backpackcloud.versiontm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionTest {

  @Test
  public void testObjectStructure() {
    Version version = new Version(1, 2, 3);

    assertEquals("1.2.3", version.toString());
    assertEquals(Precision.MICRO, version.precision());
    assertEquals(1, version.major());
    assertEquals(2, version.minor());
    assertEquals(3, version.micro());
    assertEquals(
      0b0_000000000000000000011_000000000000000000101_000000000000000000111L,
      version.value()
    );

    version = new Version(1, 2);

    assertEquals("1.2", version.toString());
    assertEquals(Precision.MINOR, version.precision());
    assertEquals(1, version.major());
    assertEquals(2, version.minor());
    assertEquals(
      0b0_000000000000000000011_000000000000000000101_000000000000000000000L,
      version.value()
    );

    version = new Version(1);

    assertEquals("1", version.toString());
    assertEquals(Precision.MAJOR, version.precision());
    assertEquals(1, version.major());
    assertEquals(
      0b0_000000000000000000011_000000000000000000000_000000000000000000000L,
      version.value()
    );

    version = new Version(9, 7, 10);

    assertEquals("9.7.10", version.toString());
    assertEquals(Precision.MICRO, version.precision());
    assertEquals(9, version.major());
    assertEquals(7, version.minor());
    assertEquals(10, version.micro());
    assertEquals(
      0b0_000000000000000010011_000000000000000001111_000000000000000010101L,
      version.value()
    );

    version = new Version(3, 0, 2000);

    assertEquals("3.0.2000", version.toString());
    assertEquals(Precision.MICRO, version.precision());
    assertEquals(3, version.major());
    assertEquals(0, version.minor());
    assertEquals(2000, version.micro());
    assertEquals(
      0b0_000000000000000000111_000000000000000000001_000000000111110100001L,
      version.value()
    );
  }

  @Test
  public void testParsing() {
    assertEquals(new Version(1, 2, 3), Version.of("1.2.3"));
    assertEquals(new Version(9, 7, 10), Version.of("9.7.10"));
    assertEquals(new Version(3, 0, 2000), Version.of("3.0.2000"));
    assertEquals(new Version(7, 4, 2), Version.of("7.4.2.GA"));
    assertEquals(new Version(1, 2, 3), Version.of("1.2.3-bla-1"));
    assertEquals(new Version(0, 1, 1), Version.of("v0.1.1"));
    assertEquals(new Version(1, 0), Version.of("1.0.x"));
    assertEquals(new Version(8, 0), Version.of("8.0.Beta"));
    assertEquals(new Version(21), Version.of("21"));
    assertEquals(new Version(1, 5), Version.of("1.5"));
    assertEquals(Version.NULL, Version.of(""));
    assertEquals(Version.NULL, Version.of(" "));
    assertEquals(Version.NULL, Version.of(null));
  }

  @Test
  public void testSegmentLimits() {
    Version version = new Version(1048575, 1, 1);

    assertEquals(
      0b0_111111111111111111111_000000000000000000011_000000000000000000011L,
      version.value()
    );

    version = new Version(1, 1048575, 1);

    assertEquals(
      0b0_000000000000000000011_111111111111111111111_000000000000000000011L,
      version.value()
    );

    version = new Version(1, 1, 1048575);

    assertEquals(
      0b0_000000000000000000011_000000000000000000011_111111111111111111111L,
      version.value()
    );
  }

  @Test
  public void testInvalidVersionsParsing() {
    assertThrows(IllegalArgumentException.class, () -> Version.of("1048576.0.1"));
    assertThrows(IllegalArgumentException.class, () -> Version.of("1.1048576.1"));
    assertThrows(IllegalArgumentException.class, () -> Version.of("1.1.1048576"));
    assertThrows(IllegalArgumentException.class, () -> Version.of("1.249524757298478957289479.16"));
    assertThrows(IllegalArgumentException.class, () -> Version.of("a.b.c"));
    assertThrows(IllegalArgumentException.class, () -> Version.of("invalid"));
  }

  @Test
  public void testInvalidVersions() {
    int invalid = 0b0100000000000000000000000;

    assertThrows(IllegalArgumentException.class, () -> new Version(invalid, 0, 1));
    assertThrows(IllegalArgumentException.class, () -> new Version(1, invalid, 1));
    assertThrows(IllegalArgumentException.class, () -> new Version(1, 1, invalid));
    assertThrows(IllegalArgumentException.class, () -> new Version(invalid, invalid, 1));
    assertThrows(IllegalArgumentException.class, () -> new Version(invalid, 1, invalid));
    assertThrows(IllegalArgumentException.class, () -> new Version(invalid, invalid, invalid));

    assertThrows(IllegalArgumentException.class, () -> new Version(invalid));
    assertThrows(IllegalArgumentException.class, () -> new Version(1, invalid));

    assertThrows(IllegalArgumentException.class, () -> new Version(-1, 0, 1));
    assertThrows(IllegalArgumentException.class, () -> new Version(1, -1, 1));
    assertThrows(IllegalArgumentException.class, () -> new Version(1, 1, -1));
  }

  @Test
  public void testComparison() {
    Version a = Version.of("7.4.2");
    Version b = Version.of("7.4.1");
    Version c = Version.of("7.2.1020");
    Version d = Version.of("6.5.9");

    assertTrue(a.compareTo(b) > 0);
    assertTrue(a.compareTo(c) > 0);
    assertTrue(a.compareTo(d) > 0);
    assertTrue(c.compareTo(d) > 0);

    assertTrue(Version.of("0.0").compareTo(Version.of("0")) > 0);
    assertTrue(Version.of("0.0.0").compareTo(Version.of("0.0")) > 0);
    assertTrue(Version.of("0.0.0").compareTo(Version.of("0")) > 0);
  }

  @Test
  public void testNullValue() {
    Version nullVersion = Version.NULL;

    assertEquals(
      0b0_000000000000000000000_000000000000000000000_000000000000000000000L,
      nullVersion.value()
    );

    assertEquals(Precision.NONE, nullVersion.precision());

    assertEquals("null", nullVersion.toString());

    assertNotEquals(new Version(0), nullVersion);
    assertNotEquals(new Version(0, 0), nullVersion);
    assertNotEquals(new Version(0, 0, 0), nullVersion);

    assertTrue(nullVersion.compareTo(new Version(0)) < 0);
    assertTrue(nullVersion.compareTo(new Version(0, 0)) < 0);
    assertTrue(nullVersion.compareTo(new Version(0, 0, 0)) < 0);
  }

  @Test
  public void testNormalization() {
    assertEquals(
      0b0_110011001111111110011_000000000000000000000_000000000000000000000L,
      new Version(0b0_110011001111111110011_111111111111111111110_111111111111111111110L).value()
    );

    assertEquals(
      0b0_110011001111111110011_000000000000000000000_000000000000000000000L,
      new Version(0b0_110011001111111110011_111111111111111111110_111111111111111111110L).value()
    );

    assertEquals(
      0b0_110011001111111110011_000000000000000000000_000000000000000000000L,
      new Version(0b0_110011001111111110011_111111111111111111110_111110001111111111111L).value()
    );

    assertEquals(
      0b0_000000000000000000000_000000000000000000000_000000000000000000000L,
      new Version(0b0_110011001111111110010_111111111111111111110_111111111111111111110L).value()
    );
  }

  @Test
  public void testEnforcingPrecision() {
    assertEquals(Version.of("2.0.0"), Version.of("2", Precision.MICRO));
    assertNotEquals(Version.of("2.0"), Version.of("2.0", Precision.MICRO));
    assertNotEquals(Version.of("2.0"), Version.of("2.0", Precision.MAJOR));
  }

  @Test
  public void testCoercion() {
    try {
      Version.of("2.7.90419495");
      throw new RuntimeException();
    } catch (InvalidSegmentException e) {
      assertEquals(Version.of("2.7"), e.coercedVersion());
    }

    try {
      Version.of("2.90419495");
      throw new RuntimeException();
    } catch (InvalidSegmentException e) {
      assertEquals(Version.of("2"), e.coercedVersion());
    }

    try {
      Version.of("90419495");
      throw new RuntimeException();
    } catch (InvalidSegmentException e) {
      assertEquals(Version.NULL, e.coercedVersion());
    }
  }

  @Test
  public void testNegativeSegments() {
    assertThrows(InvalidSegmentException.class, () -> new Version(2, 7, -9));
    assertThrows(InvalidSegmentException.class, () -> new Version(2, -7, 9));
    assertThrows(InvalidSegmentException.class, () -> new Version(-2, 7, 9));
  }

}
