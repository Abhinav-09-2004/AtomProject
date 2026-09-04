@echo off
cd /d "%~dp0"

echo Compiling the project...

if exist bin rmdir /s /q bin
mkdir bin

dir /s /b src\*.java > sources.txt

javac -d bin @sources.txt

if errorlevel 1 (
    echo.
    echo Compilation failed!
    del sources.txt
    pause
    exit /b
)

del sources.txt

echo.
echo Compilation successful!
echo.
echo Starting Movie Booking System...
echo.

java -cp bin app.MovieBookingApp

pause