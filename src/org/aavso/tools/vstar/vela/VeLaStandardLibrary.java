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
 * 
 */
public class VeLaStandardLibrary {

    private static final VeLaStandardLibrary stdLib = new VeLaStandardLibrary();

    public static VeLaStandardLibrary getInstance() {
        return stdLib;
    }

    public String getStdLibStr() {
        StringBuffer buf = new StringBuffer();
        buf.append("# FOR\n");
        buf.append("for_(f:λ xs:list) {\n");
        buf.append("  index <- 0\n");
        buf.append("  while index < length(xs) {\n");
        buf.append("    f(nth(xs index))\n");
        buf.append("    index <- index + 1\n");
        buf.append("  }\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("# FOREACH\n");
        buf.append("# This is equivalent to a list comprehension.\n");
        buf.append("# Don't need accum; just internally use accum <- []\n");
        buf.append("foreach(xs:list f:λ accum:list) : list {\n");
        buf.append("  index <- 0\n");
        buf.append("  while index < length(xs) {\n");
        buf.append("    accum <- append(accum f(nth(xs index)))\n");
        buf.append("    index <- index + 1\n");
        buf.append("  }\n");
        buf.append("  accum\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("# FILTER\n");
        buf.append("filter_(xs:list predicate:λ) : list {\n");
        buf.append("  accum <- []\n");
        buf.append("  index <- 0\n");
        buf.append("  while index < length(xs) {\n");
        buf.append("    elt <- nth(xs index)\n");
        buf.append("    if predicate(elt) then {\n");
        buf.append("      accum <- append(accum elt)\n");
        buf.append("    }\n");
        buf.append("    index <- index + 1\n");
        buf.append("  }\n");
        buf.append("  accum\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("# REDUCE\n");
        buf.append("reduce_(xs:list f:λ accum:ℤ) : ℤ {\n");
        buf.append("  index <- 0\n");
        buf.append("  while index < length(xs) {\n");
        buf.append("    accum <- f(accum nth(xs index))\n");
        buf.append("    index <- index + 1\n");
        buf.append("  }\n");
        buf.append("  accum\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("# REDUCE\n");
        buf.append("reduce_(xs:list f:λ accum:ℝ) : ℝ {\n");
        buf.append("  index <- 0\n");
        buf.append("  while index < length(xs) {\n");
        buf.append("    accum <- f(accum nth(xs index))\n");
        buf.append("    index <- index + 1\n");
        buf.append("  }\n");
        buf.append("  accum\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("# REDUCE\n");
        buf.append("reduce_(xs:list f:λ accum:𝔹) : 𝔹 {\n");
        buf.append("  index <- 0\n");
        buf.append("  while index < length(xs) {\n");
        buf.append("    accum <- f(accum nth(xs index))\n");
        buf.append("    index <- index + 1\n");
        buf.append("  }\n");
        buf.append("  accum\n");
        buf.append("}\n");
        buf.append("map_(f:λ xs:list):list {\n");
        buf.append("  foreach(xs f [])\n");
        buf.append("}\n");
        buf.append("reduce_(xs:list f:λ accum:ℤ) : ℤ {\n");
        buf.append("  foreach(xs f accum)\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("reduce_(xs:list f:λ accum:ℝ) : ℝ {\n");
        buf.append("  foreach(xs f accum)\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("reduce_(xs:list f:λ accum:𝔹) : 𝔹 {\n");
        buf.append("  foreach(xs f accum)\n");
        buf.append("}\n");
        buf.append("<<Recursively reverses a list.>>\n");
        buf.append("\n");
        buf.append("reverse_rec(xs:list):list {\n");
        buf.append("  if length(xs) = 0 then \n");
        buf.append("      []\n");
        buf.append("  else\n");
        buf.append("      append(reverse_rec(tail(xs)) head(xs))\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("<<Iteratively reverses a list.>>\n");
        buf.append("\n");
        buf.append("reverse_iter(xs:list):list {\n");
        buf.append("  accum <- []\n");
        buf.append("  index <- length(xs)-1\n");
        buf.append("  while index >= 0 {\n");
        buf.append("    accum <- append(accum nth(xs index))\n");
        buf.append("    index <- index - 1\n");
        buf.append("  }\n");
        buf.append("  accum\n");
        buf.append("}\n");
        buf.append("<<Invokes a function and returns the time (millis) to complete.>> \n");
        buf.append("\n");
        buf.append("timeit(f:λ):ℤ {\n");
        buf.append("  t0 is milliseconds()\n");
        buf.append("  f()\n");
        buf.append("  t1 is milliseconds()\n");
        buf.append("  t1-t0\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("<<Maps a function to elements of a list, returning time (millis) and results.>>\n");
        buf.append("\n");
        buf.append("timeit(f:λ xs: list):list {\n");
        buf.append("  t0 is milliseconds()\n");
        buf.append("  ys is map(f xs)\n");
        buf.append("  t1 is milliseconds()\n");
        buf.append("  [t1-t0 ys]\n");
        buf.append("}\n");
        buf.append("<<Given 2 lists return a list of pairs, up to the length of the shortest list.>>\n");
        buf.append("\n");
        buf.append("# iteration via while\n");
        buf.append("zip(xs1:list xs2:list) : list {\n");
        buf.append("  accum <- []\n");
        buf.append("  last_index is min(length(xs1) length(xs2))-1\n");
        buf.append("  index <- 0\n");
        buf.append("  while index <= last_index {\n");
        buf.append("      accum <- append(accum [nth(xs1 index) nth(xs2 index)])\n");
        buf.append("      index <- index + 1\n");
        buf.append("  }\n");
        buf.append("  accum\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("# iteration via foreach\n");
        buf.append("zip2(xs1:list xs2:list) : list {\n");
        buf.append("  last_index is min(length(xs1) length(xs2))-1\n");
        buf.append("\n");
        buf.append("  foreach(seq(0 last_index 1)\n");
        buf.append("          λ(index:ℤ):list{[nth(xs1 index) nth(xs2 index)]}\n");
        buf.append("          [])\n");
        buf.append("}\n");

        return buf.toString();
    }
}
