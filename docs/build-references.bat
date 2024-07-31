@echo off

REM 1. Build Scenamatica.jar
cd ..\Scenamatica
call ./build -D"jar.finalName=Scenamatica" -P release

if %ERRORLEVEL% NEQ 0 (
    echo Build failed.
    exit /b 1
)

REM 2. Build ledger
cd ..\Bookkeeper
call run -o dist -cp "%paper:1.16.5" -i ..\Scenamatica\ScenamaticaPlugin\target\Scenamatica.jar

if %ERRORLEVEL% NEQ 0 (
    echo Build failed.
    exit /b 1
)

REM 3. Build templating-tools
set CWD=%cd%
cd ..\Bookkeeper\templating-tools
call pnpm start -t %CWD%\bookkeeper-templates -e .mdx -o %CWD%\references %CWD%\dist\ledger.zip

if %ERRORLEVEL% NEQ 0 (
    echo Build failed.
    cd %CWD%
    exit /b 1
)
cd %CWD%

# 4. Copy schemas to static directory
mkdir static/schemas

## 4.1 Copy schemas
xcopy dist\* static\schemas /s

## 4.2 Remove LICENSE and README.md files
del static/schemas/LICENSE
del static/schemas/README.md
