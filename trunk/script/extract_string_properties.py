#!/usr/bin/python
#
# This script extracts string properties from a Java source file,
# e.g. public final static String FOO_BAR = "foo bar" and outputs 
# a name value pair that can be used in a Java properties file.

import re
import sys

oneliner_patt = re.compile("String\s+(\w+)\s*=\s*\"(.+)\"")
str_decl_patt = re.compile("String\s+(\w+)\s*=\s+$")
str_lit_patt = re.compile("\"(.+)\"")

def extract_props(path):
    print "\n%s\n" % path
    
    f = open(path)
    lines = f.readlines()
    f.close()

    i = 0
    while i < len(lines):
        line_nums = []
        line = lines[i]
        # Named string on a single line?
        matcher = oneliner_patt.search(line)
        if matcher is not None:
            strname = matcher.group(1)
            strlit = matcher.group(2)
            line_nums = [i+1]
            print "%s: %s=%s" % (line_nums, strname, strlit)
        else:
            # First part of named string declaration on one line... 
            matcher = str_decl_patt.search(line)
            if matcher is not None:
                strname = matcher.group(1)
                line_nums.append(i+1)
                # ...and the string literal value on the next.
                i += 1
                line = lines[i]
                matcher = str_lit_patt.search(line)
                if matcher is not None:
                    strlit = matcher.group(1)
                    line_nums.append(i+1)
                    print "%s: %s=%s" % (line_nums, strname, strlit)
                else:
                    print "ERROR: '%s' not followed by literal on next line" % strname
        i += 1
            
if len(sys.argv) == 2:
    extract_props(sys.argv[1])
