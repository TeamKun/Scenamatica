@echo off

REM 1. Check if Bookkeeper.jar exists
IF NOT EXIST "cli\target\Bookkeeper.jar" (
    echo Building Bookkeeper.jar...
    mvn package
    IF %ERRORLEVEL% NEQ 0 (
        echo Build failed.
        exit /b 1
    )
)

REM 2. Run the jar file
java -jar cli\target\Bookkeeper.jar %*
