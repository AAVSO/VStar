#!/bin/sh
#
# Run VStar with the same VM configuration as via JNLP.
#
# You need to set VSTAR_HOME to the VStar root directory,
# e.g. under Linux/Mac OS X: export VSTAR_HOME=/home/david/vstar

java -Xms25m -Xmx500m -jar $VSTAR_HOME/dist/vstar.jar
