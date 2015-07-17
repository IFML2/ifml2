@echo off
rem java -version:"1.6*" -Dfile.encoding=866 -Dlog4j.configuration=file:log4j.xml -classpath .;ifml2.jar;glazedlists_java15-1.9.0.jar;log4j-1.2.17.jar ifml2.players.ConsolePlayer %1
start "IFML2" /b java -classpath .;ifml2.jar;glazedlists_java15-1.9.0.jar;log4j-1.2.17.jar -Dfile.encoding=866 -Dlog4j.configuration=file:log4j.xml ifml2.players.ConsolePlayer %1