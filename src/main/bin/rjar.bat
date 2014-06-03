@echo off
IF NOT DEFINED RJAR_HOME GOTO NO_VAR_DEF
java -cp "%RJAR_HOME%\lib\groovy-all-1.6.7.jar;%RJAR_HOME%\lib\rjar-0.5.jar" com.rvkb.util.jar.JarUtil %*
GOTO END
:NO_VAR_DEF
ECHO the RJAR_HOME environment variable must be set
:END
