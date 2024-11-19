/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Marcelo "Ataxexe" Guimarães
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RangeTest {

  @Test
  public void testDescription() {
    assertEquals("[1.2.3, 4.5.6]", new Range(
      new Version(1, 2, 3),
      new Version(4, 5, 6),
      true,
      true
    ).toString());

    assertEquals("[1.2, 5.6]", new Range(
      new Version(1, 2),
      new Version(5, 6),
      true,
      true
    ).toString());

    assertEquals("[1, 5]", new Range(
      new Version(1),
      new Version(5),
      true,
      true
    ).toString());

    assertEquals("(1.2.3, 4.5.6]", new Range(
      new Version(1, 2, 3),
      new Version(4, 5, 6),
      false,
      true
    ).toString());

    assertEquals("[1.2, 5.6)", new Range(
      new Version(1, 2),
      new Version(5, 6),
      true,
      false
    ).toString());

    assertEquals("(1, 5)", new Range(
      new Version(1),
      new Version(5),
      false,
      false
    ).toString());
  }

  @Test
  public void testParsing() {
    Range range = Range.of("[1.2.3, 4.5.6]");

    assertEquals(Version.of("1.2.3"), range.min());
    assertEquals(Version.of("4.5.6"), range.max());
    assertTrue(range.includeMin());
    assertTrue(range.includeMax());

    range = Range.of("[1.2, 5.6]");

    assertEquals(Version.of("1.2"), range.min());
    assertEquals(Version.of("5.6"), range.max());
    assertTrue(range.includeMin());
    assertTrue(range.includeMax());


    range = Range.of("[1, 5.7]");

    assertEquals(Version.of("1"), range.min());
    assertEquals(Version.of("5.7"), range.max());
    assertTrue(range.includeMin());
    assertTrue(range.includeMax());

    range = Range.of("(1.2.3, 4.5.6]");

    assertEquals(Version.of("1.2.3"), range.min());
    assertEquals(Version.of("4.5.6"), range.max());
    assertFalse(range.includeMin());
    assertTrue(range.includeMax());

    range = Range.of("]1.2.3, 4.5.6]");

    assertEquals(Version.of("1.2.3"), range.min());
    assertEquals(Version.of("4.5.6"), range.max());
    assertFalse(range.includeMin());
    assertTrue(range.includeMax());

    range = Range.of("[1.2, 5.6)");

    assertEquals(Version.of("1.2"), range.min());
    assertEquals(Version.of("5.6"), range.max());
    assertTrue(range.includeMin());
    assertFalse(range.includeMax());

    range = Range.of("[1.2, 5.6[");

    assertEquals(Version.of("1.2"), range.min());
    assertEquals(Version.of("5.6"), range.max());
    assertTrue(range.includeMin());
    assertFalse(range.includeMax());

    range = Range.of("(1, 5)");

    assertEquals(Version.of("1"), range.min());
    assertEquals(Version.of("5"), range.max());
    assertFalse(range.includeMin());
    assertFalse(range.includeMax());

    range = Range.of("]1, 5[");

    assertEquals(Version.of("1"), range.min());
    assertEquals(Version.of("5"), range.max());
    assertFalse(range.includeMin());
    assertFalse(range.includeMax());
  }

  @Test
  public void testPredicate() {
    Range range = new Range(
      new Version(1, 2, 3),
      new Version(1, 3, 5),
      true,
      true
    );

    assertTrue(range.include(new Version(1, 2, 3)));
    assertTrue(range.include(new Version(1, 2, 4)));
    assertTrue(range.include(new Version(1, 2, 10)));
    assertTrue(range.include(new Version(1, 3, 3)));
    assertTrue(range.include(new Version(1, 3, 5)));

    assertFalse(range.include(new Version(1)));
    assertFalse(range.include(new Version(1, 2)));
    assertFalse(range.include(new Version(2, 2, 3)));
    assertFalse(range.include(new Version(1, 3, 6)));
    assertFalse(range.include(new Version(1, 4, 6)));

    range = new Range(
      new Version(1, 2, 3),
      new Version(1, 3, 5),
      false,
      true
    );

    assertTrue(range.include(new Version(1, 2, 4)));
    assertTrue(range.include(new Version(1, 2, 10)));
    assertTrue(range.include(new Version(1, 3, 3)));
    assertTrue(range.include(new Version(1, 3, 5)));

    assertFalse(range.include(new Version(1, 2, 3)));
    assertFalse(range.include(new Version(1)));
    assertFalse(range.include(new Version(1, 2)));
    assertFalse(range.include(new Version(2, 2, 3)));
    assertFalse(range.include(new Version(1, 3, 6)));
    assertFalse(range.include(new Version(1, 4, 6)));

    range = new Range(
      new Version(1, 2, 3),
      new Version(1, 3, 5),
      true,
      false
    );

    assertTrue(range.include(new Version(1, 2, 3)));
    assertTrue(range.include(new Version(1, 2, 4)));
    assertTrue(range.include(new Version(1, 2, 10)));
    assertTrue(range.include(new Version(1, 3, 3)));

    assertFalse(range.include(new Version(1, 3, 5)));
    assertFalse(range.include(new Version(1)));
    assertFalse(range.include(new Version(1, 2)));
    assertFalse(range.include(new Version(2, 2, 3)));
    assertFalse(range.include(new Version(1, 3, 6)));
    assertFalse(range.include(new Version(1, 4, 6)));

    range = new Range(
      new Version(1, 2, 3),
      new Version(1, 3, 5),
      false,
      false
    );

    assertTrue(range.include(new Version(1, 2, 4)));
    assertTrue(range.include(new Version(1, 2, 10)));
    assertTrue(range.include(new Version(1, 3, 3)));

    assertFalse(range.include(new Version(1, 3, 5)));
    assertFalse(range.include(new Version(1, 2, 3)));
    assertFalse(range.include(new Version(1)));
    assertFalse(range.include(new Version(1, 2)));
    assertFalse(range.include(new Version(2, 2, 3)));
    assertFalse(range.include(new Version(1, 3, 6)));
    assertFalse(range.include(new Version(1, 4, 6)));
  }

}
