Introduction
============
VStar is a statistical analysis tool for variable star observation data
developed for the American Association of Variable Star Observers 
(http://www.aavso.org/) and the CitizenSky project (http://www.citizensky.org/).

Installation
============
In order to run VStar, you need to have version 1.5 or higher of the Java 
runtime installed on your computer. See http://java.com/en/download/manual.jsp

The dist directory contains the latest vstar.jar built from source so you
don't have to build it (see below).

The file extlib/ReadMe.txt details additional free Java libraries that are
required to run VStar.

The data directory contains sample variable star data files.

Running VStar
=============
Once the required libraries are in the extlib directory, VStar can be run 
using one of the following methods:

o Double-click the file: dist/vstar.jar
o From a Linux, Mac OS X, or other Unix shell type: java -jar dist/vstar.jar    
o From a DOS prompt type: java -jar dist\vstar.jar 
o ant run (for developers and testers)

Try opening a sample data file:

1. Select "New Star from File..." from the File menu.
2. Navigate to the vstar/data menu via the file dialog.
3. Choose the file "eps_aur_2445000.csv".
4. Click each Mode radio button in turn to see the different data and plot views.

When VStar is mature enough, the plan is to make it available via Java 
WebStart on the AAVSO website so that the latest version will always just 
be a mouse-click away.

Building from source
====================
This section is of interest to developers rather than VStar users. 

The file build.xml is an Ant file used for building a VStar distribution jar file
from source. This is not necessary if you just want to run VStar, only if you 
want to Ant can be downloaded from http://ant.apache.org/ and 1.7.0 or higher 
is assumed. Type "ant -p" to see the options.

The src directory contains the Java source code for the tool.

Testing
=======
The test directory contains unit tests. For testing, JUnit 3.8.2 is 
assumed (http://www.junit.org). This is of interest to developers only.

Licensing
=========
VStar is licensed under the Affero GNU Public License. See doc/license for
details.
