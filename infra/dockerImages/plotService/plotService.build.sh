#! /bin/bash

# build the plot service docker image

VERSION=0.0.1
IMAGE_NAME=janbabak/plot-service
DOCKER_FILE=infra/dockerImages/plotService/plotService.Dockerfile

OPTIONS=$@ # allow to pass -x push argument that will build the image locally without pushing and updating the manifest

./infra/dockerImages/scripts/build-image.sh $IMAGE_NAME $VERSION $DOCKER_FILE $OPTIONS