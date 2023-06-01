#!/bin/bash
#
# Create Mac OSX application.

cp -rf install-files/mac/VStar.app .
cp VStar.sh VStar.app/Contents/MacOS
cp -r dist VStar.app/Contents/MacOS
cp -r extlib VStar.app/Contents/MacOS
