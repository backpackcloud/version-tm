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

import java.io.Serial;

/// Exception thrown when an attempt to create a Version is made using an invalid value
/// for any of its segments.
///
/// @see Version#MAX_SEGMENT_VALUE
/// @see Version#MIN_SEGMENT_VALUE
/// @since 1.1
/// @author Marcelo "Ataxexe" Guimarães
public class InvalidSegmentException extends IllegalArgumentException {

  @Serial
  private static final long serialVersionUID = -2742064623233614453L;

  private final Version coercedVersion;

  /// Creates a new instance of this exception
  ///
  /// @param value          the invalid value
  /// @param coercedVersion a coerced version made by discarding this input, might be {@link Version#NULL}
  public InvalidSegmentException(int value, Version coercedVersion) {
    super(String.valueOf(value));
    this.coercedVersion = coercedVersion;
  }

  /// @return a valid Version object that doesn't contain the invalid segment. Might be {@link Version#NULL}.
  public Version coercedVersion() {
    return coercedVersion;
  }

}
