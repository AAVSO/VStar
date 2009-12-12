Introduction
============
VStar is a statistical analysis tool for variable star observation data
developed for the American Association of Variable Star Observers 
(http://www.aavso.org/) and the Citizen Sky project (http://www.citizensky.org/).

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

o Double-click the vstar.jar file icon (dist/vstar.jar)
o Type java -jar dist/vstar.jar (Unix) or java -jar dist\vstar.jar (DOS)
o ant run (for developers and testers)

Try opening a sample data file:

1. Select "New Star from File..." from the File menu.
2. Navigate to the vstar/data menu via the file dialog.
3. Choose the file "eps_aur_2445000.csv".
4. Click each Mode radio button in turn to see the different data and plot views.

Another way to run VStar is by using WebStart from the Citizen Sky website.
Go to http://www.citizensky.org/teams/vstar-software-development and click on
the "Run VStar Now" button. Directions for using the WebStart version of VStar
are on the same webpage.

Building from source
====================
This section is of interest to developers rather than VStar users. 

The file build.xml is an Ant file used for building a VStar distribution jar file
from source. This is not necessary if you just want to run VStar, only if you 
want to Ant can be downloaded from http://ant.apache.org/ and 1.7.0 or higher 
is assumed. Type "ant -p" to see the options.

The src directory contains the Java source code for the tool.

The most recent version of the source code may be obtained from the VStar 
SourceForge repository at http://vstar.svn.sourceforge.net

Testing
=======
The test directory contains unit tests. For testing, JUnit 3.8.2 is 
assumed (http://www.junit.org). This is of interest to developers only.

Licensing
=========
VStar is licensed under the GNU Affero General Public License, version 3.
See doc/license or http://www.gnu.org/licenses/agpl-3.0-standalone.html for
details.

For licensing and links to source code for the external libraries used by VStar
see the included ./extlib/ReadMe.txt file.
