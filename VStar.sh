#!/bin/sh
#
# Run VStar

APP_DIR=$(dirname "$0")

# 32 or 64 bit?
VER=`uname -a | grep _64`

if [ "$VER" != "" ]; then
    # 64 bit, so determine half of available memory for Mac OS X or Linux
    if [ "`echo $OSTYPE | grep darwin`" != "" ]; then
        # Mac OS X...
        HALF_MEM=$(perl -e "print int(`sysctl -n hw.memsize` / (1024*1024*1024) / 2);")
    else
        # ...otherwise, assume Linux...
        HALF_MEM=$(perl -e "print int(`cat /proc/meminfo | head | awk '/MemTotal:/{print $2}'` / 1024/1024/2)")
    fi
 
    if [ "$HALF_MEM" == "" ]; then
        # ...for any other case (e.g. a Unix with no /proc 
        # or non-Unix bash), set to a conservative value of 4GB
        HALF_MEM="4"
    fi
       
    MAX_MEM=${HALF_MEM}g
else
    MAX_MEM=1500mb
fi

echo $MAX_MEM

java -splash:"$APP_DIR/extlib/vstaricon.png" \
     -Xms800m -Xmx${MAX_MEM} -jar "$APP_DIR/dist/vstar.jar" $*