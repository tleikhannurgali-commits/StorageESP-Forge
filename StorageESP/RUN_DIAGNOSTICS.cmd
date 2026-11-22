@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title StorageESP - Full Gradle Error Log
color 0B

echo =====================================================
echo          STORAGE ESP - BUILD DIAGNOSTICS
echo =====================================================
echo.

if not exist "build.gradle" (
  echo ERROR: Put RUN_DIAGNOSTICS.cmd in the StorageESP folder
  echo next to BUILD_MOD.cmd and build.gradle, then run it there.
  goto missing
)

set "GRADLE=%~dp0.build-tools\gradle-9.5.0\bin\gradle.bat"
if not exist "%GRADLE%" (
  echo ERROR: Gradle was not found in .build-tools.
  echo Run BUILD_MOD.cmd once in this folder to download it first.
  goto missing
)

echo Running Gradle. Full output will be saved in:
echo   %~dp0gradle-full.log
echo.
call "%GRADLE%" build --no-daemon --stacktrace --console=plain > "%~dp0gradle-full.log" 2>&1
set "CODE=%ERRORLEVEL%"
echo.
if "%CODE%"=="0" (
  echo BUILD SUCCEEDED!
  echo Check the build\libs folder for your JAR.
) else (
  echo BUILD FAILED with exit code %CODE%.
  echo.
  echo First useful error lines from the saved log:
  powershell.exe -NoProfile -Command "Select-String -LiteralPath '.\gradle-full.log' -SimpleMatch -Pattern 'What went wrong','Caused by:','Could not','error:','FAILURE:' | Select-Object -First 20 | ForEach-Object { $_.Line }"
  echo.
  echo Send the complete gradle-full.log file to ChatGPT.
  echo It is located next to this RUN_DIAGNOSTICS.cmd file.
)
echo.
echo Press any key to close this window.
pause >nul
exit /b %CODE%

:missing
echo.
echo Press any key to close this window.
pause >nul
exit /b 1
