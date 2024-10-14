#! /bin/bash

# build the frontend docker image

VERSION=0.0.3
IMAGE_NAME=janbabak/noql-frontend
DOCKER_FILE=infra/dockerImages/frontend/frontend.Dockerfile

OPTIONS=$@ # allow to pass -x push argument that will build the image locally without pushing and updating the manifest

./infra/dockerImages/scripts/build-image.sh $IMAGE_NAME $VERSION $DOCKER_FILE $OPTIONS