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
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

/// A simple and lightweight three-segment version implementation.
///
/// The segments are "major", "minor" and "micro". They are stored on a single <code>long</code> field with a
/// predetermined length of 20 bits for each segment. That means the theoretical maximum value of a segment is
/// <code>(2 ^ 20) - 1 = 1_048_576</code>.
///
/// It's possible to have a version object without the three segments, and that won't change their position
/// in the field, so the objects are comparable regardless of how many segments they have.
///
/// By convention, the absence of a segment is treated as less value than the segment of a value <code>zero</code>,
/// so the version <code>2.0.0</code> is greater than the version <code>2.0</code>.
///
/// @author Marcelo "Ataxexe" Guimarães
public class Version implements Serializable, Comparable<Version> {

  @Serial
  private static final long serialVersionUID = 2280347665608754965L;

  /// The size reserved for the segment value
  private static final int SEGMENT_SIZE = 20;
  /// The size reserved for the segment flag
  private static final int FLAG_SIZE = 1;

  /// The theoretical maximum value of a segment
  public static final int MAX_SEGMENT_VALUE = (int) (Math.pow(2, SEGMENT_SIZE) - 1);
  /// The theoretical minimum value of a segment
  public static final int MIN_SEGMENT_VALUE = 0;

  /// Where the micro segment starts
  private static final int SEGMENT_MICRO = FLAG_SIZE;
  /// Where the minor segment starts
  private static final int SEGMENT_MINOR = SEGMENT_MICRO + SEGMENT_SIZE + FLAG_SIZE;
  /// Where the major segment starts
  private static final int SEGMENT_MAJOR = SEGMENT_MINOR + SEGMENT_SIZE + FLAG_SIZE;

  ///  The mask used to determine if the segment flags are on only for the major segment
  private static final long MAJOR_PRECISION_MASK = 1L << (SEGMENT_MAJOR - FLAG_SIZE);
  ///  The mask used to determine if the segment flags are on for the major and minor segments
  private static final long MINOR_PRECISION_MASK = (1L << (SEGMENT_MINOR - FLAG_SIZE)) | MAJOR_PRECISION_MASK;
  ///  The mask used to determine if all the segment flags are on
  private static final long MICRO_PRECISION_MASK = (1L << (SEGMENT_MICRO - FLAG_SIZE)) | MINOR_PRECISION_MASK;

  /// The representation of a Version <code>null</code>.
  public static final Version NULL = new Version(0L);

  /// Stores all the information about this version.
  ///
  /// There's a total of 21 bits allocated to each segment, being the first one a flag to indicate whether
  /// the segment is counted. This allows us to use zeroes as both a value (when the flag is set to <code>one</code>)
  /// or nothing (when the flag is set to <code>zero</code>).
  ///
  /// The remaining 20 bits are used to store the actual segment value. This leaves us with exactly 63 bits to store
  /// the values and flags, saving the last bit to avoid dealing with negative numbers.
  private final long value;

  /// Creates a new Version object using the given internal representation.
  ///
  /// @param value the internal representation of this version
  /// @see #Version(int)  Version
  /// @see #Version(int, int) Version
  /// @see #Version(int, int, int) Version
  public Version(long value) {
    if (value < 0) {
      throw new IllegalArgumentException("Invalid value: " + value + " < 0");
    }

    // normalizes the internal implementation by zeroing segments that are not part of the precision
    if ((value & MICRO_PRECISION_MASK) == MICRO_PRECISION_MASK) {
      this.value = value;

    } else if ((value & MINOR_PRECISION_MASK) == MINOR_PRECISION_MASK) {
      this.value = value & ((~0L) << SEGMENT_SIZE + FLAG_SIZE);

    } else if ((value & MAJOR_PRECISION_MASK) == MAJOR_PRECISION_MASK) {
      this.value = value & ~((~0L) << SEGMENT_SIZE + FLAG_SIZE) << ((SEGMENT_SIZE + FLAG_SIZE) * 2);

    } else {
      this.value = 0;
    }
  }

  /// Creates a version object with all the three segments present.
  ///
  /// @param major the value of the major segment
  /// @param minor the value of the minor segment
  /// @param micro the value of the micro segment
  /// @throws InvalidSegmentException if any segment is not within the accepted range
  /// @see #MAX_SEGMENT_VALUE
  /// @see #MIN_SEGMENT_VALUE
  public Version(int major, int minor, int micro) {
    this(checkSegments(major, minor, micro,
        () -> ((long) major << SEGMENT_MAJOR | (long) minor << SEGMENT_MINOR | (long) micro << SEGMENT_MICRO) | MICRO_PRECISION_MASK)
    );
  }

  /// Creates a version object with only the first two segments.
  ///
  /// @param major the value of the major segment
  /// @param minor the value of the minor segment
  /// @throws InvalidSegmentException if any segment is not within the accepted range
  /// @see #MAX_SEGMENT_VALUE
  /// @see #MIN_SEGMENT_VALUE
  public Version(int major, int minor) {
    this(checkSegments(major, minor, 0,
        () -> ((long) major << SEGMENT_MAJOR | (long) minor << SEGMENT_MINOR) | MINOR_PRECISION_MASK)
    );
  }

