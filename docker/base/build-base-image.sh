#!/bin/bash


export IMAGE_NAME=${IMAGE_NAME:-"nautilus-cloud"}
export IMAGE_VERSION=${IMAGE_VERSION:-"base-latest"}
export DOCKER_REPO=${DOCKER_REPO:-"cryptonomic"}

docker build \
--build-arg BASE_IMAGE_TAG="8u222-jdk-stretch" \
--build-arg SBT_VERSION="1.2.8" \
--build-arg SCALA_VERSION="2.12.8" \
--build-arg USER_ID=1001 \
--build-arg GROUP_ID=1001 \
-t $IMAGE_NAME:base-tmp \
github.com/hseeberger/scala-sbt.git#:debian

docker build -f docker/base/dockerfile-base -t $IMAGE_NAME:$IMAGE_VERSION .
