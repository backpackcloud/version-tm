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

package com.backpackcloud.versiontm.constraints;

import com.backpackcloud.versiontm.Constraint;
import com.backpackcloud.versiontm.Version;

/// A constraint represented a range of Versions.
///
/// Ranges are composed of a lower and an upper [limit][Limit]. Each can be included
/// or excluded from the range itself.
///
/// @param lower the lower limit of the range
/// @param upper the upper limit of the range
/// @author Marcelo "Ataxexe" Guimarães
/// @see Limit
/// @see Interval
/// @since 2.0
public record Range(Limit lower, Limit upper) implements Constraint {

  /// Creates a new Range by including the lower limit and excluding the upper limit.
  ///
  /// @param lowerLimit the lower limit of the range (will be included in the range)
  /// @param upperLimit the upper limit of the range (will be excluded from the range)
  public Range(Version lowerLimit, Version upperLimit) {
    this(Limit.inclusive(lowerLimit), Limit.exclusive(upperLimit));
  }

  @Override
  public boolean test(Version version) {
    long lowerValue = lower.value().value();
    long upperValue = upper.value().value();
    long targetValue = version.value();

    return switch (lower.type()) {
      case INCLUSIVE -> switch (upper.type()) {
        case INCLUSIVE -> targetValue >= lowerValue && targetValue <= upperValue;
        case EXCLUSIVE -> targetValue >= lowerValue && targetValue < upperValue;
      };
      case EXCLUSIVE -> switch (upper.type()) {
        case INCLUSIVE -> targetValue > lowerValue && targetValue <= upperValue;
        case EXCLUSIVE -> targetValue > lowerValue && targetValue < upperValue;
      };
    };
  }

}
