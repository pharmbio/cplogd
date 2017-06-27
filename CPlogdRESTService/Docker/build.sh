#!/bin/bash

# Build the maven project
cd .. 
mvn clean package
cp target/*.war Docker/

cd Docker/

DOCKER_API_VERSION=v1.23

docker build -t cplogd .
docker save -o cplogd.tar cplogd

touch cplogd.tar.gz
rm cplogd.tar.gz
gzip cplogd.tar

