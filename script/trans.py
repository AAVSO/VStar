#!/usr/bin/python
#
# Generate obfuscated names.

import sys

factors = [163, 173, 163, 173, 181, 193, 197, 199, 211, 223, 227, 271, 277, 
           281, 283, 293, 307, 383, 389, 401, 409, 419, 431, 433, 479, 487, 
           491, 499, 503, 509, 521, 557, 563, 569, 571, 587]

def trans(s):
    return map(lambda x,y: x*ord(y), factors[0:len(s)], s)

for arg in sys.argv[1:]:
    print "%s -> %s" % (arg, trans(arg))
