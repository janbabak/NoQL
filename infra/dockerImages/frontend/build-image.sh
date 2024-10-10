#! /bin/bash

VERSION="0.0.3"

buildImage() {
    echo "Building frontend image..."

    docker build \
        -t janbabak/noql-frontend-arm64:$VERSION \
        -f ./infra/dockerImages/frontend/frontend.Dockerfile \
        --platform linux/arm64 .

    docker build \
        -t janbabak/noql-frontend-amd64:$VERSION \
        -f ./infra/dockerImages/frontend/frontend.Dockerfile \
        --platform linux/amd64 .
}

pushImage() {
    echo "Pushing frontend image..."

    docker push janbabak/noql-frontend-arm64:$VERSION
    docker push janbabak/noql-frontend-amd64:$VERSION
}

updateManifest() {
    echo "Updating manifest..."

    docker manifest create janbabak/noql-frontend:$VERSION \
        --amend janbabak/noql-frontend-arm64:$VERSION \
        --amend janbabak/noql-frontend-amd64:$VERSION

    docker manifest push janbabak/noql-frontend:$VERSION
}

buildImage

# if `-x push` is passed, only build the image without pushing
if [[ "$1" != "-x" || "$2" != "push" ]]; then
    pushImage
    updateManifest
else
    echo "Skipping push and manifest update."
fi