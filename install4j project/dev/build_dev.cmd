@echo off
echo [%date% %time%] Starting build dev... >> autobuild.log
"C:\Program Files\install4j6\bin\install4jc.exe" --build-ids=29 "C:\Users\mbalanov\IdeaProjects\IFML2j\install4j project\dev\IFML2-dev.install4j" >> autobuild.log
echo [%date% %time%] Finished build dev >> autobuild.log
echo. >> autobuild.log
pause