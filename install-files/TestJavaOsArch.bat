@ECHO OFF
SET JAVA=java

%JAVA% JavaOsArch
IF ERRORLEVEL 1 GOTO :JAVA32
ECHO JAVA 64-bit detected!
GOTO :EOF

:JAVA32
ECHO Cannot detect 64-bit Java! JAVA is 32-bit or unknown