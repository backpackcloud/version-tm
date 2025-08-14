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
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PrecisionTest {

  @Test
  public void testSegments() {
    assertEquals(0, Precision.NONE.segments());
    assertEquals(1, Precision.MAJOR.segments());
    assertEquals(2, Precision.MINOR.segments());
    assertEquals(3, Precision.MICRO.segments());
  }

  @Test
  public void testFromSegments() {
    assertEquals(Precision.NONE, Precision.fromSegments(0));
    assertEquals(Precision.MAJOR, Precision.fromSegments(1));
    assertEquals(Precision.MINOR, Precision.fromSegments(2));
    assertEquals(Precision.MICRO, Precision.fromSegments(3));

    assertThrows(IllegalArgumentException.class, () -> Precision.fromSegments(-2));
    assertThrows(IllegalArgumentException.class, () -> Precision.fromSegments(-1));
    assertThrows(IllegalArgumentException.class, () -> Precision.fromSegments(4));
    assertThrows(IllegalArgumentException.class, () -> Precision.fromSegments(5));
  }

  @Test
  public void testMore() {
    assertEquals(Precision.MAJOR, Precision.NONE.more());
    assertEquals(Precision.MINOR, Precision.MAJOR.more());
    assertEquals(Precision.MICRO, Precision.MINOR.more());
    assertEquals(Precision.MICRO, Precision.MICRO.more());
  }

  @Test
  public void testLess() {
    assertEquals(Precision.MINOR, Precision.MICRO.less());
    assertEquals(Precision.MAJOR, Precision.MINOR.less());
    assertEquals(Precision.MAJOR, Precision.MAJOR.less());
    assertEquals(Precision.NONE, Precision.NONE.less());
  }

}
