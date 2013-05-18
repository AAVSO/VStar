#!/bin/sh
#
# Webstart deployment script. This is intended for localhost based test purposes.

rm -rf vstar_test
unzip vstar*.zip
mv vstar vstar_test

# Copy the local JNLP file to the webstart area, replacing the 
# official release file, for test purposes.
cp vstar-local.jnlp vstar_test/vstar.jnlp

# Sign the jars.
find vstar_test/extlib -name "*.jar" -exec jarsigner -keystore myKeystore-local -storepass foobar {} myself \; 
jarsigner -keystore myKeystore-local -storepass foobar vstar_test/dist/vstar.jar myself
