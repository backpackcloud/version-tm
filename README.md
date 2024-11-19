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

