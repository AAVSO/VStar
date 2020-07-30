#!/bin/sh
#
# Run VStar

APP_DIR=$(dirname "$0")

# 32 or 64 bit?
VER=`uname -a | grep _64`

if [ "$VER" != "" ]; then
    # 64 bit, so determine half of available memory for Mac OS X or Linux
    if [ `echo $OSTYPE | grep darwin` != "" ]; then
        # Mac OS X...
        HALF_MEM=$(perl -e "print int(`sysctl -n hw.memsize` / (1024*1024*1024) / 2);")
    else
        # ...otherwise, assume Linux/Unix
        HALF_MEM=$(perl -e "print int(`free -h | awk '/^Mem:/{print $2}' | sed 's/G//'` / 2);")
    fi
    
    MAX_MEM=${HALF_MEM}g
else
    MAX_MEM=1500mb
fi

#echo $MAX_MEM

java -splash:"$APP_DIR/extlib/vstaricon.png" \
     -Xms800m -Xmx${MAX_MEM} -jar "$APP_DIR/dist/vstar.jar" $*