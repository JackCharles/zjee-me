#!/bin/sh

echo "git pull"

git pull

echo 'kill running process...'

jps | awk '{print $1}' | xargs kill

mvn clean

echo 'compile and packaging jar...'
mvn package -Dmaven.test.skip=true

echo 'copy native lib...'
/bin/cp -rf native-lib/libsigar-amd64-linux.so /usr/lib64

echo 'start application...'
nohup java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8418,suspend=n -jar -Dfile.encoding=UTF-8 -Dserver.port=80 target/zjee-me-1.0.jar &

sleep 1

tail -f nohup.out
