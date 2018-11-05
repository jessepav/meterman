@ECHO OFF
SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

cd %~dp0..

SET output_dir=build\javadoc

IF "%1" NEQ "" (
    SET visibility=%1
    SET output_dir="!output_dir!-!visibility!"
    IF "!visibility!" EQU "private" SET linksrc=-linksource
) ELSE (
    SET visibility=protected
)

rmdir /S /Q %output_dir%
mkdir %output_dir%

javadoc -classpath lib\*;build\production\Meterman -%visibility% %linksrc% ^
   -sourcepath src -subpackages com.illcode.meterman -d "%output_dir%" ^
   -windowtitle "Meterman Javadocs" -doctitle "Meterman Javadocs" ^
   -linkoffline https://jessepav.github.io/java-api-docs/json-simple-3.0.2/ scripts\package-info\json-simple ^
   -linkoffline https://docs.oracle.com/javase/7/docs/api/ scripts\package-info\java7 ^
   -linkoffline https://jessepav.github.io/java-api-docs/BeanShell/javadoc/ scripts\package-info\bsh

:END
ENDLOCAL
ECHO ON
@EXIT /B 0
