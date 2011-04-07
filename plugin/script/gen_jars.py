#!/usr/bin/python
#
# Generate plugin jar files from Java source.
#
# Must be run from vstar/plugin/src

import os

#root_dir = os.path.join(os.environ['VSTAR_HOME'])
root_dir = ''

vstar_dist_jar = os.path.join('..', '..', 'dist', 'vstar.jar')
tamfits_jar = os.path.join('..', '..', 'plugin', 'lib', 'tamfits.jar')

#plugin_src_dir = os.path.join(root_dir, 'plugin', 'src')
plugin_src_dir = root_dir
plugin_pkg = 'org.aavso.tools.vstar.external.plugin'
plugin_path = os.path.join(plugin_src_dir, os.path.sep.join(plugin_pkg.split('.')))
plugin_classes = [
                  'AAVSOExtendedFileFormatObservationSource',
                  'AAVSOUploadFileFormatObservationSource',
                  'CustomFilterTest',
                  'ExampleObSource',
                  'JDToDateTool',
                  'KeplerFITSObservationSource',
                  'ObservationCounter',
                  'PeriodAnalysisPluginTest1',
                  'SuperWASPFITSObservationSource',
                  'MeanTimeBetweenSelectionTool'
                 ]
   
libs = "%s:%s" % (vstar_dist_jar, tamfits_jar)

for plugin_class in plugin_classes:
    # Compile.
    plugin_src_file = "%s%s%s.java" % (plugin_path, os.path.sep, plugin_class)
    javac_command = "javac -cp %s %s" % (libs, plugin_src_file)
    print javac_command
    os.system(javac_command)

    # Create jar file.
    plugin_classes = "%s%s%s*.*" % (plugin_path, os.path.sep, plugin_class)
    plugin_jar_file = "%s.%s.jar" % (plugin_pkg, plugin_class)
    jar_command = "jar -cf %s %s" % (plugin_jar_file, plugin_classes)
    print jar_command
    os.system(jar_command)
