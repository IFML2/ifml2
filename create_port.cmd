rem rmdir portable
rem mkdir portable
rem mkdir "portable\Saved Games"
xcopy /y out\artifacts\ifml2\*.* "portable\*.*"
xcopy /e /y libs "portable\libs\"
xcopy /e /y Games "portable\Games\"
xcopy /e /y Tests "portable\Tests\"
xcopy /y docs\notes.txt "portable\"
pause