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

    infra/dockerImages/backend/backend.build.backend -x push
    infra/dockerImages/frontend/frontend.build.frontend -x push
}

removeOldContainer() {
    echo "Removing the old backend container..."

    docker rm -f noql-backend-dev noql-frontend-dev
}

compileJar
buildDockerImage
removeOldContainer
