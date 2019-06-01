#!/bin/sh

cd demo	

echo 'pulling latest code from github...'
git pull

mvn clean

echo 'compile and packaging jar...'
mvn package -Dmaven.test.skip=true

cd target

mv -f demo-1.0.jar ../../

cd ../../

nohup java -jar demo-1.0.jar -Dspring.config.location=application.properties &

tail -f nohup.out
