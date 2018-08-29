"""
This script generates an Ant build.xml file suitable for creating a plugin
of a particular type.
"""

import os
import sys
from optparse import OptionParser


TYPES =  ["CustomFilter", "GeneralTool", 
          "ModelCreator", "ObservationSource", 
          "ObservationTool","PeriodAnalysis"]


def main():
    parser = OptionParser()

    parser.add_option("-s", "--show-plugin-types",
                      action="store_true", dest="show_types", default=False,
                      help="Show plug-in types and exit")

    parser.add_option("-v", "--vstar-home-dir",
                      action="store", dest="vstar_home", default=None,
                      help="VStar home directory", metavar="VSTAR_HOME")

    parser.add_option("-t", "--plugin-type",
                      action="store", dest="plugin_type", default=None,
                      help="Plug-in type", metavar="PLUGIN_TYPE")

    parser.add_option("-p", "--plugin-package",
                      action="store", dest="plugin_pkg", default=None,
                      help="Plug-in package", metavar="PLUGIN_PACKAGE")

    parser.add_option("-c", "--plugin-class",
                      action="store", dest="plugin_class", default=None,
                      help="Plug-in class", metavar="PLUGIN_CLASS")

    options, args = parser.parse_args()

    if options.show_types:
        for t in TYPES:
            print(t)
        return

    if options.plugin_type not in TYPES:
        usage(parser, "Plug-in type must be one of: ".format(TYPES))

    with open("build.xml", "w") as build:
        with open("build-template.xml") as template:
            lines = template.readlines()
            for line in lines:
                if '"vstar_home"' in line:
                    line = line.format(options.vstar_home)
                if '"plugin_type"' in line:
                    line = line.format(options.plugin_type)
                if '"plugin_pkg"' in line:
                    line = line.format(options.plugin_pkg)
                if '"plugin_class"' in line:
                    line = line.format(options.plugin_class)
                if '"plugin_src_dir"' in line:
                    path = options.plugin_pkg.replace(".", os.sep)
                    line = line.format(path)

                build.write(line)


def usage(parser, msg=None):
    if msg is not None:
        print(msg)
    parser.print_help()
    print('Example: python gen-ant.py --vstar-home-dir="/Users/david/vstar" '
          '--plugin-type="ObservationSource" '
          '--plugin-package="my.plugin" '
          '--plugin-class="MyFancyNewObservationSource"')
    sys.exit(1)


if __name__ == '__main__':
    main()
