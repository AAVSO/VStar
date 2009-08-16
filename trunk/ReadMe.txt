This is VStar: a statistical analysis tool for variable star data
developed for the American Association of Variable Star Observers 
(http://www.aavso.org/) and the CitizenSky project (http://www.citizensky.org/).

The src directory contains the Java source code for the tool.

The test directory contains unit tests. For testing, JUnit 3.8.2 or higher 
is assumed (http://www.junit.org). 

The data directory contains sample data files.

The dist directory contains the latest vstar.jar built from source so you
don't have to build it.

See extlib/ReadMe.txt regearding required libraries.

The file build.xml is an Ant file used for building a VStar distribution jar 
file. Ant can be downloaded from http://ant.apache.org/ and 1.7.0 or higher 
is assumed. Type "ant -p" to see the options.

VStar can be run using ant ("ant run") or with vstar.sh (Linux, Mac OS X, 
other Unixen) or vstar.bat (Windows).

Ultimately the plan is to make VStar available via Java WebStart on the AAVSO 
website.
