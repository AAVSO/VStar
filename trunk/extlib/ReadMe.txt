VStar is reliant upon a number of freely available open source libraries that
are distributed as Java archive files ("jar" files) and are described below.

The following libraries must (and should already be) be present in the 
current directory (./extlib) for VStar to function. The list of jar files
is given below, along with the web address from which to obtain the source 
code.

  o commons-math-2.2.jar
    http://commons.apache.org/math/
    
  o jcommon-1.0.16.jar
    http://sourceforge.net/projects/jfreechart/files/

  o jfreechart-1.0.13.jar
    http://sourceforge.net/projects/jfreechart/files/

  o mysql-connector-java-5.1.10-bin.jar
    http://dev.mysql.com/downloads/connector/j/5.1.html

  o jmathplot.jar
    http://code.google.com/p/jmathplot/
  
  o javacsv
    https://sourceforge.net/projects/javacsv/
    
  o c3p0
    https://sourceforge.net/projects/c3p0/

  o mchange-commons-java
    c3p0 is dependent upon this.
    https://sourceforge.net/projects/c3p0/
     
  o nico-camargo-toolbar-icons-4.0.jar
    Toolbar icons created by Citizen Sky participant, Nico Camargo.
  
  o vstaricon.png
    This image was also created by Nico Camargo and is used for the
    splash screen and desktop icon.
    
The libraries are covered under the following licenses and plain text 
copies of the licenses are included in the ./extlib directory. See the 
URL for the web page of each project for more information.

  o Apache License
    apache-LICENSE.txt
    http://commons.apache.org/license.html
    See also http://www.apache.org/licenses/GPL-compatibility.html
        
    * Commons Math  http://commons.apache.org/math/
        
  o GNU Lesser General Public License, version 2.1
    lgpl-2.1.txt
    http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

    * JCommon  http://sourceforge.net/projects/jfreechart/
    * JFreeChart  http://sourceforge.net/projects/jfreechart/
    * c3p0  https://sourceforge.net/projects/c3p0/
    * mchange-commons-java  https://sourceforge.net/projects/c3p0/

  o GNU Lesser General Public License, version 2.0
    lgpl-2.0.txt
    http://www.gnu.org/licenses/old-licenses/lgpl-2.0.html

    * javacsv  https://sourceforge.net/projects/javacsv/
    
  o GNU General Public License, version 2
    gpl-2.0.txt
    http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

    * MySQL Connector/J  http://dev.mysql.com/downloads/connector/j/
    
  o New BSD License
    http://www.opensource.org/licenses/bsd-license.php
    
    * jmathplot  http://code.google.com/p/jmathplot/
     
If you are a developer who wishes to contribute toward coding or 
testing VStar, the following libraries must be downloaded for unit 
testing purposes:

 - JUnit 3.8.2:
     junit.jar (http://www.junit.org/)
     
 - Cobertura 1.9.3:
     o http://cobertura.sourceforge.net/
     o Unpack the cobertura-1.9.3 archive into the extlib directory.
     o This is used in build-dev.xml for code coverage.
