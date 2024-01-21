#!/bin/bash
#
# Run VeLa interpreter

APP_DIR=${HOME}/vstar

echo $APP_DIR

java -Xms25m -Xmx500m -cp "$APP_DIR/dist/vstar.jar" org.aavso.tools.vstar.vela.VeLaScriptDriver $*
