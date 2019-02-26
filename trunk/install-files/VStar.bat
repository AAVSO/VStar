:: Run VStar with the same VM configuration as via JNLP.

:: VSTAR_HOME needs to be set to the VStar root directory,
:: e.g. set VSTAR_HOME=C:\vstar
:: If not set, the script assumes the current directory is the
:: directory that the script is running from.

if not "%VSTAR_HOME%" == "" GOTO RUN

set VSTAR_HOME=%~dp0

:RUN 
java -splash:"%VSTAR_HOME%\extlib\vstaricon.png" -Xms800m -Xmx1500m -jar "%VSTAR_HOME%\dist\vstar.jar" %*
