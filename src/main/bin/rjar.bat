@echo off
IF NOT DEFINED RJAR_HOME GOTO NO_VAR_DEF
java -cp "%RJAR_HOME%\lib\groovy-all-2.4.1.jar;%RJAR_HOME%\lib\rjar-0.6.1.jar" com.rvkb.util.jar.JarUtil %*
GOTO END
:NO_VAR_DEF
ECHO the RJAR_HOME environment variable must be set
:END
