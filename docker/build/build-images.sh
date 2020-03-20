#!/bin/bash

set -e
set -x

# build base image
docker inspect --type=image nautilus-cloud:base-latest &>/dev/null || ./docker/base/build-base-image.sh
# 2-stage build in docker 
docker build -t nautilus-cloud -f ./docker/build/dockerfile-build .
