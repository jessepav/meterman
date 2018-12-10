#!/bin/sh

java -Xmx256M -cp "meterman.jar:lib/*:jars/*" -splash:assets/meterman/splash-screen.png \
    com.illcode.meterman.Meterman
