REM Run VStar with the same VM configuration as via JNLP.

REM You need to set VSTAR_HOME to the VStar root directory,
REM e.g. set VSTAR_HOME=C:\vstar

java -Xms25m -Xmx500m -jar %VSTAR_HOME%\dist\vstar.jar
