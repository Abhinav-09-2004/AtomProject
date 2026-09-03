@echo off
echo Compiling the project...
if not exist bin mkdir bin
javac -d bin src\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Compilation successful.
echo Running MovieBookingApp...
echo.
java -cp bin MovieBookingApp
pause
