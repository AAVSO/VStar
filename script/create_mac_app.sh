#!/bin/bash
#
# Create Mac OSX application.

rm -rf VStar.app

cp -r install-files/mac/VStar.app .

cp VStar.sh VStar.app/Contents/MacOS

cp -r dist VStar.app/Contents/MacOS

mkdir VStar.app/Contents/MacOS/extlib
cp extlib/commons-math-2.2.jar VStar.app/Contents/MacOS/extlib
cp extlib/javacsv.jar VStar.app/Contents/MacOS/extlib
cp extlib/jfreechart-1.5.6.jar VStar.app/Contents/MacOS/extlib
cp extlib/jmathplot.jar VStar.app/Contents/MacOS/extlib
cp extlib/antlr-runtime-4.5.3.jar VStar.app/Contents/MacOS/extlib
cp extlib/nico-camargo-toolbar-icons-4.0.jar VStar.app/Contents/MacOS/extlib
cp extlib/nashorn-core-15.7.jar VStar.app/Contents/MacOS/extlib
cp extlib/asm-7.3.1.jar VStar.app/Contents/MacOS/extlib
cp extlib/asm-commons-7.3.1.jar VStar.app/Contents/MacOS/extlib
cp extlib/asm-tree-7.3.1.jar VStar.app/Contents/MacOS/extlib
cp extlib/asm-util-7.3.1.jar VStar.app/Contents/MacOS/extlib
cp extlib/asm-analysis-7.3.1.jar VStar.app/Contents/MacOS/extlib
cp extlib/vstaricon.png VStar.app/Contents/MacOS/extlib
