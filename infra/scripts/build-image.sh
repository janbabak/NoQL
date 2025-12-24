#!/bin/bash
set -eo pipefail

# Multi-arch Docker build script for both arm64 and amd64

IMAGE_NAME=$1
VERSION=$2
DOCKER_FILE=$3
FLAG=${4:-}

show_help() {
    echo "Usage: $0 <image_name> <version> <docker_file_path> [-h|--help] [-x push]"
    echo "Example: $0 janbabak/noql-backend 0.0.3 infra/dockerImages/backend.Dockerfile"
    echo "Options:"
    echo "  -h, --help    Show this help message and exit"
    echo "  -x push       Build the images locally without pushing to Docker Hub"
}

processArguments() {
    if [[ "$IMAGE_NAME" == "-h" || "$IMAGE_NAME" == "--help" ]]; then
        show_help
        exit 0
    fi

    if [[ -z "$IMAGE_NAME" || -z "$VERSION" || -z "$DOCKER_FILE" ]]; then
        echo "Error: Image name, version, and Dockerfile path are required."
        show_help
        exit 1
    fi
}

processArguments

# Determine if we should push to Docker Hub
PUSH=true
if [[ "$FLAG" == "-x" && "${5:-}" == "push" ]]; then
    PUSH=false
fi

buildImage() {
    echo "Building multi-arch image $IMAGE_NAME:$VERSION..."

    if [[ $PUSH == true ]]; then
        docker buildx build \
            --platform linux/amd64,linux/arm64 \
            -t ${IMAGE_NAME}:$VERSION \
            -f $DOCKER_FILE \
            --push \
            .
    else
        docker buildx build \
            --platform linux/amd64,linux/arm64 \
            -t ${IMAGE_NAME}:$VERSION \
            -f $DOCKER_FILE \
            --load \
            .
    fi

    echo "Build completed for $IMAGE_NAME:$VERSION"
}

buildImage