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

import java.util.Objects;

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
public class Version implements Comparable<Version> {

  /// The size reserved for the segment value
  private static final int SEGMENT_SIZE = 20;
  /// The size reserved for the segment flag
  private static final int FLAG_SIZE = 1;
  /// The theoretical maximum value of a segment
  private static final int MAX = (int) (Math.pow(2, SEGMENT_SIZE) - 1);

  /// Where the micro segment starts
  private static final int SEGMENT_MICRO = FLAG_SIZE;
  /// Where the minor segment starts
  private static final int SEGMENT_MINOR = SEGMENT_MICRO + SEGMENT_SIZE + FLAG_SIZE;
  /// Where the major segment starts
  private static final int SEGMENT_MAJOR = SEGMENT_MINOR + SEGMENT_SIZE + FLAG_SIZE;

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
  private final Precision precision;

  /// Creates a new Version object using the given internal representation.
  ///
  /// @param value the internal representation of this version
  /// @see #Version(int)  Version
  /// @see #Version(int, int) Version
  /// @see #Version(int, int, int) Version
  public Version(long value) {
    if (value < 0) {
      throw new IllegalArgumentException("Invalid value");
    }

    boolean knownMicro = (value & 0b0_000000000000000000000_000000000000000000000_000000000000000000001L) ==
      0b0_000000000000000000000_000000000000000000000_000000000000000000001L;

    boolean knownMinor = (value & 0b0_000000000000000000000_000000000000000000001_000000000000000000000L) ==
      0b0_000000000000000000000_000000000000000000001_000000000000000000000L;

    boolean knownMajor = (value & 0b0_000000000000000000001_000000000000000000000_000000000000000000000L) ==
      0b0_000000000000000000001_000000000000000000000_000000000000000000000L;

    // normalizes the internal implementation by zeroing segments that are not part of the precision
    if (knownMicro && knownMinor && knownMajor) {
      this.precision = Precision.MICRO;
      this.value = value;

    } else if (knownMinor && knownMajor) {
      this.precision = Precision.MINOR;
      this.value = value & 0b0_111111111111111111111_111111111111111111111_000000000000000000000L;

    } else if (knownMajor) {
      this.precision = Precision.MAJOR;
      this.value = value & 0b0_111111111111111111111_000000000000000000000_000000000000000000000L;

    } else {
      precision = Precision.NONE;
      this.value = 0;
    }
  }

  /// Creates a version object with all the three segments present.
  ///
  /// @param major the value of the major segment
  /// @param minor the value of the minor segment
  /// @param micro the value of the micro segment
  public Version(int major, int minor, int micro) {
    this(((long) major << SEGMENT_MAJOR | (long) minor << SEGMENT_MINOR | (long) micro << SEGMENT_MICRO) |
      0b0_000000000000000000001_000000000000000000001_000000000000000000001L);

    if (major > MAX || minor > MAX || micro > MAX) {
      throw new IllegalArgumentException("Invalid value");
    }
  }

  /// Creates a version object with only the first two segments.
  ///
  /// @param major the value of the major segment
  /// @param minor the value of the minor segment
  public Version(int major, int minor) {
    this(((long) major << SEGMENT_MAJOR | (long) minor << SEGMENT_MINOR) |
      0b0_000000000000000000001_000000000000000000001_000000000000000000000L);

    if (major > MAX || minor > MAX) {
      throw new IllegalArgumentException("Invalid value");
    }
  }

  /// Creates a version object with only the first segment.
  ///
  /// @param major the value of the major segment
  public Version(int major) {
    this(((long) major << SEGMENT_MAJOR) |
      0b0_000000000000000000001_000000000000000000000_000000000000000000000L);

    if (major > MAX) {
      throw new IllegalArgumentException("Invalid value");
    }
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

  /// Creates a new version with the major segment incremented by <code>one</code>. Preserves the [#precision()].
  ///
  /// @return a new version <code>X+1.Y.Z</code>
  public Version nextMajor() {
    return switch (precision) {
      case NONE -> NULL;
      case MAJOR -> new Version(major() + 1);
      case MINOR -> new Version(major() + 1, minor());
      case MICRO -> new Version(major() + 1, minor(), micro());
    };
  }

  /// Creates a new version with the minor segment incremented by <code>one</code> (if possible).
  /// Preserves the [#precision()].
  ///
  /// @return a new version <code>X.Y+1.Z</code>
  public Version nextMinor() {
    return switch (precision) {
      case NONE -> NULL;
      case MAJOR -> new Version(major());
      case MINOR -> new Version(major(), minor() + 1);
      case MICRO -> new Version(major(), minor() + 1, micro());
    };
  }

  /// Creates a new version with the micro segment incremented by <code>one</code> (if possible).
  /// Preserves the [#precision()].
  ///
  /// @return a new version <code>X.Y.Z+1</code>
  public Version nextMicro() {
    return switch (precision) {
      case NONE -> NULL;
      case MAJOR -> new Version(major());
      case MINOR -> new Version(major(), minor());
      case MICRO -> new Version(major(), minor(), micro() + 1);
    };
  }

  /// Creates a new version with the given value as its major segment.
  ///
  /// @param newMajor the value of the major segment
  /// @return a new version with the given major segment.
  public Version withMajor(int newMajor) {
    return switch (precision) {
      case NONE -> NULL;
      case MAJOR -> new Version(newMajor);
      case MINOR -> new Version(newMajor, minor());
      case MICRO -> new Version(newMajor, minor(), micro());
    };
  }

  /// Creates a new version with the given value as its minor segment.
  ///
  /// @param newMinor the value of the minor segment
  /// @return a new version with the given minor segment.
  public Version withMinor(int newMinor) {
    return switch (precision) {
      case NONE -> NULL;
      case MAJOR, MINOR -> new Version(major(), newMinor);
      case MICRO -> new Version(major(), newMinor, micro());
    };
  }

  /// Creates a new version with the given value as its micro segment.
  ///
  /// @param newMicro the value of the micro segment
  /// @return a new version with the given micro segment.
  public Version withMicro(int newMicro) {
    if (precision == Precision.NONE) {
      return Version.NULL;
    }
    return new Version(major(), minor(), newMicro);
  }

  /// Return the precision which this version value is stored.
  ///
  /// @return the precision of this version value.
  public Precision precision() {
    return precision;
  }

  private int segment(int segmentFactor) {
    return (int) ((this.value >>> segmentFactor) & 0b0_000000000000000000000_000000000000000000000_011111111111111111111);
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
    return switch (precision) {
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
  public static Version of(String value) {
    if (value == null || value.isBlank()) {
      return NULL;
    }

    String[] tokens = value.split("\\.", 3);
    int[] values = new int[3];

    for (int i = 0; i < values.length && i < tokens.length; i++) {
      String segment = tokens[i].replaceAll("\\D.*", "");
      if (!segment.isBlank()) {
        try {
          values[i] = Integer.parseInt(segment);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid input", e);
        }
      } else {
        throw new IllegalArgumentException("Invalid input");
      }
    }

    final Version result;

    if (tokens.length == 3) {
      result = new Version(
        values[0],
        values[1],
        values[2]
      );
    } else if (tokens.length == 2) {
      result = new Version(
        values[0],
        values[1]
      );
    } else if (tokens.length == 1) {
      result = new Version(
        values[0]
      );
    } else {
      throw new IllegalArgumentException("Invalid input");
    }

    return result;
  }

}
