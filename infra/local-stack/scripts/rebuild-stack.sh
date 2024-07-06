#! /bin/bash

# This script rebuilds a local stack - rebuild backend with up-to-date code and its dependencies as docker containers

compileJar() {
    echo "Building the backend JAR..."

    cd backend
    ./gradlew clean build -x test

    cd ..
}

buildDockerImage() {
    echo "Building the backend container..."

    docker build -t janbabak/noql-backend:0.0.1 -f ./infra/dockerImages/backend.Dockerfile .
}

removeOldContainer() {
    echo "Removing the old backend container..."

    docker rm -f noql-backend-dev
}

compileJar
buildDockerImage
removeOldContainer
