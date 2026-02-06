/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package org.aavso.tools.vstar.vela;

/**
 * VeLa standard library functions.
 */
public class VeLaStandardLibrary {

    private static final VeLaStandardLibrary stdLib = new VeLaStandardLibrary();

    public static VeLaStandardLibrary getInstance() {
        return stdLib;
    }

    public String getStdLibStr() {
        StringBuffer code = new StringBuffer();

        code.append(forFunctions());
        code.append(filterFunctions());
        code.append(mapReduceFunctions());
        code.append(reverseFunctions());
        code.append(timeFunctions());
        code.append(zipFunctions());

        return code.toString();
    }

    private String forFunctions() {
        return """
                # FOR
                for_(f:λ xs:list) {
                  index <- 0
                  while index < length(xs) {
                    f(nth(xs index))
                    index <- index + 1
                  }
                }

                # FOREACH
                # TODO: should this be ACCUMULATE or ACCUM or FOLD or REDUCE (see below!)?
                # This is equivalent to a list comprehension without the optional filter.
                foreach(xs:list f:λ accum:list) : list {
                  index <- 0
                  while index < length(xs) {
                    accum <- append(accum f(nth(xs index)))
                    index <- index + 1
                  }
                  accum
                }

                """;
    }

    private String filterFunctions() {
        return """
                # FILTER
                filter_(xs:list predicate:λ) : list {
                  accum <- []
                  index <- 0
                  while index < length(xs) {
                    elt <- nth(xs index)
                    if predicate(elt) then {
                      accum <- append(accum elt)
                    }
                    index <- index + 1
                  }
                  accum
                }

                """;
    }

    private String mapReduceFunctions() {
        return """
                # MAP
                map_(f:λ xs:list) : list {
                  foreach(xs f [])
                }

                # REDUCE
                reduce_(xs:list f:λ accum:ℤ) : ℤ {
                  index <- 0
                  while index < length(xs) {
                    accum <- f(accum nth(xs index))
                    index <- index + 1
                  }
                  accum
                }

                # REDUCE
                reduce_(xs:list f:λ accum:ℝ) : ℝ {
                  index <- 0
                  while index < length(xs) {
                    accum <- f(accum nth(xs index))
                    index <- index + 1
                  }
                  accum
                }

                # REDUCE
                reduce_(xs:list f:λ accum:𝔹) : 𝔹 {
                  index <- 0
                  while index < length(xs) {
                    accum <- f(accum nth(xs index))
                    index <- index + 1
                  }
                  accum
                }

                reduce_(xs:list f:λ accum:ℤ) : ℤ {
                  foreach(xs f accum)
                }

                reduce_(xs:list f:λ accum:ℝ) : ℝ {
                  foreach(xs f accum)
                }

                reduce_(xs:list f:λ accum:𝔹) : 𝔹 {
                  foreach(xs f accum)
                }

                """;
    }

    private String reverseFunctions() {
        return """
                <<Recursively reverses a list.>>
                reverse_rec(xs:list):list {
                  if length(xs) = 0 then
                      []
                  else
                      append(reverse_rec(tail(xs)) head(xs))
                }

                <<Iteratively reverses a list.>>
                reverse_iter(xs:list):list {
                  accum <- []
                  index <- length(xs)-1
                  while index >= 0 {
                    accum <- append(accum nth(xs index))
                    index <- index - 1
                  }
                  accum
                }

                """;
    }

    private String timeFunctions() {
        return """
                <<Invokes a function and returns the time (millis) to complete.>>
                timeit(f:λ):ℤ {
                  t0 is milliseconds()
                  f()
                  t1 is milliseconds()
                  t1-t0
                }

                <<Maps a function to elements of a list, returning time (millis) and results.>>
                timeit(f:λ xs:list):list {
                  t0 is milliseconds()
                  ys is map(f xs)
                  t1 is milliseconds()
                  [t1-t0 ys]
                }

                """;
    }

    private String zipFunctions() {
        return """
                <<Given 2 lists return a list of pairs, up to the length of the shortest list.>>
                # iteration via while
                zip(xs1:list xs2:list) : list {
                  accum <- []
                  last_index is min(length(xs1) length(xs2))-1
                  index <- 0
                  while index <= last_index {
                      accum <- append(accum [nth(xs1 index) nth(xs2 index)])
                      index <- index + 1
                  }
                  accum
                }

                <<Given 2 lists return a list of pairs, up to the length of the shortest list.>>
                # iteration via foreach
                zip2(xs1:list xs2:list) : list {
                  last_index is min(length(xs1) length(xs2))-1

                  foreach(seq(0 last_index 1)
                          λ(index:ℤ):list{[nth(xs1 index) nth(xs2 index)]}
                          [])
                }

                """;
    }
}
