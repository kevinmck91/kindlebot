@echo off

if not exist config\application.properties (
    echo.
    echo Missing config\application.properties
    echo.
    echo Please copy:
    echo application.properties_template
    echo to:
    echo application.properties
    echo and fill in your Firebase project details.
    echo.
    pause
    exit /b
)

if not exist config\serviceAccountKey.json (
    echo.
    echo Missing config\serviceAccountKey.json
    echo.
    echo Please copy:
    echo serviceAccountKey_template.json
    echo to:
    echo serviceAccountKey.json
    echo and fill in your Firebase service account key.
    echo.
    pause
    exit /b
)

java -jar kindlebot.jar

pause