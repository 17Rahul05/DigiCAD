# Create bin directory if it doesn't exist
if (-not (Test-Path bin)) {
    New-Item -ItemType Directory -Path bin
} else {
    Remove-Item -Path bin\* -Recurse -Force
}

# Find all Java source files
$javaFiles = Get-ChildItem -Path sim -Filter *.java -Recurse | Select-Object -ExpandProperty FullName

# Compile
Write-Host "Compiling..."
javac -d bin $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed."
    exit $LASTEXITCODE
}

# Find JAR command
$jarCmd = "jar"
if (Get-Command "jar" -ErrorAction SilentlyContinue) {
    $jarCmd = "jar"
} elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\jar.exe")) {
    $jarCmd = "$env:JAVA_HOME\bin\jar.exe"
    Write-Host "Found jar in JAVA_HOME: $jarCmd"
} elseif (Test-Path "C:\Program Files\Java\jdk-25.0.2\bin\jar.exe") {
    $jarCmd = "C:\Program Files\Java\jdk-25.0.2\bin\jar.exe"
    Write-Host "Found jar in standard path: $jarCmd"
} else {
    Write-Host "Error: 'jar' command not found. Please ensure JDK is installed and in PATH."
    exit 1
}

# Package JAR
Write-Host "Packaging JAR..."
& $jarCmd cvfm DigiCAD.jar manifest.txt -C bin .

if ($LASTEXITCODE -ne 0) {
    Write-Host "JAR creation failed."
    exit $LASTEXITCODE
}

Write-Host "Success! JAR created: DigiCAD.jar"
