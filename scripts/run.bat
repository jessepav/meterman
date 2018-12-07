@echo off
pushd %~dp0..
java -Xmx256M -cp build\production\Meterman;lib\*;jars\* -splash:assets/meterman/splash-screen.png ^
    com.illcode.meterman.Meterman
popd
