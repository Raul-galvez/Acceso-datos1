@echo off
setlocal
cd /d "%~dp0"

if not exist out mkdir out
javac -encoding UTF-8 -d out src\*.java
if errorlevel 1 goto :fallo

java -cp out Main
goto :fin

:fallo
echo.
echo La compilacion ha fallado.
:fin
echo.
pause
