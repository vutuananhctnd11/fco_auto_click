@echo off
chcp 65001 >nul
echo ========================================================
echo        FCOAutoClick - BUILD WINDOWS EXE
echo               by TunEngDev2003
echo ========================================================
echo.

if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-21.0.12" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
    )
)

if not defined JAVA_HOME (
    echo [ERROR] Khong tim thay JAVA_HOME! Vui long cai dat JDK 21 tro len.
    pause
    exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [*] Su dung Java tai: %JAVA_HOME%
java -version
echo.

echo [*] 1/4 Dang build project voi Maven...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERROR] Build Maven that bai!
    pause
    exit /b 1
)

echo.
echo [*] 2/4 Dang sao chep dependencies...
call mvn dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/package-input
if errorlevel 1 (
    echo [ERROR] Sao chep dependencies that bai!
    pause
    exit /b 1
)

copy /Y "target\autoclicker-1.0.0-SNAPSHOT.jar" "target\package-input\" >nul

echo.
echo [*] 3/4 Dang tao FCOAutoClick.exe bang jpackage...
if exist "target\dist\FCOAutoClick" (
    rmdir /S /Q "target\dist\FCOAutoClick"
)

"%JAVA_HOME%\bin\jpackage.exe" ^
    --type app-image ^
    --name "FCOAutoClick" ^
    --input "target/package-input" ^
    --main-jar "autoclicker-1.0.0-SNAPSHOT.jar" ^
    --main-class "com.example.autoclicker.Launcher" ^
    --icon "src/main/resources/icons/FCOAutoClick.ico" ^
    --dest "target/dist" ^
    --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (
    echo [ERROR] jpackage that bai!
    pause
    exit /b 1
)

echo.
echo ========================================================
echo [THANH CONG] File FCOAutoClick.exe da duoc tao tai:
echo    target\dist\FCOAutoClick\FCOAutoClick.exe
echo ========================================================
echo.
pause
