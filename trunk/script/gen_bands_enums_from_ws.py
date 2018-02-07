#!/usr/bin/python
#
# Converts web service elements into SeriesType enum constructor invocations.
#
# See https://www.aavso.org/vsx/index.php?view=api.bands for XML.
#
# The enum values go to standard output.

import sys
from lxml import etree
import urllib.request

def gen_bands(tree, locale_bands):
    for band in tree.findall('Band'):
        code = band.find('Code').text
        desc = band.find('Description').text
        short_name = band.find('ShortName').text
        r = band.find('RColor').text
        g = band.find('GColor').text
        b = band.find('BColor').text

        if False:
            print (code, desc, short_name, r, g, b)
        else:
            print("public static final SeriesType {0} = new SeriesType" \
                    "({1}, {2}, {3}, new Color({4}, {5}, {6}));\n". \
                    format(make_band_enum_name(desc), 
                           code,
                           _desc(desc, locale_bands), 
                           _short_name(short_name, locale_bands),
                           r, g, b))

def make_band_enum_name(band):
    band = band.replace(' ', '_')
    band = band.replace('-', '_')
    band = band.replace('(', '')
    band = band.replace(')', '')
    band = band.replace('.', 'pt')
    return band

def _desc(desc, locale_bands):
    if desc in locale_bands:
        desc = 'LocaleProps.get("{0}")'.format(locale_bands[desc])
    elif desc.find('Orange') != -1:
        desc = 'LocaleProps.get("{0}") + " (Liller)"'.\
            format(locale_bands['Orange'])
    else:
        desc = '"{0}"'.format(desc)

    return desc

def _short_name(short_name, locale_bands):
    name = short_name[0:short_name.find('-Vis.')]

    if name in locale_bands:
        name = 'LocaleProps.get("{0}") + "-Vis."'.format(locale_bands[name])
    elif short_name == 'Orange':
        name = 'LocaleProps.get("{0}")'.format(locale_bands['Orange'])
    else:
        name = '"{0}"'.format(short_name)

    return name

if __name__ == '__main__':
    locale_bands = {}
    locale_bands['Visual'] = 'VISUAL_SERIES'
    locale_bands['Unknown'] = 'UNKNOWN_SERIES'
    locale_bands['Red'] = 'RED_SERIES'
    locale_bands['Green'] = 'GREEN_SERIES'
    locale_bands['Blue'] = 'BLUE_SERIES'
    locale_bands['Yellow'] = 'YELLOW_SERIES'
    locale_bands['Orange'] = 'ORANGE_SERIES'
    
    url = 'https://www.aavso.org/vsx/index.php?view=api.bands'
    tree = etree.parse(urllib.request.urlopen(url))
    gen_bands(tree, locale_bands)
