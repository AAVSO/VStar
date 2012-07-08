# See http://weblogs.java.net/blog/vivekp/archive/2006/12/webservices_in.html

# Do all of this properly with Ant.

# This script must be run from the src directory.

VSTAR_HOME=$(dirname "$0")
DEPLOY_DIR=~/tmp/ws

# Generates web service endpoint and starts the AAVSO observation 
# source web service, then generates web service client from WSDL.

apt -cp $VSTAR_HOME/dist/vstar.jar -d $DEPLOY_DIR org/aavso/tools/vstar/input/ws/endpoint/AAVSOObsWebService.java 

java -cp $DEPLOY_DIR:$VSTAR_HOME/dist/vstar.jar:/Users/david/vstar/extlib/mysql-connector-java-5.1.10-bin.jar org.aavso.tools.vstar.input.ws.endpoint.AAVSOObsWebService 8080 &

sleep 1

# Import client from WSDL, compile and run test app.
wsimport -p org.aavso.tools.vstar.input.ws.client -keep http://localhost:8080/obs?wsdl

# Question: shouldn't "-cp ." instead be VSTAR_HOME below?

javac -cp . org/aavso/tools/vstar/input/ws/client/AAVSOObsWebServiceClientApp.java

java -cp . org.aavso.tools.vstar.input.ws.client.AAVSOObsWebServiceClientApp
