#!/usr/bin/env bash

git pull
./gradlew clean shadowJar
docker build -t arynxd/monkebot-kt:latest .
docker login
docker push arynxd/monkebot-kt:latest
echo "Done!"

