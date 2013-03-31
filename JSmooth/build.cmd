@echo off

title Starting build...

rem echo. >> build.log
rem echo [Log started at %date% %time%] >> build.log

title Copying required files...
copy ..\out\artifacts\ifml2\*.jar distrib\
rem >> build.log
copy ..\log4j.xml distrib\
rem  >> build.log
xcopy ..\Games distrib\Games\ /E /Y
rem  >> build.log
xcopy ..\libs distrib\libs\ /E /Y
rem  >> build.log
xcopy ..\Tests distrib\Tests\ /E /Y

title Building...
cd distrib
"%ProgramFiles%\JSmooth 0.9.9-7\jsmoothcmd.exe" IFML2.jsmooth
rem  >> ..\build.log

rem echo %errorlevel%



rem echo [Log ended at %date% %time%] >> build.log

pause