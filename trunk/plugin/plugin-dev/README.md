## Ant-based tool to create VStar plug-ins

### Ant
First, install the [Ant build tool](https://ant.apache.org/manual/index.html), ensuring the environment variables are set for your operating system as per the "Installing Ant" section.

### Get Started
Get ready to create a new plug-in by first copying this directory and everything in it to a location of your choice. 

### gen-ant.py
The `gen-ant.py` Python script is used to create an Ant `build.xml` file for a particular plug-in type. The script has been tested with Python 3.

In a console (shell or Windows command prompt) type:

	python gen-ant.py --help

to give usage information:

	Usage: gen-ant.py [options]
	
	Options:
	  -h, --help            show this help message and exit
	  -s, --show-plugin-types
	                        Show plug-in types and exit
	  -v VSTAR_HOME, --vstar-home-dir=VSTAR_HOME
	                        VStar home directory
	  -t PLUGIN_TYPE, --plugin-type=PLUGIN_TYPE
	                        Plug-in type
	  -p PLUGIN_PACKAGE, --plugin-package=PLUGIN_PACKAGE
	                        Plug-in package
	  -c PLUGIN_CLASS, --plugin-class=PLUGIN_CLASS
	                        Plug-in class

### Available Plug-in Types

	python gen-ant.py --show-plugin-types
	
		CustomFilter
		GeneralTool
		ModelCreator
		ObservationSource
		ObservationTool
		PeriodAnalysis

### Example Usage
Here is an example of creating an observation source plug-in `build.xml` file:

	python gen-ant.py --vstar-home-dir="/Users/david/vstar"
	                  --plugin-type="ObservationSource" 
	                  --plugin-package="my.plugin" 
	                  --plugin-class="SimpleExampleObSource"

The options above are for an observation source plug-in with the class name `SimpleExampleObSource`, in the package `my.plugin` (so a directory called `my/plugin`) with the VStar home directory `/Users/david/vstar`. The latter is the top-level directory corresponding to a VStar distribution from [SourceForge](https://sourceforge.net/projects/vstar/files).

### Creating Skeleton Plug-in Source Code
Given the foregoing, to create skeleton code for an observation source plug-in, type this in a shell (Unix shell, Mac Terminal, Windows command prompt, ...):

	ant skeleton

### Creating Example Plug-in Source Code
Instead of a plug-in skeleton, you can create a fully functioning example plug-in with the command:

	ant example

In both cases (skeleton and example), the code will be in the directory you specified via `gen-ant.py`.

### Build the Plug-in
Once you have optionally edited the code to implement your plug-in's 
functionality (or just want to start with the example code), to compile the source and build the plug-in jar file, type this:

	ant jar

Skeleton code will build but won't do anything. Example code should yield a functioning plug-in without any modification.

You will find class files in the `build` directory and the jar file in the `dist` directory.  

### Install the Plug-in Code
To install the plug-in's jar file into the `vstar_plugins` directory, type:

	ant install

which will build the source first if necessary, or just:

	ant

since `install` is the default target.

### Closing Remarks
To see all options, type:

	ant -p

For non-trivial plug-in code, I would recommend using an IDE such as Eclipse. Use of such a tool is beyond the scope of this document.

Hopefully this will make it easier to get started writing VStar plug-ins. 
