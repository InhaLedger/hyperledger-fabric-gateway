#!/bin/bash


docker stop gateway

git pull origin master
./gradlew build -x test 
docker-compose up -d --build 
