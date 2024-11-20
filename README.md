# Version™

Version™ is a lightweight implementation of a version schema that uses three segments: major, minor and micro.
Three segments, all starting with the letter 'M'. Three... M... TM...

Hello? Are you still here? That's what happens when I try to name things (please don't ask me how I invalidate caches).

Anyway, this library was written because of a necessity to index and search version numbers. While indexing Strings
could work fine, I wanted to use a simplistic approach.

## Internal Structure

Version™ works by allocating a `long` value for all the three segments plus one flag per each. By mashing everything
together into a single number, operations like searching, comparing and indexing become much simpler.

The segments are stored using the following structure:

```
0[major segment: 20 bits][flag: 1 bit][minor segment: 20 bits][flag: 1 bit][micro segment: 20 bits][flag: 1 bit]
```

The most significant bit is left untouched to avoid having to deal with negative numbers (and blowing up the whole idea
of simplicity). Each segment will be stored using 20 bits for its value and 1 bit as a flag to indicate whether that
particular segment is part of the version number. Since 20 bits are allocated for each segment, they can hold up to a
value of `(2 ^ 20) - 1 = 1_048_575`. It's probably enough for the majority of the cases.

The usage of segment flags allows versions with just the major segment or with the major and minor segments while still
making them comparable in some sort of way. A version with more segments is more precise, and thus will have a greater
value (`1.0.0 > 1.0 > 1`).

Suppose you have the version `1.0.0`. When we break down the segments, we have:

- `major`: `0b0011`
- `minor`: `0b0001`
- `micro`: `0b0001`

Appending the segments will give us the `long` representation, which is:

```
0_00000000000000000001_1_00000000000000000000_1_00000000000000000000_1 => 13_194_141_630_465
 |   major  segment   |f|   minor  segment   |f|   micro  segment   |f
```

The version `1.0` will have its value as (note the absence of the flag in the last segment):

```
0_00000000000000000001_1_00000000000000000000_1_00000000000000000000_0 => 13_194_141_630_464
 |   major  segment   |f|   minor  segment   |f|   micro  segment   |f
```

The version `1` will have its value as (note the absence of two flags):

```
0_00000000000000000001_1_00000000000000000000_0_00000000000000000000_0 => 13_194_139_533_312
 |   major  segment   |f|   minor  segment   |f|   micro  segment   |f
```

Since `13_194_141_630_465 > 13_194_141_630_464 > 13_194_139_533_312`, we can infer that `1.0.0 > 1.0 > 1`.

Now, suppose you have the version `2.4.3`. When we break down the segments, we have:

- `major`: `0b0101`
- `minor`: `0b1001`
- `micro`: `0b0111`

Appending the segments will give us the `long` representation, which is:

```
0_00000000000000000010_1_00000000000000000100_1_00000000000000000011_1 => 21_990_251_429_895
 |   major  segment   |f|   minor  segment   |f|   micro  segment   |f
```

The version `2.3.9` will have its value as:

```
0_00000000000000000010_1_00000000000000000011_1_00000000000000001001_1 => 21_990_247_235_603
 |   major  segment   |f|   minor  segment   |f|   micro  segment   |f
```

Since `21_990_251_429_895 > 21_990_247_235_603`, we can conclude that `2.4.3 > 2.3.9`.

## Usage

The whole usage boils down to only three classes: `Version`, `Precision` and `Constraints`.

```java
// you can call one of the overloaded constructors that accept 1, 2 or 3 segments
Version a = new Version(1, 0, 2);
// or you can pass a String to the helper method (check the docs for usage help)
Version b = Version.of("2.0");

// constraints only versions that are greater or equal to 2.0.0
Predicate<Version> constraint = Constraints.create(">= 2.0.0");

System.out.println(constraint.test(a)); // false
System.out.println(constraint.test(b)); // false ... WTF?!??!
// remember, 2.0.0 > 2.0 > 2

constraint = Constraints.create(">= 2");

System.out.println(constraint.test(b)); // now it's true :)

// or you could have created the version from a String, but enforcing a specific precision
b = Version.of("2.0", Precision.MICRO);

constraint = Constraints.create(">= 2.0.0");
System.out.println(constraint.test(b)); // now it's also true :)
```

You can get the bits from the Maven Central using the following:

- `groupId`: com.backpackcloud
- `artifactId`: version-tm

## Why UTC?

Why should you Use This Crap™? Well... I don't know exactly why. Maybe you need something really light that can get the
job done.

I will give you my use case for building this.

I had to cache and index a variety of different versions from software runtimes to query later on. Those queries had a
bunch of different patterns, as I was searching for usage and also CVE exposure. Since all the different versions of the
software products I was analyzing could be tossed on three segments, I did a search across libraries that could parse a
Semantic Version in a way that I could store and play around with them.

I don't know if there's something as simple as this, but I couldn't find, and turns out the exercise of solving this
was really fun, and I've decided to publish the source code just to have at least one piece of useful-ish thing that
I could be proud of showing to my kids.

I don't intend to make this comply to the Semantic Versioning, but this could be used as a core for making such thing.
The idea is to have something easy to work with that can also be stored as the easiest possible thing to compare: a
freaking **number**.
