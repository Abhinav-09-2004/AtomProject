@echo off
cd /d "%~dp0"

echo Compiling the project...

if exist bin rmdir /s /q bin
mkdir bin

javac -sourcepath src -d bin src\seat\*.java src\booking\*.java src\app\*.java src\test\*.java

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

java -cp bin app.MovieBookingApp

pause