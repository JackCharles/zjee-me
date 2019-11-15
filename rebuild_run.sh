#!/bin/sh

echo 'kill running process...'

jps | awk '{print $1}' | xargs kill

echo 'pulling latest code from github...'
git pull

mvn clean

echo 'compile and packaging jar...'
mvn package -Dmaven.test.skip=true

echo 'copy native lib...'
/bin/cp -rf native-lib/libsigar-amd64-linux.so /usr/lib64

echo 'start application...'
nohup java -jar -Dserver.port=443 target/zjee-ml-1.0.jar &

sleep 1

tail -f nohup.out
