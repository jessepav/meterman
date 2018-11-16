@echo off
pushd %~dp0..
java -Xmx256M -cp build\production\Meterman;lib\* -splash:assets/meterman/splash-screen.png ^
    com.illcode.meterman.Meterman
popd
