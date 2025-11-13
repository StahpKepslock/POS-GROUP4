@echo off
set "SCRIPT_DIR=%~dp0"
set "MAVEN_EXEC=%SCRIPT_DIR%maven\apache-maven-3.9.6\bin\mvn.cmd"

echo Building project...
call "%MAVEN_EXEC%" clean package
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo Build successful! Starting application...
echo.

set "M2_REPO=%USERPROFILE%\.m2\repository"
set "JAVAFX_VERSION=17.0.1"

set "MODULE_PATH=%M2_REPO%\org\openjfx\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%-win.jar;%M2_REPO%\org\openjfx\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%-win.jar;%M2_REPO%\org\openjfx\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%-win.jar;%M2_REPO%\org\openjfx\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%-win.jar"

java --module-path "%MODULE_PATH%" --add-modules=javafx.controls,javafx.fxml -jar "%SCRIPT_DIR%target\pos-desktop-1.0.0.jar"

if errorlevel 1 (
    echo Application crashed!
    pause
)