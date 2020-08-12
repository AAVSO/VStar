@echo off

:: VSTAR_HOME needs to be set to the VStar root directory,
:: e.g. set VSTAR_HOME=C:\vstar
:: If not set, the script assumes the current directory is the
:: directory that the script is running from.

title VStar

if not "%VSTAR_HOME%" == "" goto :RUN

set VSTAR_HOME=%~dp0

:RUN

if "%PROCESSOR_ARCHITECTURE%" == "x86" ( 
    if not defined PROCESSOR_ARCHITEW6432 (goto :RUN_X86)
) 

::64-bit Windows
java -splash:"%VSTAR_HOME%\extlib\vstaricon.png" -Xms800m -Xmx4000m -jar "%VSTAR_HOME%\dist\vstar.jar" %*
if ERRORLEVEL 1 goto :ERROR
goto :EOF

:RUN_X86
:: 32-bit Windows
java -splash:"%VSTAR_HOME%\extlib\vstaricon.png" -Xms800m -Xmx1000m -jar "%VSTAR_HOME%\dist\vstar.jar" %*
if ERRORLEVEL 1 goto :ERROR
goto :EOF

:ERROR
echo *** Nonzero exit code: possible ERROR running VStar
pause
