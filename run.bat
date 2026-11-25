@echo off
REM Compile and run DEAD SIGNAL (requires Java 11+)
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
if errorlevel 1 (pause & exit /b 1)
del sources.txt
java -cp out deadsignal.Main
