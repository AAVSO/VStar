#!/bin/sh
#
# Run VStar

APP_DIR=$(dirname "$0")

# 32 or 64 bit?
VER=`uname -a | grep _64`

if [ "$VER" != "" ]; then
    # 64 bit, so determine half of available memory
    HALF_MEM=$(perl -e "print `sysctl -n hw.memsize` / (1024*1024*1024) / 2;")
    MAX_MEM=${HALF_MEM}g
else
    MAX_MEM=1500mb
fi

java -splash:"$APP_DIR/extlib/vstaricon.png" \
     -Xms800m -Xmx${MAX_MEM} -jar "$APP_DIR/dist/vstar.jar" $*