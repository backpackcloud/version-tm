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

import com.backpackcloud.versiontm.constraints.Comparison;
import com.backpackcloud.versiontm.constraints.Compatibility;
import com.backpackcloud.versiontm.constraints.Interval;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// Represents a constraint used to evaluate Version objects.
///
/// @author Marcelo "Ataxexe" Guimarães
/// @since 2.0
public interface Constraint extends Predicate<Version> {

  /// The pattern used to parse the expressions.
  Pattern NOTATION_PATTERN = Pattern.compile("(?<left>[0-9.]+)?\\s*(?<operation>[~=\\-|<>!]+)\\s*(?<right>[0-9.]+)");

  /// A central point for creating the constraints provided by this library using notations. Doesn't enforce precision.
  ///
  /// @param notation the notation of the constraint to create
  /// @see #create(String, Precision)
  static Constraint create(String notation) {
    return create(notation, Precision.NONE);
  }

  /// A central point for creating the constraints provided by this class using notations.
  ///
  /// Notations can be of two types:
  ///
  /// - One operand: `symbol version` (example: `> 3.2`, `~> 4.2`)
  /// - Two operands: `version1 symbol version2` (example: `1.0 |- 2.0`)
  ///
  /// The recognized symbols are defined in each of the following constraint enumerations:
  ///
  /// - [Comparison] - for simple comparisons with the raw version value (without considering segments)
  /// - [Interval] - for ranges of versions
  /// - [Compatibility] - for comparisons involving segments
  ///
  /// @param inputNotation     the notation of the constraint to create
  /// @param enforcedPrecision the precision to enforce
  /// @see #create(String)
  static Constraint create(String inputNotation, Precision enforcedPrecision) {
    if (inputNotation == null || inputNotation.isBlank()) {
      return version -> false;
    }
    String notation = inputNotation.trim();

    Matcher matcher = NOTATION_PATTERN.matcher(notation);

    if (matcher.matches()) {
      String leftInput = matcher.group("left");
      String rightInput = matcher.group("right");
      String operation = matcher.group("operation");

      Version left = leftInput == null ? Version.NULL : Version.of(leftInput, enforcedPrecision);
      Version right = Version.of(rightInput, enforcedPrecision);

      return Comparison.ofSymbol(operation)
        .map(op -> op.createConstraint(right))
        .or(() -> Interval.ofSymbol(operation)
          .map(interval -> interval.createConstraint(left, right)))
        .or(() -> Compatibility.ofSymbol(operation)
          .map(interval -> interval.createConstraint(right)))
        .orElseThrow(() -> new IllegalArgumentException("Invalid notation: " + notation));
    }
    if (Character.isDigit(notation.charAt(0))) {
      return Comparison.EQUAL.createConstraint(Version.of(notation, enforcedPrecision));
    }
    throw new IllegalArgumentException("Invalid notation: '" + notation + "'");
  }

}
