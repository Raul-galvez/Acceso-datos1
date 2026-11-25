@echo off
setlocal
cd /d "%~dp0"

set "M2=%USERPROFILE%\.m2\repository"
call :jar "org\xerial\sqlite-jdbc\3.46.1.0" sqlite-jdbc-3.46.1.0.jar
call :jar "org\slf4j\slf4j-api\1.7.36" slf4j-api-1.7.36.jar
call :jar "org\slf4j\slf4j-nop\1.7.36" slf4j-nop-1.7.36.jar
set "JARS=%M2%\org\xerial\sqlite-jdbc\3.46.1.0\sqlite-jdbc-3.46.1.0.jar;%M2%\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;%M2%\org\slf4j\slf4j-nop\1.7.36\slf4j-nop-1.7.36.jar"

if not exist out mkdir out
javac -encoding UTF-8 -cp "%JARS%" -d out src\*.java
if errorlevel 1 goto :fallo

java -cp "out;%JARS%" Main
goto :fin

:fallo
echo.
echo La compilacion ha fallado.
:fin
echo.
pause
exit /b

rem Descarga la libreria si no esta en este ordenador
:jar
if exist "%M2%\%~1\%~2" exit /b
set "RUTA=%~1"
curl -sSfL --create-dirs -o "%M2%\%~1\%~2" "https://repo1.maven.org/maven2/%RUTA:\=/%/%~2"
exit /b
