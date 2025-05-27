@echo off
echo Compiling PropertyRegistry.java and PropertyViewer.java...

:: Create lib directory if it doesn't exist
if not exist lib mkdir lib

:: Download JSON library if needed
if not exist lib\json.jar (
    echo Downloading JSON library...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar' -OutFile 'lib\json.jar'"
)

:: Compile the Java files
javac -cp "lib\json.jar;." PropertyRegistry.java PropertyViewer.java

echo Compilation complete! 