@echo off
pushd %~dp0..
java -Xmx256M -cp build\production\meterman;build\production\riverboat;lib\*;jars\* ^
    -splash:assets/meterman/splash-screen.png ^
    com.illcode.meterman.Meterman
popd
