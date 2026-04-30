@echo off
setlocal enabledelayedexpansion

if exist projectBL\src\projectBL\WebServer.java (
    pushd projectBL
    echo Compiling project source files in projectBL\src\projectBL...
    javac -d bin -sourcepath src src\projectBL\*.java src\projectBL\service\*.java src\projectBL\dao\*.java src\projectBL\model\*.java
    if errorlevel 1 (
        echo Compilation failed.
        popd
        exit /b 1
    )
    echo Starting web server on http://localhost:9090
    java -cp bin projectBL.WebServer
    popd
) else (
    echo ERROR: projectBL\src\projectBL\WebServer.java not found.
    echo Please run this script from the outer workspace root: %CD%
)
