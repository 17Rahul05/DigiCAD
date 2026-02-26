@echo off
setlocal enabledelayedexpansion

:: Clean/Create bin directory
if exist bin (
    rmdir /s /q bin
)
mkdir bin

:: Get all java files
set "javaFiles="
for /r sim %%f in (*.java) do (
    set "javaFiles=!javaFiles! "%%f""
)

:: Compile
echo Compiling...
javac -d bin %javaFiles%

if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b %errorlevel%
)

:: Find JAR command
set "JAR_CMD=jar"
where jar >nul 2>nul
if %errorlevel% neq 0 (
    echo 'jar' command not found in PATH. Checking JAVA_HOME...
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jar.exe" (
            set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
            echo Found jar in JAVA_HOME: !JAR_CMD!
        )
    )
)

:: Fallback to specific JDK path if still not found
where !JAR_CMD! >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Java\jdk-25.0.2\bin\jar.exe" (
        set "JAR_CMD=C:\Program Files\Java\jdk-25.0.2\bin\jar.exe"
        echo Found jar in standard path: !JAR_CMD!
    ) else (
        echo Error: 'jar' command not found. Please ensure JDK is installed and in PATH.
        exit /b 1
    )
)

:: Package JAR
echo Packaging JAR...
"!JAR_CMD!" cvfm DigiCAD.jar manifest.txt -C bin .

if %errorlevel% neq 0 (
    echo JAR creation failed.
    exit /b %errorlevel%
)

echo Success! JAR created: DigiCAD.jar
