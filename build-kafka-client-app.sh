#!/bin/bash
set -e

./kafka-client-app/gradlew -p kafka-client-app build -x test
docker build -t kafka-client-app:latest ./kafka-client-app