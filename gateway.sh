#!/bin/bash


docker stop gateway
./gradlew build -x test 
docker-compose up -d --build 
