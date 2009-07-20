This is VStar: a statistical analysis tool for variable star data
developed for the American Association of Variable Star Observers 
(http://www.aavso.org/) and the CitizenSky project (http://www.citizensky.org/).

The src directory contains the Java source code for the tool.

The test directory contains unit tests. For testing, JUnit 3.8.2 or higher 
is assumed (http://www.junit.org). 

The data directory contains sample data files.

The file build.xml is an Ant file used for building a VStar distribution jar 
file. Ant can be downloaded from http://ant.apache.org/ and 1.7.0 or higher 
is assumed.

VStar requires the following libraries to be in the extlib directory.

- jcommon-1.0.16.jar (http://www.jfree.org/jcommon/)
- jfreechart-1.0.13.jar (http://www.jfree.org/jfreechart/)
- mysql-connector-java-5.0.5-bin.jar (http://dev.mysql.com/downloads/connector/j/5.0.html)  (TODO: review)
- swing-worker-1.2.jar (https://swingworker.dev.java.net/servlets/ProjectDocumentList)
- JUnit 3.8.2 (http://www.junit.org/)

See extlib/ReadMe.txt for more details about libraries.
