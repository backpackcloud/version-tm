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
    return new Range(reference, limit, true, false);
  }

  public static Predicate<Version> of(String input) {
    input = input.trim();
    if (input.startsWith("[") || input.startsWith("(") || input.startsWith("]")) {
      return Range.of(input);
    } else {
      String[] tokens = input.split("\\s+", 2);

      if (tokens.length != 2) {
        throw new IllegalArgumentException("Invalid input");
      }

      String operator = tokens[0];
      Version reference = Version.of(tokens[1]);

      return switch (operator) {
        case ">=" -> greaterThanOrEqualTo(reference);
        case ">" -> greaterThan(reference);
        case "=" -> equalTo(reference);
        case "!=" -> differentFrom(reference);
        case "<" -> lessThan(reference);
        case "<=" -> lessThanOrEqualTo(reference);
        case "~>" -> pessimisticallyCompatibleWith(reference);
        default -> throw new IllegalArgumentException("Invalid operator");
      };
    }
  }

}
