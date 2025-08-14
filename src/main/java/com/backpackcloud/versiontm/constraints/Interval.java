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

import java.util.Optional;
import java.util.stream.Stream;

public enum Interval {

  CLOSED_CLOSED("|-|") {
    @Override
    public Constraint createConstraint(Version lower, Version upper) {
      return new Range(Limit.inclusive(lower), Limit.inclusive(upper));
    }
  },
  CLOSED_OPEN("|-") {
    @Override
    public Constraint createConstraint(Version lower, Version upper) {
      return new Range(Limit.inclusive(lower), Limit.exclusive(upper));
    }
  },
  OPEN_CLOSED("-|") {
    @Override
    public Constraint createConstraint(Version lower, Version upper) {
      return new Range(Limit.exclusive(lower), Limit.inclusive(upper));
    }
  },
  OPEN_OPEN("--") {
    @Override
    public Constraint createConstraint(Version lower, Version upper) {
      return new Range(Limit.exclusive(lower), Limit.exclusive(upper));
    }
  };

  private final String symbol;

  Interval(String symbol) {
    this.symbol = symbol;
  }

  public String symbol() {
    return symbol;
  }

  public abstract Constraint createConstraint(Version lower, Version upper);

  public static Optional<Interval> ofSymbol(String symbol) {
    return Stream.of(values())
      .filter(operation -> operation.symbol().equals(symbol))
      .findFirst();
  }

}
