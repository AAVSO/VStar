#!/usr/bin/python
#
# Converts TSV lines to SeriesType enum constructor invocations.
#
# Columns in each line of input are:
# Code Description shortName R_color G_color B_color
#
# The enum values go to standard output.

import sys

def gen_bands(lines):
    for line in lines:
        if line[0:4] == 'Code':
            continue

        if line[-1] == '\n':
            line = line[0:-1]

        (code, desc, short_name, r, g, b) = line.split('\t')

        if False:
            print (code, desc, short_name, r, g, b)
        else:
            print "%s(%s, \"%s\", \"%s\", new Color(%s, %s, %s))," % \
                (make_band_enum_name(desc), code, desc, short_name, r, g, b)

def make_band_enum_name(band):
    band = band.replace(' ', '_')
    band = band.replace('-', '_')
    band = band.replace('(', '')
    band = band.replace(')', '')
    band = band.replace('.', 'pt')
    return band

if len(sys.argv) == 2:
    f = open(sys.argv[1])
    lines = f.readlines()
    f.close()
    gen_bands(lines)
