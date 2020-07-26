#!/bin/sh
#
# Run VStar with the same VM configuration as via JNLP.

APP_DIR=$(dirname "$0")

# 32 or 64 bit?
VER=`uname -a | grep _64`

if [ "$VER" != "" ]; then
    MAX_MEM=32g
else
    MAX_MEM=1500mb
fi

java -splash:"$APP_DIR/extlib/vstaricon.png" \
     -Xms800m -Xmx${MAX_MEM} -jar "$APP_DIR/dist/vstar.jar" $*