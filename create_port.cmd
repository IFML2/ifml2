@echo off
rmdir portable
mkdir portable
mkdir "portable\Saved Games"
xcopy /y out\artifacts\ifml2\*.* "potable\*.*"
xcopy /e /y libs "potable\libs\"
xcopy /e /y Games "potable\Games\"
xcopy /e /y Tests "potable\Tests\"