  /// Creates a version object with only the first segment.
  ///
  /// @param major the value of the major segment
  /// @throws InvalidSegmentException if any segment is not within the accepted range
  /// @see #MAX_SEGMENT_VALUE
  /// @see #MIN_SEGMENT_VALUE
  public Version(int major) {
    this(checkSegments(major, 0, 0,
        () -> ((long) major << SEGMENT_MAJOR) | MAJOR_PRECISION_MASK)
    );
  }

  private static long checkSegments(int major, int minor, int micro, Supplier<Long> supplier) {
    if (major > MAX_SEGMENT_VALUE || major < MIN_SEGMENT_VALUE) {
      throw new InvalidSegmentException(major, Version.NULL);
    }
    if (minor > MAX_SEGMENT_VALUE || minor < MIN_SEGMENT_VALUE) {
      throw new InvalidSegmentException(minor, new Version(major));
    }
    if (micro > MAX_SEGMENT_VALUE || micro < MIN_SEGMENT_VALUE) {
      throw new InvalidSegmentException(micro, new Version(major, minor));
    }
    return supplier.get();
  }

  /// Returns the internal representation of this version, for storing purposes.
  ///
  /// @return the internal representation of this version.
  public long value() {
    return value;
  }

  /// Returns the major segment of this version.
  ///
  /// @return the major segment
  public int major() {
    return segment(SEGMENT_MAJOR);
  }

  /// Returns the minor segment of this version. If this version doesn't have a minor segment, <code>zero</code>
  /// is returned.
  ///
  /// @return the minor segment
  /// @see #precision()
  public int minor() {
    return segment(SEGMENT_MINOR);
  }

  /// Returns the micro segment of this version. If this version doesn't have a micro segment, <code>zero</code>
  /// is returned.
  ///
  /// @return the micro segment
  /// @see #precision()
  public int micro() {
    return segment(SEGMENT_MICRO);
  }

  /// Return the precision which this version value is stored.
  ///
  /// @return the precision of this version value.
  public Precision precision() {
    if ((value & MICRO_PRECISION_MASK) == MICRO_PRECISION_MASK) {
      return Precision.MICRO;
    }

    if ((value & MINOR_PRECISION_MASK) == MINOR_PRECISION_MASK) {
      return Precision.MINOR;
    }

    if ((value & MAJOR_PRECISION_MASK) == MAJOR_PRECISION_MASK) {
      return Precision.MAJOR;
    }

    return Precision.NONE;
  }

  private int segment(int segmentFactor) {
    return (int) ((this.value >>> segmentFactor) & ~((~0) << SEGMENT_SIZE));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Version that)) return false;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public int compareTo(Version o) {
    return Long.compare(this.value, o.value);
  }

  @Override
  public String toString() {
    return switch (precision()) {
      case NONE -> "null";
      case MAJOR -> String.valueOf(major());
      case MINOR -> String.format("%d.%d", major(), minor());
      case MICRO -> String.format("%d.%d.%d", major(), minor(), micro());
    };
  }

  /// Tries to parse the given value in the format <code>major.minor.micro</code>. Returns [#NULL]
  /// if it's not possible to parse the input.
  ///
  /// @param value the value to parse
  /// @return the Version representation of the given string, or [#NULL] if not possible.
  /// @see #of(String, Precision)
  public static Version of(String value) {
    return of(value, Precision.NONE);
  }

  /// Tries to parse the given value in the format <code>major.minor.micro</code>. Returns [#NULL]
  /// if it's not possible to parse the input.
  ///
  /// @param value             the value to parse
  /// @param enforcedPrecision the precision to enforce by filling missing segments with zeroes
  /// @return the Version representation of the given string, or [#NULL] if not possible.
  /// @see #of(String)
  public static Version of(String value, Precision enforcedPrecision) {
    if (value == null || value.isBlank()) {
      return NULL;
    }

    String[] tokens = value.trim().split("\\.", 3);
    int precision = enforcedPrecision == Precision.NONE ? tokens.length : enforcedPrecision.segments();

    int[] values = new int[3];

    for (int i = 0; i < values.length && i < tokens.length; i++) {
      // I don't care about anything that's not a digit
      String segment = tokens[i]
        // first, remove any non-digits from the beginning
        .replaceAll("^\\D", "")
        // after that, remove anything after the first non-digit until the end
        .replaceAll("\\D.*$", "");

      if (!segment.isBlank()) {
        try {
          values[i] = Integer.parseInt(segment);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid input: '" + value + "'", e);
        }
      } else if (i > 0 && i == tokens.length - 1) {
        // if this is the last token, and it's invalid
        // let's lower the precision and let it pass
        precision--;
      } else {
        throw new IllegalArgumentException("Invalid input: '" + value + "'");
      }
    }

    return create(precision, values);
  }

  private static Version create(int precision, int[] values) {
    return switch (precision) {
      case 3 -> new Version(values[0], values[1], values[2]);
      case 2 -> new Version(values[0], values[1]);
      case 1 -> new Version(values[0]);
      default -> throw new IllegalArgumentException("Invalid precision");
    };
  }

}
