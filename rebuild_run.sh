#!/bin/sh

echo "git pull"

git pull

echo 'kill running process...'

jps | awk '{print $1}' | xargs kill

mvn clean

echo 'compile and packaging jar...'
mvn package -Dmaven.test.skip=true

echo 'start application...'
nohup java -Xms128m -Xmn128m -jar -Dfile.encoding=UTF-8 -Dserver.port=8080 target/zjee-me-1.0.jar &

sleep 1

tail -f nohup.out
