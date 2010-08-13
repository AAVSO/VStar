VStar is reliant upon a number of freely available open source libraries that
are distributed as Java archive files ("jar" files) which are described below.

The following libraries must (and should already be) be present in the 
current directory (./extlib) for VStar to function. The list of jar files
is given below, along with the web address from which to obtain the source 
code.

  o commons-math-2.1.jar
    http://commons.apache.org/math/
    
  o jcommon-1.0.16.jar
    http://sourceforge.net/projects/jfreechart/files/

  o jfreechart-1.0.13.jar
    http://sourceforge.net/projects/jfreechart/files/

  o swing-worker-1.2.jar
    http://swingworker.dev.java.net/servlets/ProjectDocumentList

  o jlfgr-1_0.jar
    http://java.sun.com/developer/techDocs/hi/repository/

  o mysql-connector-java-5.1.10-bin.jar
    http://dev.mysql.com/downloads/connector/j/5.1.html

The libraries are covered under the following licenses, and plain text copies
of the licenses are included in the ./extlib directory. See the URL for the
web page of each project for more information.

  o Apache License
    ./extlib/apache-LICENSE.txt
    http://commons.apache.org/license.html
    See also http://www.apache.org/licenses/GPL-compatibility.html
        
    * Commons Math  http://commons.apache.org/math/
        
  o GNU Lesser General Public License, version 2.1
    ./extlib/lgpl-2.1.txt
    http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

    * JCommon  http://sourceforge.net/projects/jfreechart/
    * JFreeChart  http://sourceforge.net/projects/jfreechart/
    * SwingWorker  http://swingworker.dev.java.net/

  o GNU General Public License, version 2
    ./extlib/gpl-2.0.txt
    http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

    * MySQL Connector/J  http://dev.mysql.com/downloads/connector/j/

  o Sun Software License Agreement
    ./extlib/sun_license.txt
    (see download page which is linked from the url below)

    * Java Look and Feel Graphics Repository (jlfgr)
      http://java.sun.com/developer/techDocs/hi/repository/

This section categorises the libraries in terms of function:

 - Math (statistics):
     commons-math-2.1.jar
      
 - JFreeChart related:
     jcommon-1.0.16.jar
     jfreechart-1.0.13.jar
     
 - Java Look and Feel Graphics Repository:
     jlfgr-1_0.jar
     
 - MySQL JDBC:
     mysql-connector-java-5.1.10-bin.jar
     
If you are a developer who wishes to contribute toward coding or 
testing VStar, the following libraries must be downloaded for unit 
testing purposes:

 - JUnit 3.8.2:
     junit.jar (http://www.junit.org/)
     
 - Cobertura 1.9.3:
     o http://cobertura.sourceforge.net/
     o Unpack the cobertura-1.9.3 archive into the extlib directory.
     o This is used in build-full.xml for code coverage.
