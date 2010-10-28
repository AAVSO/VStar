Introduction
============
VStar is a statistical analysis tool for variable star observation data
developed for the American Association of Variable Star Observers 
(http://www.aavso.org/) and the Citizen Sky project 
(http://www.citizensky.org/).

VStar can load observational data from files or the AAVSO International 
Database, display interactive light curves and phase plots, and perform period 
analysis.
 
Installation
============
In order to run VStar, you need to have version 1.6 or higher of the Java 
runtime installed on your computer. See http://java.com/en/download/manual.jsp

The dist directory contains the latest vstar.jar built from source so you
don't have to build it (see below).

The file extlib/ReadMe.txt details additional free Java libraries that are
required (and should already be present as part of this distribution) to run
VStar.

The data directory contains sample variable star data files.

Running VStar
=============
VStar can be launched by:

o Double-clicking the vstar.jar file icon (dist/vstar.jar)
o Typing: java -jar dist/vstar.jar (Unix) or java -jar dist\vstar.jar (DOS)
o Typing: ./script/run_vstar.sh (Unix) or script\run_vstar.bat (DOS)
  See those script/batch files for more details. The main benefit of the
  scripts is that they run VStar with the same memory allocation parameters
  as for WebStart (see below), permitting bigger/more datasets to be loaded.
  They require the VSTAR_HOME environment to be set.
o Typing: ant run
  This option is intended for developers. Type ant -p for more details.

The easiest way to run VStar is by clicking on the Java WebStart (TM) link
on the Citizen Sky or VStar SourceForge web sites:

o Go to http://www.citizensky.org/teams/vstar-software-development
o Click on the "Download VStar Now" button

Directions for using the WebStart version of VStar are on the same web page. 
Launching VStar via WebStart means that you will always have the most up-to-
date version of VStar. You can also double-click the vstar.jnlp file at the
top-level of this distribution.

If you obtained the VStar distribution via SourceForge, try opening a sample
data file in VStar:

1. Select "New Star from File..." from the File menu.
2. Navigate to the data directory via the file dialog.
3. Choose the file "eps_aur_2445000.csv".
4. Click each Mode radio button in turn to see the different data 
   and plot views.

If you started VStar via WebStart rather than obtaining the distribution
via SourceForge, this sample data file and others are available here:

  http://vstar.svn.sourceforge.net/viewvc/vstar/trunk/data/
  
Click on one of the files in your web browser, then click the download link
associated with one of the file's versions (if there is more than one), saving
as a text file.
 
Signing up for a free account at http://www.citizensky.org/ will give you
access to the AAVSO International Database via VStar. This requirement may
eventually be removed.

Building from source
====================
This section is of interest to developers rather than VStar users. 

The file build.xml is an Ant file used for building a VStar distribution jar 
file from source. This is not necessary if you just want to run VStar, only 
if you want to Ant can be downloaded from http://ant.apache.org/ and 1.7.0 
or higher is assumed. Type "ant -p" to see the options.

The src directory contains the Java source code for the tool.

The most recent version of the source code may be obtained from the VStar 
SourceForge repository at http://vstar.svn.sourceforge.net

Testing
=======
The test directory contains unit tests. For testing, JUnit 3.8.2 is 
assumed (http://www.junit.org). This is of interest to developers only.
See also extlib/ReadMe.txt for more about testing libraries.

Licensing
=========
VStar is licensed under the GNU Affero General Public License, version 3.
See doc/license or http://www.gnu.org/licenses/agpl-3.0-standalone.html for
details.

For licensing and links to source code for the external libraries used by 
VStar see the included ./extlib/ReadMe.txt file.
