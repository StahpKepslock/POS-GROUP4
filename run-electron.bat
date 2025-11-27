@echo off
echo --- SETUP ---
set "NODE_DIR=%~dp0nodejs"
set "PATH=%NODE_DIR%;%PATH%"
set "APP_DIR=%~dp0migrated\backend\pos_app"
echo.

echo --- VERIFYING ENVIRONMENT ---
echo Node version:
node -v
if errorlevel 1 ( echo ERROR. & pause & exit /b )
echo.
echo NPM version:
call npm -v
if errorlevel 1 ( echo ERROR. & pause & exit /b )
echo.

echo --- CHANGING DIRECTORY ---
pushd "%APP_DIR%"
if errorlevel 1 (
    echo ERROR: Could not change to directory: "%APP_DIR%"
    pause
    exit /b
)
echo Now in directory: %cd%
echo.

echo --- INSTALLING DEPENDENCIES ---
echo This may take a few minutes...
call npm install
if errorlevel 1 (
    echo ERROR: npm install failed. See messages above.
    pause
    exit /b
)
echo.

echo --- REBUILDING FOR ELECTRON ---
echo This ensures database compatibility.
call npm run rebuild
if errorlevel 1 (
    echo ERROR: Rebuild failed. See messages above.
    pause
    exit /b
)
echo.

echo --- STARTING APP ---
call npm run electron
if errorlevel 1 (
    echo ERROR: Failed to start app.
    pause
    exit /b
)

popd
echo.
echo Script finished successfully.
pause