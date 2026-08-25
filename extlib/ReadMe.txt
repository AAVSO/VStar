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
  
  o nashorn-core-15.7.jar
    Standalone OpenJDK Nashorn JavaScript engine. Required for
    Tool -> Run Script... on Java 15+, where Nashorn is no longer
    bundled with the JDK.
    GPL-2 with Classpath exception (same family as OpenJDK). VStar
    remains AGPL-3; the Classpath exception is what permits linking.
    Do not copy Nashorn sources into VStar files. Corresponding source:
    https://github.com/openjdk/nashorn

  o asm-7.3.1.jar
  o asm-commons-7.3.1.jar
  o asm-tree-7.3.1.jar
  o asm-util-7.3.1.jar
  o asm-analysis-7.3.1.jar
    ASM bytecode library, a dependency of Nashorn.
    BSD 3-Clause. Copyright (c) 2000-2011 INRIA, France Telecom.
    https://asm.ow2.io/

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
       
  o GNU General Public License, version 2 with Classpath exception
    gpl-2.0.txt (GPL-2 text)
    gpl-2.0-classpath-exception.txt (Classpath exception addendum)
    Full Nashorn LICENSE (GPL-2 + exception):
    https://github.com/openjdk/nashorn/blob/main/LICENSE
    GPL-2 without the Classpath exception is not compatible with
    AGPL-3; the exception is required for shipping Nashorn with VStar.

    * OpenJDK Nashorn  https://github.com/openjdk/nashorn

  o BSD 3-Clause License
    asm-bsd-3-clause.txt
    https://asm.ow2.io/license.html

    * ASM  https://asm.ow2.io/
       
If you are a developer who wishes to contribute toward coding or 
testing VStar, the following libraries are required for unit testing
and CI purposes. None of these libraries are redistributed as part
of a VStar release.

  o JUnit 4 (test runner, required by pitest mutation testing)
    - junit-4.12.jar
    - hamcrest-core-1.3.jar
    - Eclipse Public License 1.0 (not distributed with VStar releases)
    - https://junit.org/junit4/

  o AssertJ Swing (functional GUI testing, see issue #579 prong B)
    - assertj-swing-3.17.1.jar
    - assertj-swing-junit-3.17.1.jar
    - assertj-core-3.17.2.jar
    - Apache License 2.0
    - https://assertj.github.io/doc/#assertj-swing
    On JDK 9+ the test target passes a small set of --add-opens flags
    (see build.xml) so AssertJ Swing can reflect into AWT/Swing
    internals. On Linux CI the ant invocation is wrapped in xvfb-run
    so GUI tests have a virtual display.

  o Property based testing framework
    - quicktheories-0.26.jar
    - https://github.com/quicktheories/QuickTheories

For line and branch coverage reporting, JaCoCo is used.

  o JaCoCo code coverage library
    - https://www.jacoco.org/jacoco/
    - jacocoant.jar (0.8.14)
    - Eclipse Public License 2.0

For static analysis with SpotBugs, the full distribution is bundled
under tools/spotbugs/ and used by the spotbugs Ant target. Run with:
  ant spotbugs

  o SpotBugs static analysis (4.9.3)
    - https://spotbugs.github.io
    - tools/spotbugs/lib/
    - GNU Lesser General Public License, version 2.1

For nullness checking with the Checker Framework, the annotation
types are in extlib and the checker processor is under tools:
  ant checker

  o Checker Framework (3.54.1)
    - https://checkerframework.org
    - extlib/checker-qual-3.54.1.jar (annotation types, MIT license)
    - tools/checker-framework/checker-3.54.1.jar (processor, GPL-2.0 with Classpath Exception)
    Currently runs with -Awarns (warnings only, does not fail the build).

For mutation testing, additional libraries are required.

  o pitest mutation testing framework
    - https://pitest.org
    - pitest-1.15.3.jar
    - pitest-ant-1.15.3.jar
    - pitest-entry-1.15.3.jar
    - JUnit 4 required by pitest (see above)
    - commons-text-1.12.0.jar (Apache 2.0) - runtime dependency
    - commons-lang3-3.17.0.jar (Apache 2.0) - runtime dependency of commons-text
