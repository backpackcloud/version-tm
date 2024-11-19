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

import java.util.function.Predicate;

public class Range implements Predicate<Version> {

  private final Predicate<Version> predicate;
  private final Version min;
  private final Version max;
  private final boolean includeMin;
  private final boolean includeMax;

  public Range(Version min, Version max, boolean includeMin, boolean includeMax) {
    this.min = min;
    this.max = max;
    this.includeMin = includeMin;
    this.includeMax = includeMax;

    Predicate<Version> predicate;

    if (includeMin) {
      predicate = Constraints.greaterThanOrEqualTo(min);
    } else {
      predicate = Constraints.greaterThan(min);
    }

    if (includeMax) {
      predicate = predicate.and(Constraints.lessThanOrEqualTo(max));
    } else {
      predicate = predicate.and(Constraints.lessThan(max));
    }

    this.predicate = predicate;
  }

  public Version min() {
    return min;
  }

  public Version max() {
    return max;
  }

  public boolean includeMin() {
    return includeMin;
  }

  public boolean includeMax() {
    return includeMax;
  }

  /// Alias to {@link #test(Version)}
  public boolean include(Version version) {
    return test(version);
  }

  @Override
  public boolean test(Version version) {
    return predicate.test(version);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    if (includeMin) {
      builder.append("[");
    } else {
      builder.append("(");
    }

    builder.append(min).append(", ").append(max);

    if (includeMax) {
      builder.append("]");
    } else {
      builder.append(")");
    }

    return builder.toString();
  }

  public static Range of(String input) {
    Version min, max;
    boolean includeMin, includeMax;

    String[] tokens = input.split("\\s*,\\s*", 2);

    String minInput = tokens[0];
    String maxInput = tokens[1];

    if (minInput.charAt(0) == '[') {
      includeMin = true;
    } else if (minInput.charAt(0) == '(' || minInput.charAt(0) == ']') {
      includeMin = false;
    } else {
      throw new IllegalArgumentException("Invalid range format");
    }

    min = Version.of(minInput.substring(1));

    if (maxInput.charAt(maxInput.length() - 1) == ']') {
      includeMax = true;
    } else if (maxInput.charAt(maxInput.length() - 1) == ')' || maxInput.charAt(maxInput.length() - 1) == '[') {
      includeMax = false;
    } else {
      throw new IllegalArgumentException("Invalid range format");
    }

    max = Version.of(maxInput.substring(0, maxInput.length() - 1));

    return new Range(min, max, includeMin, includeMax);
  }

}
