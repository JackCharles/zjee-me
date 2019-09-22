#!/bin/sh

echo 'kill running process...'

jps | awk '{print $1}' | xargs kill

echo 'pulling latest code from github...'
git pull

mvn clean

echo 'compile and packaging jar...'
mvn package -Dmaven.test.skip=true

echo 'start application...'
nohup java -jar target/zjee-ml-1.0.jar -Dserver.port=443 &

sleep 1

tail -f nohup.out
