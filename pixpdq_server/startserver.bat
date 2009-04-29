@echo off

set JAVA_HOME=C:/Java/jre1.6.0_05

if NOT EXIST %JAVA_HOME% ( echo Warning: The JAVA_HOME variable is not set or not valid. )

"%JAVA_HOME%/bin/java" -jar OpenPIXPDQAdapter.jar 
pause