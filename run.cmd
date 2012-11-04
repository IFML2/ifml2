xcopy /y out\artifacts\ifml2\*.* "test run\*.*"
xcopy /e /y Games "test run\Games\"
xcopy /e /y libs "test run\libs\"
xcopy /e /y Tests "test run\Tests\"
cd "test run"
java -Dlog4j.configuration=file:log4j.xml -jar ifml2.jar "player"


rem java -Dlog4j.configuration=file:log4j.xml -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -jar ifml2.jar "player"

rem java -Dlog4j.configuration=file:log4j.xml -cp "C:\Program Files\Java\jdk1.7.0\jre\lib\charsets.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\deploy.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\javaws.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\jce.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\jsse.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\management-agent.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\plugin.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\resources.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\rt.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\ext\dnsns.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\ext\localedata.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\ext\sunec.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\ext\sunjce_provider.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\ext\sunmscapi.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\ext\sunpkcs11.jar;C:\Program Files\Java\jdk1.7.0\jre\lib\ext\zipfs.jar;C:\Users\realsonic\Documents\ЯРИЛ 2.0\ifml2 project\out\production\ifml2;C:\Users\realsonic\Documents\ЯРИЛ 2.0\ifml2 project\lib\apache-log4j-1.2.17\log4j-1.2.17.jar;C:\Users\realsonic\Documents\ЯРИЛ 2.0\ifml2 project\lib\glazedlists_java15-1.9-20110828.184933-10.jar;C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 11.1.1\lib\idea_rt.jar" -jar ifml2.jar "player"

rem pause