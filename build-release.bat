@echo off
REM Build script for Kindlebot release

REM Get version from pom.xml (project version, not dependency versions)
for /f "usebackq delims=" %%a in (`powershell -NoProfile -Command "$([xml](Get-Content 'pom.xml')).project.version"`) do set "VERSION=%%a"
echo Building version - %VERSION%

REM Clean and package
call mvn clean package

REM Copy JAR to release
copy target\kindlebot-%VERSION%.jar release\kindlebot.jar

REM Create versioned ZIP
cd release
powershell "Compress-Archive -Path * -DestinationPath ..\kindlebot-%VERSION%.zip"
cd ..

echo Release package created: kindlebot-%VERSION%.zip