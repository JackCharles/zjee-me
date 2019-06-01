#!/bin/sh

echo 'pulling latest code from github...'
git pull

mvn clean

echo 'compile and packaging jar...'
mvn package -Dmaven.test.skip=true

nohup java -jar target/demo-1.0.jar -Dspring.config.location=application.properties &

tail -f nohup.out
