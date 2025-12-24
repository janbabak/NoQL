#!/bin/bash
set -eo pipefail

# Multi-arch Docker build script for both arm64 and amd64
# Usage: ./build-image.sh <image_name> <version> <docker_file_path> [-x] [-c <context>]

IMAGE_NAME=$1
VERSION=$2
DOCKER_FILE=$3
CONTEXT=""
PUSH=true

# Parse flags
shift 3
while [[ $# -gt 0 ]]; do
    case "$1" in
        -x)
            PUSH=false
            shift
            ;;
        -c)
            CONTEXT="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 <image_name> <version> <docker_file_path> [-x] [-c <context>]"
            echo "Example: $0 janbabak/noql-frontend 0.0.1 frontend/NoQL/frontend.Dockerfile -x -c frontend/NoQL"
            echo "Options:"
            echo "  -x         Build locally without pushing to Docker Hub"
            echo "  -c <path>  Specify build context (defaults to Dockerfile folder)"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

if [[ -z "$CONTEXT" ]]; then
    CONTEXT=$(dirname "$DOCKER_FILE")
fi

echo "Building multi-arch image $IMAGE_NAME:$VERSION..."
echo "Using Dockerfile: $DOCKER_FILE"
echo "Using build context: $CONTEXT"
echo "Push enabled: $PUSH"

# Build the image
if [[ $PUSH == true ]]; then
    docker buildx build \
        --platform linux/amd64,linux/arm64 \
        -t "${IMAGE_NAME}:${VERSION}" \
        -f "$DOCKER_FILE" \
        --push \
        "$CONTEXT"
else
    docker buildx build \
        --platform linux/amd64,linux/arm64 \
        -t "${IMAGE_NAME}:${VERSION}" \
        -f "$DOCKER_FILE" \
        --load \
        "$CONTEXT"
fi

echo "Build completed for $IMAGE_NAME:$VERSION"