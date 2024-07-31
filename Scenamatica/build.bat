@echo off

mvn install --file ../Bookkeeper/pom.xml
mvn package --file pom.xml "$@"
