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

/// A collection of useful predicates to build version constraints.
///
/// @author Marcelo "Ataxexe" Guimarães
public final class Constraints {

  private Constraints() {

  }

  /// @param reference the version to use as a reference point
  /// @return a predicate that only allows exact versions (including the precision).
  public static Predicate<Version> equalTo(Version reference) {
    return version -> version.compareTo(reference) == 0;
  }

  /// @param reference the version to use as a reference point
  /// @return a predicate that only allows versions different from the reference (including the precision).
  public static Predicate<Version> differentFrom(Version reference) {
    return version -> version.compareTo(reference) != 0;
  }

  /// @param reference the version to use as a reference point
  /// @return a predicate that only allows versions with a value less than the value of the reference
  /// (regardless of the precision)
  public static Predicate<Version> lessThan(Version reference) {
    return version -> version.compareTo(reference) < 0;
  }

  /// @param reference the version to use as a reference point
  /// @return a predicate that only allows versions with a value less than or equal the value of the reference
  /// (regardless of the precision)
  public static Predicate<Version> lessThanOrEqualTo(Version reference) {
    return lessThan(reference).or(equalTo(reference));
  }

  /// @param reference the version to use as a reference point
  /// @return a predicate that only allows versions with a value greater than the value of the reference
  /// (regardless of the precision)
  public static Predicate<Version> greaterThan(Version reference) {
    return version -> version.compareTo(reference) > 0;
  }

  /// @param reference the version to use as a reference point
  /// @return a predicate that only allows versions with a value greater than or equal the value of the reference
  /// (regardless of the precision)
  public static Predicate<Version> greaterThanOrEqualTo(Version reference) {
    return greaterThan(reference).or(equalTo(reference));
  }

  /// Creates a new range using the provided values.
  ///
  /// @param min        the minimum value of the range
  /// @param max        the maximum value of the range
  /// @param includeMin if the minimum value is part of the range
  /// @param includeMax if the maximum value is part of the range
  public static Predicate<Version> range(Version min, Version max, boolean includeMin, boolean includeMax) {
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

    return predicate;
  }

  /// Creates a new Range based on the given notation.
  ///
  /// The notation uses the following characters for each part of the range:
  ///
  /// - `[` at the beginning: if the minimum value is part of the range
  /// - `]` or `(` at the beginning: if the minimum value is not part of the range
  /// - `]` at the end: if the maximum value is part of the range
  /// - `[` or `)` at the beginning: if the maximum value is not part of the range
  ///
  /// So, a notation of `[1.0, 1.4]` creates a Range that goes from `1.0` to `1.4`, and includes
  /// both sides. A notation of `[1.1.2, 1.2)` creates a Range that goes from `1.1.2` to `1.2`,
  /// and includes only `1.1.2`, so the value `1.2` is outside the Range.
  ///
  /// @param notation the notation of the range
  /// @return the created Range that satisfies the given notation.
  /// @see #range(String, Precision)
  public static Predicate<Version> range(String notation) {
    return range(notation, Precision.NONE);
  }

  /// Creates a new Range based on the given notation.
  ///
  /// The notation uses the following characters for each part of the range:
  ///
  /// - `[` at the beginning: if the minimum value is part of the range
  /// - `]` or `(` at the beginning: if the minimum value is not part of the range
  /// - `]` at the end: if the maximum value is part of the range
  /// - `[` or `)` at the beginning: if the maximum value is not part of the range
  ///
  /// So, a notation of `[1.0, 1.4]` creates a Range that goes from `1.0` to `1.4`, and includes
  /// both sides. A notation of `[1.1.2, 1.2)` creates a Range that goes from `1.1.2` to `1.2`,
  /// and includes only `1.1.2`, so the value `1.2` is outside the Range.
  ///
  /// @param notation          the notation of the range
  /// @param enforcedPrecision the precision to enforce by filling up missing segments with zeroes
  /// @return the created Range that satisfies the given notation.
  /// @see #range(String)
  public static Predicate<Version> range(String notation, Precision enforcedPrecision) {
    Version min, max;
    boolean includeMin, includeMax;

    String[] tokens = notation.split("\\s*,\\s*", 2);

    String minInput = tokens[0];
    String maxInput = tokens[1];

    if (minInput.charAt(0) == '[') {
      includeMin = true;
    } else if (minInput.charAt(0) == '(' || minInput.charAt(0) == ']') {
      includeMin = false;
    } else {
      throw new IllegalArgumentException("Invalid range format: '" + notation + "'");
    }

    min = Version.of(minInput.substring(1), enforcedPrecision);

    if (maxInput.charAt(maxInput.length() - 1) == ']') {
      includeMax = true;
    } else if (maxInput.charAt(maxInput.length() - 1) == ')' || maxInput.charAt(maxInput.length() - 1) == '[') {
      includeMax = false;
    } else {
      throw new IllegalArgumentException("Invalid range format: '" + notation + "'");
    }

    max = Version.of(maxInput.substring(0, maxInput.length() - 1), enforcedPrecision);

    return range(min, max, includeMin, includeMax);
  }

