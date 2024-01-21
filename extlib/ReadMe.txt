VStar is reliant upon a number of freely available open source libraries that
are distributed as Java archive files ("jar" files) and are described below.

The following libraries must (and should already be) be present in the 
current directory (extlib) for VStar to function. The list of jar files
is given below, along with the web address from which to obtain the source 
code.

  o commons-math-2.2.jar
    http://commons.apache.org/math/

  o jfreechart-1.5.2.jar
    https://github.com/jfree/jfreechart/releases/tag/v1.5.2

  o jmathplot.jar
    http://code.google.com/p/jmathplot/
  
  o javacsv.jar
    https://sourceforge.net/projects/javacsv/
         
  o antlr-runtime-4.5.3.jar
  o antlr-4.5.3-complete.jar
    http://www.antlr.org
    
  o nico-camargo-toolbar-icons-4.0.jar
    Toolbar icons created by Citizen Sky participant, Nico Camargo.
  
  o vstaricon.png
    This image was also created by Nico Camargo and is used for the
    splash screen and desktop icon.

The libraries are covered under the following licenses and plain text 
copies of the licenses are included in the doc/license directory.
See the URL for the web page of each project for more information.

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

  o GNU Lesser General Public License, version 2.0
    lgpl-2.0.txt
    http://www.gnu.org/licenses/old-licenses/lgpl-2.0.html

    * javacsv  https://sourceforge.net/projects/javacsv/
        
  o FreeBSD License (2 clause)
    http://www.opensource.org/licenses/bsd-license.php
    
    * jmathplot  http://code.google.com/p/jmathplot/
  
  o ANTLR4 Licence (3 clause Modified BSD licence)
    http://www.antlr.org/license.html
    
    * ANTLR4 http://www.antlr.org
       
If you are a developer who wishes to contribute toward coding or 
testing VStar, apart from JUnit 3 (junit.jar), the following libraries
must be downloaded (e.g. from https://mvnrepository.com) for unit 
testing purposes. Note that none of these libraries are redistributed
as part of a VStar release.

  o Property based testing framework
    - quicktheories-0.26.jar
    - https://github.com/quicktheories/QuickTheories

For mutation testing, additional libraries are required.

  o pitest mutation testing framework
    - https://pitest.org
    - pitest-1.15.3.jar
    - pitest-ant-1.15.3.jar
    - pitest-entry-1.15.3.jar
    - testng-7.9.0.jar
    - JUnit 4 (or 5) required by pitest
      o under EPL-1.0 licence: can't be linked or distributed with VStar
        and not currently included here
        - junit-4.12.jar
        - hamcrest-core-1.3.jar
