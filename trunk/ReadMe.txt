VStar is a statistical analysis tool for variable star observation data
developed for the American Association of Variable Star Observers 
(http://www.aavso.org/) and the CitizenSky project (http://www.citizensky.org/).

The dist directory contains the latest vstar.jar built from source so you
don't have to build it with Ant (see below).

The file extlib/ReadMe.txt details the required additional libraries.

Once the required libraries are in the extlib directory, VStar can be run 
using one of the following methods:

o Double-click the file: dist/vstar.jar
o Unix shell: vstar.sh (Linux, Mac OS X, other Unixen, cygwin, mingw)
o DOS shell: vstar.bat
o ant run

Try opening a sample data file:

1. Select "New Star from File..." from the File menu.
2. Navigate to the vstar/data menu via the file dialog.
3. Choose the file "eps_aur_2445000.csv".
4. Click each Mode radio button in turn to see the different data and plot views.

When VStar is mature enough, the plan is to make it available via Java 
WebStart on the AAVSO website.

The file build.xml is an Ant file used for building a VStar distribution jar 
file from source. This is not necessary if you just want to run VStar, only if
you want to Ant can be downloaded from http://ant.apache.org/ and 1.7.0 
or higher is assumed. Type "ant -p" to see the options.

The src directory contains the Java source code for the tool.

The test directory contains unit tests. For testing, JUnit 3.8.2 is 
assumed (http://www.junit.org). 

The data directory contains sample variable star data files.

VStar is licensed under the Affero GNU Public License. See doc/license for
details.
