#!/bin/sh
#
# Run VStar with the same VM configuration as via JNLP.

APP_DIR=$(dirname "$0")

java -splash:"$APP_DIR/extlib/vstaricon.png" \
     -Xms800m -Xmx1500m -jar "$APP_DIR/dist/vstar.jar" $*
     
