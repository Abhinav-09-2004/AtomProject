@echo off
cd /d "%~dp0"

echo Compiling the project...

if exist bin rmdir /s /q bin
mkdir bin

javac -d bin src\*.java

if errorlevel 1 (
    echo.
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)

echo.
echo Compilation successful!
echo.
echo Starting Movie Booking System...
echo.

java -cp bin MovieBookingApp

pause