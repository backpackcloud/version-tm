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

/// Enumeration of the possible precision in regard to accurate segments.
///
/// The precision essentially tells which segments are used to determine a version value.
/// A segment might hold the value <code>zero</code>, but doesn't necessarily imply that
/// it's used to either format the version or to check its precedence with other versions.
///
/// @author Ataxexe
public enum Precision {

  /// Indicates no precision at all.
  ///
  /// If a {@link Version} has this precision, it basically means it's {@link Version#NULL}
  NONE,
  /// Indicates that only the major segment is accurate.
  MAJOR,
  /// Indicates that only the major and minor segments are accurate.
  MINOR,
  /// Indicates that all three segments are accurate.
  MICRO;

  /// @return the number of segments this precision ensures to be accurate
  public int segments() {
    return ordinal();
  }

  public Precision less() {
    if (this == NONE || this == MAJOR) {
      return this;
    }
    return fromSegments(segments() - 1);
  }

  public Precision more() {
    if (this == MICRO) {
      return this;
    }
    return fromSegments(segments() + 1);
  }

  public static Precision fromSegments(int segments) {
    if (segments < 0 || segments > 3) {
      throw new IllegalArgumentException("Invalid segments: " + segments);
    }
    return values()[segments];
  }

}
