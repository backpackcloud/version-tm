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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConstraintsTest {

  @Test
  public void testDefaultConstraint() {
    Helper helper = new Helper("", "1.2.3");

    helper.assertTrue("1.2.3");

    helper.assertFalse("1.2.4");
    helper.assertFalse("1.1.3");
    helper.assertFalse("2.2.3");

    helper.assertFalse("1.2.0", "1.2");
    helper.assertFalse("1.0", "1");
    helper.assertFalse("1.0.0", "1");
  }

  @Test
  public void testEqualTo() {
    Helper helper = new Helper("=", "1.2.3");

    helper.assertTrue("1.2.3");

    helper.assertFalse("1.2.4");
    helper.assertFalse("1.1.3");
    helper.assertFalse("2.2.3");

    helper.assertFalse("1.2.0", "1.2");
    helper.assertFalse("1.0", "1");
    helper.assertFalse("1.0.0", "1");
  }

  @Test
  public void testDifferentFrom() {
    Helper helper = new Helper("!=", "1.2.3");

    helper.assertFalse("1.2.3");

    helper.assertTrue("1.2.4");
    helper.assertTrue("1.1.3");
    helper.assertTrue("2.2.3");
    helper.assertTrue("1.2.0", "1.2");
    helper.assertTrue("1.0", "1");
    helper.assertTrue("1.0.0", "1");
  }

  @Test
  public void testGreaterThan() {
    Helper helper = new Helper(">", "1.2.3");

    helper.assertTrue("1.2.4");
    helper.assertTrue("1.3.0");
    helper.assertTrue("2.0.0");

    helper.assertFalse("1.2.3");
    helper.assertFalse("1.0.5");
    helper.assertFalse("1.1.5");
    helper.assertFalse("1.2");
    helper.assertFalse("1");
  }

  @Test
  public void testGreaterThanOrEqualToTo() {
    Helper helper = new Helper(">=", "1.2.3");

    helper.assertTrue("1.2.4");
    helper.assertTrue("1.3.0");
    helper.assertTrue("2.0.0");
    helper.assertTrue("1.2.3");

    helper.assertFalse("1.0.5");
    helper.assertFalse("1.1.5");
    helper.assertFalse("1.2");
    helper.assertFalse("1");
  }

  @Test
  public void testLessThan() {
    Helper helper = new Helper("<", "1.2.3");

    helper.assertFalse("1.2.4");
    helper.assertFalse("1.3.0");
    helper.assertFalse("2.0.0");
    helper.assertFalse("1.2.3");

    helper.assertTrue("1.0.5");
    helper.assertTrue("1.1.5");
    helper.assertTrue("1.2");
    helper.assertTrue("1");
  }

  @Test
  public void testLessThanOrEqualToTo() {
    Helper helper = new Helper("<=", "1.2.3");

    helper.assertFalse("1.2.4");
    helper.assertFalse("1.3.0");
    helper.assertFalse("2.0.0");

    helper.assertTrue("1.2.3");
    helper.assertTrue("1.0.5");
    helper.assertTrue("1.1.5");
    helper.assertTrue("1.2");
    helper.assertTrue("1");
  }

  @Test
  public void testAtLeastAtMinorLevel() {
    Helper helper = new Helper("~>", "1.3.2");

    helper.assertTrue("1.3.2");
    helper.assertTrue("1.3.3");
    helper.assertTrue("1.3.20");

    helper.assertFalse("1.3.0");
    helper.assertFalse("1.1.0");
    helper.assertFalse("1.3");
    helper.assertFalse("1.1");
    helper.assertFalse("1");
    helper.assertFalse("1.4.0");
    helper.assertFalse("1.4");
    helper.assertFalse("2");
    helper.assertFalse("2.0");
    helper.assertFalse("2.0.0");
    helper.assertFalse("2.1.0");

    helper.assertTrue("1.3.20", "1.3");
    helper.assertTrue("1.3.20", "1");
    helper.assertTrue("1.9", "1");
    helper.assertFalse("2", "1");
  }

  @Test
  public void testAtLeastAtMajorLevel() {
    Helper helper = new Helper("~~>", "1.3.2");

    helper.assertTrue("1.3.2");
    helper.assertTrue("1.3.3");
    helper.assertTrue("1.3.20");

    helper.assertFalse("1.3.0");
    helper.assertFalse("1.1.0");
    helper.assertFalse("1.3");
    helper.assertFalse("1.1");
    helper.assertFalse("1");
    helper.assertTrue("1.4.0");
    helper.assertTrue("1.4");
    helper.assertTrue("1.4.8");
    helper.assertTrue("1.6");
    helper.assertTrue("1.6.9");
    helper.assertFalse("2");
    helper.assertFalse("2.0");
    helper.assertFalse("2.0.0");
    helper.assertFalse("2.1.0");

    helper.assertTrue("1.3.20", "1.3");
    helper.assertTrue("1.3.20", "1");
    helper.assertTrue("1.9", "1");
    helper.assertFalse("2", "1");
  }

  @Test
  public void testPriorToMinor() {
    Helper helper = new Helper("<~", "3.4.6");

    helper.assertFalse("3.4.6");
    helper.assertTrue("3.4.5");
    helper.assertTrue("3.4.4");
    helper.assertTrue("3.4.0");
    helper.assertTrue("3.4");

    helper.assertFalse("3.3.6");
    helper.assertFalse("3.2.4");
    helper.assertFalse("3.2");
    helper.assertFalse("3.1.8");
    helper.assertFalse("3.1");
    helper.assertFalse("3.0");
    helper.assertFalse("3");

    helper.assertFalse("2.2.2");
    helper.assertFalse("2.2");
    helper.assertFalse("2");
  }

  @Test
  public void testPriorToMajor() {
    Helper helper = new Helper("<~~", "3.4.6");

    helper.assertFalse("3.4.6");
    helper.assertTrue("3.4.5");
    helper.assertTrue("3.4.4");
    helper.assertTrue("3.4.0");
    helper.assertTrue("3.4");

    helper.assertTrue("3.3.6");
    helper.assertTrue("3.2.4");
    helper.assertTrue("3.2");
    helper.assertTrue("3.1.8");
    helper.assertTrue("3.1");
    helper.assertTrue("3.0");
    helper.assertTrue("3");

    helper.assertFalse("2.2.2");
    helper.assertFalse("2.2");
    helper.assertFalse("2");
  }

  @Test
  public void testRange() {
    Constraint range = Constraint.create("1.2.3 |-| 1.3.5");

    assertTrue(range.test(new Version(1, 2, 3)));
    assertTrue(range.test(new Version(1, 2, 4)));
    assertTrue(range.test(new Version(1, 2, 10)));
    assertTrue(range.test(new Version(1, 3, 3)));
    assertTrue(range.test(new Version(1, 3, 5)));

    assertFalse(range.test(new Version(1)));
    assertFalse(range.test(new Version(1, 2)));
    assertFalse(range.test(new Version(2, 2, 3)));
    assertFalse(range.test(new Version(1, 3, 6)));
    assertFalse(range.test(new Version(1, 4, 6)));

    range = Constraint.create("1.2.3 -| 1.3.5");

    assertTrue(range.test(new Version(1, 2, 4)));
    assertTrue(range.test(new Version(1, 2, 10)));
    assertTrue(range.test(new Version(1, 3, 3)));
    assertTrue(range.test(new Version(1, 3, 5)));

    assertFalse(range.test(new Version(1, 2, 3)));
    assertFalse(range.test(new Version(1)));
    assertFalse(range.test(new Version(1, 2)));
    assertFalse(range.test(new Version(2, 2, 3)));
    assertFalse(range.test(new Version(1, 3, 6)));
    assertFalse(range.test(new Version(1, 4, 6)));

    range = Constraint.create("1.2.3 |- 1.3.5");

    assertTrue(range.test(new Version(1, 2, 3)));
    assertTrue(range.test(new Version(1, 2, 4)));
    assertTrue(range.test(new Version(1, 2, 10)));
    assertTrue(range.test(new Version(1, 3, 3)));

    assertFalse(range.test(new Version(1, 3, 5)));
    assertFalse(range.test(new Version(1)));
    assertFalse(range.test(new Version(1, 2)));
    assertFalse(range.test(new Version(2, 2, 3)));
    assertFalse(range.test(new Version(1, 3, 6)));
    assertFalse(range.test(new Version(1, 4, 6)));

    range = Constraint.create("1.2.3 - 1.3.5");

    assertTrue(range.test(new Version(1, 2, 4)));
    assertTrue(range.test(new Version(1, 2, 10)));
    assertTrue(range.test(new Version(1, 3, 3)));

    assertFalse(range.test(new Version(1, 3, 5)));
    assertFalse(range.test(new Version(1, 2, 3)));
    assertFalse(range.test(new Version(1)));
    assertFalse(range.test(new Version(1, 2)));
    assertFalse(range.test(new Version(2, 2, 3)));
    assertFalse(range.test(new Version(1, 3, 6)));
    assertFalse(range.test(new Version(1, 4, 6)));

    range = Constraint.create("1.2 - 2.0");

    assertTrue(range.test(new Version(1, 2, 4)));
    assertTrue(range.test(new Version(1, 2, 0)));

    assertFalse(range.test(new Version(1, 2)));

    assertTrue(range.test(new Version(2)));

    assertFalse(range.test(new Version(2, 0)));
    assertFalse(range.test(new Version(2, 0, 0)));
  }

  @Test
  public void testFailure() {
    Constraint constraint = Constraint.create(null);

    assertFalse(constraint.test(new Version(1, 2, 3)));
    assertFalse(constraint.test(new Version(1, 0, 0)));
    assertFalse(constraint.test(null));
    assertFalse(constraint.test(Version.NULL));

    constraint = Constraint.create("");

    assertFalse(constraint.test(new Version(1, 2, 3)));
    assertFalse(constraint.test(new Version(1, 0, 0)));
    assertFalse(constraint.test(null));
    assertFalse(constraint.test(Version.NULL));

    constraint = Constraint.create("  ");

    assertFalse(constraint.test(new Version(1, 2, 3)));
    assertFalse(constraint.test(new Version(1, 0, 0)));
    assertFalse(constraint.test(null));
    assertFalse(constraint.test(Version.NULL));

    assertThrows(IllegalArgumentException.class, () -> Constraint.create("{1.2.3}"));
  }

  static class Helper {
    private final String operator;
    private final String defaultReference;

    Helper(String operator, String defaultReference) {
      this.operator = operator;
      this.defaultReference = defaultReference;
    }

    void assertTrue(String test) {
      assertTrue(test, defaultReference);
    }

    void assertFalse(String test) {
      assertFalse(test, defaultReference);
    }

    void assertTrue(String test, String reference) {
      Assertions.assertTrue(Constraint.create(String.format("%s %s", operator, reference)).test(Version.of(test)));
    }

    void assertFalse(String test, String reference) {
      Assertions.assertFalse(Constraint.create(String.format("%s %s", operator, reference)).test(Version.of(test)));
    }

  }

}
