Ant file for creating and building VStar plug-ins
=================================================

First, install the Ant build tool from here:

  https://ant.apache.org/manual/index.html

ensuring the environment variables are set for your operating system as per 
the "Installing Ant" section of that page.

Create a new plugin by copying this directory to a location of your 
choice.

Then, near the top of the build.xml file you will see this property block:

	<!-- ** User properties START ** -->
	<property name="vstar_home" location="/Users/david/vstar" />
	<property name="plugin_type" value="ObservationSource" />
	<property name="plugin_src_dir" location="my/vstar/plugin" />
	<property name="plugin_pkg" value="my.vstar.plugin" />
	<property name="plugin_class" value="SimpleExampleObSource" />
	<!-- ** User properties END ** -->

The settings above are for an observation source plugin with the class name
SimpleExampleObSource, in the package my.vstar.plugin, in a directory called
my/vstar/plugin with VStar located in /Users/david/vstar.

Modify these to suit your environment and desired plugin type.

Note that currently, the plugin source package path and plugin package 
properties must be independently specified.

Legal values for the plugin_type property are:

- CustomFilter
- GeneralTool
- ModelCreator
- ObservationSource
- ObservationTool
- PeriodAnalysis

Given the foregoing, to create skeleton code for an observation source 
plugin, type this in a shell (Unix shell, Mac Terminal, Windows command 
prompt, ...):

  ant skeleton

Instead of a skeleton, you can create a fully functioning example plugin
by entering:

  ant example

Once you have optionally edited the code to implement your plugin's 
functionality (or just want to use the example code) to compile the 
source and build the jar file, type this:

  ant jar

Skeleton code will build but won't do anything.

To install the plugin's jar file into the vstar_plugins directory, type:

  ant install

which will build the source first if necessary, or just:

  ant

since "install" is the default target.

To see all options, type:

  ant -p

For non-trivial plugin code, I would recommend using an IDE such as Eclipse.
Use of such a tool is beyond the scope of this document.
