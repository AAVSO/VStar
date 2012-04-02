#!/bin/sh
#
# Run VStar with the same VM configuration as via JNLP.

APP_DIR=$(dirname "$0")

java -Xms25m -Xmx500m -jar "$APP_DIR/dist/vstar.jar" $*
