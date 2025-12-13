#! /bin/bash

# build requested docker image for both arm64 and amd64 architectures.

IMAGE_NAME=$1
VERSION=$2
DOCKER_FILE=$3

show_help() {
    echo "Usage: $0 <image_name> <version> <docker_file_path> [-h|--help] [-x push]"
    echo "Example: $0 janbabak/noql-frontend 0.0.3"
    echo "Options:"
    echo "  -h, --help    Show this help message and exit"
    echo "  -x push       Build the image locally without pushing and updating the manifest"
}

buildImage() {
    echo "Building $IMAGE_NAME:$VERSION image..."

    docker build \
        -t ${IMAGE_NAME}-arm64:$VERSION \
        -f $DOCKER_FILE \
        --platform linux/arm64 .

    docker build \
        -t ${IMAGE_NAME}-amd64:$VERSION \
        -f $DOCKER_FILE \
        --platform linux/amd64 .
}

pushImage() {
    echo "Pushing $IMAGE_NAME:$VERSION image..."

    docker push ${IMAGE_NAME}-arm64:$VERSION
    docker push ${IMAGE_NAME}-amd64:$VERSION
}

updateManifest() {
    echo "Updating manifest..."

    docker manifest create ${IMAGE_NAME}:$VERSION \
        ${IMAGE_NAME}-arm64:$VERSION \
        ${IMAGE_NAME}-amd64:$VERSION

    docker manifest push ${IMAGE_NAME}:$VERSION
}

processArguments() {
    if [[ "$1" == "-h" || "$1" == "--help" ]]; then
        show_help
        exit 0
    fi

    if [[ -z "$IMAGE_NAME" || -z "$VERSION" || -z "$DOCKER_FILE" ]]; then
        echo "Error: Image name and version are required."
        show_help
        exit 1
    fi
}

processArguments
buildImage

# if `-x push` is passed, only build the image without pushing
if [[ "$4" != "-x" || "$5" != "push" ]]; then
    pushImage
    updateManifest
else
    echo "Skipping push and manifest update."
fi