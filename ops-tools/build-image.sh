#!/bin/bash

ARTIFACT_VERSION="${ARTIFACT_VERSION:-latest}"
REPOSITORY="time-tracker-backend"
DOCKER_FILE="./ops-tools/docker/Dockerfile"

cd ..

# Build time-tracker image
docker build -f ${DOCKER_FILE} -t ${REPOSITORY}:${ARTIFACT_VERSION} .
docker tag ${REPOSITORY}:${ARTIFACT_VERSION} ${REPOSITORY}:latest

# Push time-tracker image
#docker push ${REPOSITORY}:${ARTIFACT_VERSION}
#docker push ${REPOSITORY}:latest

# Cleanup
#docker rmi ${REPOSITORY}:${ARTIFACT_VERSION}
#docker rmi ${REPOSITORY}:latest