  /// Creates a predicate for pessimistic constraints.
  ///
  /// A pessimistic constraint allows versions up to a point where they might introduce breaking changes.
  /// So, versions with precision {@link Precision#MICRO} or {@link Precision#MINOR} increment to
  /// `major.minor+1` (note the absence of the micro segment for both cases), and versions with precision
  /// {@link Precision#MAJOR} increment to `major+1`.
  ///
  /// @param reference the version to use as a reference point
  /// @return a predicate that allows versions compatible with the reference in a pessimistic way
  public static Predicate<Version> pessimisticallyCompatibleWith(Version reference) {
    Version limit = switch (reference.precision()) {
      case NONE -> Version.NULL;
      case MAJOR -> new Version(reference.major() + 1);
      case MINOR, MICRO -> new Version(reference.major(), reference.minor() + 1);
    };
    return range(reference, limit, true, false);
  }

  /// A central point for creating the constraints provided by this class using notations.
  ///
  /// The supported notations are:
  ///
  /// - {@link #range(String) range}
  /// - `>= {version}` - {@link #greaterThanOrEqualTo(Version)}
  /// - `> {version}` - {@link #greaterThan(Version)}
  /// - `= {version}` - {@link #equalTo(Version)}
  /// - `!= {version}` - {@link #differentFrom(Version)}
  /// - `< {version}` - {@link #lessThan(Version)}
  /// - `<= {version}` - {@link #lessThanOrEqualTo(Version)}
  /// - `~> {version}` - {@link #pessimisticallyCompatibleWith(Version)}
  ///
  /// @param notation the notation of the constraint to create
  /// @see #create(String, Precision)
  public static Predicate<Version> create(String notation) {
    return create(notation, Precision.NONE);
  }

  /// A central point for creating the constraints provided by this class using notations.
  ///
  /// The supported notations are:
  ///
  /// - {@link #range(String) range}
  /// - `>= {version}` - {@link #greaterThanOrEqualTo(Version)}
  /// - `> {version}` - {@link #greaterThan(Version)}
  /// - `= {version}` - {@link #equalTo(Version)}
  /// - `!= {version}` - {@link #differentFrom(Version)}
  /// - `< {version}` - {@link #lessThan(Version)}
  /// - `<= {version}` - {@link #lessThanOrEqualTo(Version)}
  /// - `~> {version}` - {@link #pessimisticallyCompatibleWith(Version)}
  ///
  /// @param notation          the notation of the constraint to create
  /// @param enforcedPrecision the precision to enforce by filling up missing segments with zeroes
  /// @see #create(String)
  public static Predicate<Version> create(String notation, Precision enforcedPrecision) {
    if (notation == null) {
      return version -> false;
    }
    notation = notation.trim();
    if (notation.startsWith("[") || notation.startsWith("(") || notation.startsWith("]")) {
      return range(notation);
    } else {
      String[] tokens = notation.split("\\s+", 2);

      // if no notation, assumes equalTo
      if (tokens.length == 1) {
        return equalTo(Version.of(tokens[0], enforcedPrecision));
      }
      if (tokens.length != 2) {
        throw new IllegalArgumentException("Invalid notation: '" + notation + "'");
      }

      String operator = tokens[0];
      Version reference = Version.of(tokens[1], enforcedPrecision);

      return switch (operator) {
        case ">=" -> greaterThanOrEqualTo(reference);
        case ">" -> greaterThan(reference);
        case "=" -> equalTo(reference);
        case "!=" -> differentFrom(reference);
        case "<" -> lessThan(reference);
        case "<=" -> lessThanOrEqualTo(reference);
        case "~>" -> pessimisticallyCompatibleWith(reference);
        default -> throw new IllegalArgumentException("Invalid notation: '" + notation + "'");
      };
    }
  }

}
