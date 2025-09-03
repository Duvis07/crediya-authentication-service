@echo off
REM CrediYa Authentication Service - Build Script for Podman (Windows)
REM This script builds the JAR file and Docker image for the authentication service

setlocal enabledelayedexpansion

echo Building CrediYa Authentication Service...

REM Configuration
set SERVICE_NAME=authentication-service
set IMAGE_NAME=crediya/authentication-service
set IMAGE_TAG=latest
set JAR_NAME=authentication-service.jar

echo Step 1: Building JAR with Gradle...
call gradlew.bat clean build -x test
if !errorlevel! neq 0 (
    echo JAR build failed
    exit /b 1
)
echo JAR build successful

echo Step 2: Copying JAR to deployment directory...
REM Find the built JAR file
for /f "delims=" %%i in ('dir /b /s applications\app-service\build\libs\*.jar 2^>nul') do set JAR_FILE=%%i
if not defined JAR_FILE (
    echo JAR file not found in applications\app-service\build\libs
    exit /b 1
)

REM Copy JAR to deployment directory
copy "!JAR_FILE!" "deployment\!JAR_NAME!"
echo JAR copied to deployment\!JAR_NAME!

echo Step 3: Building Docker image with Podman...
podman build -t "!IMAGE_NAME!:!IMAGE_TAG!" -f deployment\Dockerfile deployment\
if !errorlevel! neq 0 (
    echo Docker image build failed
    exit /b 1
)
echo Docker image built successfully

echo Step 4: Image information...
podman images | findstr "!IMAGE_NAME!"

echo Build completed successfully!
echo Next steps:
echo    • Run: podman-compose up -d to start services
echo    • Check: podman-compose logs -f to view logs
echo    • Test: curl http://localhost:8080/actuator/health

endlocal
