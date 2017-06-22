@echo off

set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=n
mvn hpi:run -Djetty.port=8090