## VStar

### Introduction
VStar is a visualisation and analysis tool for variable star observation data
developed in collaboration with the American Association of Variable Star 
Observers (http://www.aavso.org/) and the Citizen Sky project.

VStar can load observation data from files or the AAVSO International 
Database, display interactive light curves and phase plots, and perform period 
analysis.

(Plug-ins)[https://www.aavso.org/vstar-plugin-library] allow VStar's functionality to be extended.

### Installation
In order to run VStar, you need to have version 1.8 or higher of the Java 
runtime installed on your computer. For more, see:

* (Oracle)[https://www.oracle.com/java/technologies/javase-jre8-downloads.html] or 
* (Amazon Corretto)[https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html]

The dist directory contains the latest vstar.jar built from source so you
don't have to build it (see below).

The file extlib/ReadMe.txt details additional Java libraries that are
required (and should already be present as part of this distribution) to run
VStar.

The data directory contains sample variable star data files mostly taken
from the AAVSO International Database.

### Running VStar
The easiest way to run the latest version of VStar is by clicking on the
Java WebStart (TM) download button on the AAVSO web page Data tab (Data -> 
Data Analysis) or the VStar GitHub web site or the Citizen Sky VStar 
team page: 

* http://www.aavso.org/vstar-overview
* https://github.com/AAVSO/VStar

The WebStart vstar.jnlp file can be saved (e.g. to the desktop) and used as an
alternative to accessing VStar via the web site.

VStar can also be launched by one of the following methods:
* Mac OS X: double-clicking the "VStar" launcher application.
* Windows: double-clicking "VStar.exe" launcher application.
* Linux (or other Unix variants or bash users): running the "VStar.sh" shell script.

Each of the foregoing options runs VStar with the same memory allocation 
parameters as for WebStart, permitting, for example, larger datasets 
to be loaded. For running VStar locally, this is the best method.
  
### Opening a data file in VStar
If you downloaded a VStar distribution, you will find a data folder containing sample datasets.

If you started VStar via WebStart instead, the latest example data files are available here:

   https://github.com/AAVSO/VStar/tree/master/data

To open a data file in VStar:

1. Select "New Star from File..." from the File menu.
2. Navigate to the data directory via the file dialog.
3. Choose a file, e.g. "eps_aur_2445000.csv".
   and plot views.

For more complete documentation, see the (VStar User Manual)[https://github.com/AAVSO/VStar/blob/master/doc/user_manual/VStarUserManual.pdf].

### Building from source
This section is of interest to developers rather than VStar users. 

The file build.xml is an Ant file used for building a VStar distribution jar 
file from source. This is not necessary if you just want to run VStar, only 
if you want to Ant can be downloaded from http://ant.apache.org/ and 1.7.0 
or higher is assumed. Type "ant -p" to see the options.

The src directory contains the Java source code for the tool.

The most recent version of the source code may be obtained from the VStar 
GitHub repository at https://github.com/AAVSO/VStar

### Testing
The test directory contains unit tests. For testing, JUnit 3.8.2 is 
assumed (http://www.junit.org). This is of interest to developers only.
See also extlib/ReadMe.txt for more about testing libraries.

### Licensing
VStar is licensed under the GNU Affero General Public License, version 3.
See doc/license or http://www.gnu.org/licenses/agpl-3.0-standalone.html for
details.

For licensing and links to source code for the external libraries used by 
VStar see the included ./extlib/ReadMe.txt file.
