@echo off
pushd %~dp0..
del meterman-src.7z
"c:\Program Files\7-Zip\7z.exe" a meterman-src.7z @scripts\srclist.txt -bb ^
    -xr!.*.marks -xr!.svn -xr!*.png -xr!*.ttf -xr!*.ogg -xr!*.wav
popd
