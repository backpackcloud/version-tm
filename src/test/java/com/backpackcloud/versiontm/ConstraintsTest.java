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

import java.util.function.Function;
import java.util.function.Predicate;

public class ConstraintsTest {

  @Test
  public void testEqualTo() {
    Helper helper = new Helper("=", Constraints::equalTo);

    helper.assertTrue("1.2.3", "1.2.3");

    helper.assertFalse("1.2.3", "1.2.4");
    helper.assertFalse("1.2.3", "1.1.3");
    helper.assertFalse("1.2.3", "2.2.3");
    helper.assertFalse("1.2", "1.2.0");
    helper.assertFalse("1", "1.0");
    helper.assertFalse("1", "1.0.0");
  }

  @Test
  public void testDifferentFrom() {
    Helper helper = new Helper("!=", Constraints::differentFrom);

    helper.assertFalse("1.2.3", "1.2.3");

    helper.assertTrue("1.2.3", "1.2.4");
    helper.assertTrue("1.2.3", "1.1.3");
    helper.assertTrue("1.2.3", "2.2.3");
    helper.assertTrue("1.2", "1.2.0");
    helper.assertTrue("1", "1.0");
    helper.assertTrue("1", "1.0.0");
  }

  @Test
  public void testGreaterThan() {
    Helper helper = new Helper(">", Constraints::greaterThan);

    helper.assertTrue("1.2.3", "1.2.4");
    helper.assertTrue("1.2.3", "1.3.0");
    helper.assertTrue("1.2.3", "2.0.0");

    helper.assertFalse("1.2.3", "1.2.3");
    helper.assertFalse("1.2.3", "1.0.5");
    helper.assertFalse("1.2.3", "1.1.5");
    helper.assertFalse("1.2.3", "1.2");
    helper.assertFalse("1.2.3", "1");
  }

  @Test
  public void testGreaterThanOrEqualToTo() {
    Helper helper = new Helper(">=", Constraints::greaterThanOrEqualTo);

    helper.assertTrue("1.2.3", "1.2.4");
    helper.assertTrue("1.2.3", "1.3.0");
    helper.assertTrue("1.2.3", "2.0.0");
    helper.assertTrue("1.2.3", "1.2.3");

    helper.assertFalse("1.2.3", "1.0.5");
    helper.assertFalse("1.2.3", "1.1.5");
    helper.assertFalse("1.2.3", "1.2");
    helper.assertFalse("1.2.3", "1");
  }

  @Test
  public void testLessThan() {
    Helper helper = new Helper("<", Constraints::lessThan);

    helper.assertFalse("1.2.3", "1.2.4");
    helper.assertFalse("1.2.3", "1.3.0");
    helper.assertFalse("1.2.3", "2.0.0");
    helper.assertFalse("1.2.3", "1.2.3");

    helper.assertTrue("1.2.3", "1.0.5");
    helper.assertTrue("1.2.3", "1.1.5");
    helper.assertTrue("1.2.3", "1.2");
    helper.assertTrue("1.2.3", "1");
  }

  @Test
  public void testLessThanOrEqualToTo() {
    Helper helper = new Helper("<=", Constraints::lessThanOrEqualTo);

    helper.assertFalse("1.2.3", "1.2.4");
    helper.assertFalse("1.2.3", "1.3.0");
    helper.assertFalse("1.2.3", "2.0.0");

    helper.assertTrue("1.2.3", "1.2.3");
    helper.assertTrue("1.2.3", "1.0.5");
    helper.assertTrue("1.2.3", "1.1.5");
    helper.assertTrue("1.2.3", "1.2");
    helper.assertTrue("1.2.3", "1");
  }

  @Test
  public void testPessimisticallyCompatibleWith() {
    Helper helper = new Helper("~>", Constraints::pessimisticallyCompatibleWith);

    helper.assertTrue("1.3.2", "1.3.2");
    helper.assertTrue("1.3.2", "1.3.3");
    helper.assertTrue("1.3.2", "1.3.20");
    helper.assertTrue("1.3", "1.3.20");

    helper.assertFalse("1.3.2", "1.3.0");
    helper.assertFalse("1.3.2", "1.1.0");
    helper.assertFalse("1.3.2", "1.3");
    helper.assertFalse("1.3.2", "1.1");
    helper.assertFalse("1.3.2", "1");
    helper.assertFalse("1.3.2", "1.4.0");
    helper.assertFalse("1.3.2", "1.4");
    helper.assertFalse("1.3.2", "2");
    helper.assertFalse("1.3.2", "2.0");
    helper.assertFalse("1.3.2", "2.0.0");
    helper.assertFalse("1.3.2", "2.1.0");
  }

  static class Helper {
    private final String operator;
    private final Function<Version, Predicate<Version>> function;

    Helper(String operator, Function<Version, Predicate<Version>> function) {
      this.operator = operator;
      this.function = function;
    }

    void assertTrue(String reference, String test) {
      Assertions.assertTrue(function.apply(Version.of(reference)).test(Version.of(test)));
      Assertions.assertTrue(Constraints.of(String.format("%s %s", operator, reference)).test(Version.of(test)));
    }

    void assertFalse(String reference, String test) {
      Assertions.assertFalse(function.apply(Version.of(reference)).test(Version.of(test)));
      Assertions.assertFalse(Constraints.of(String.format("%s %s", operator, reference)).test(Version.of(test)));
    }

  }

}
