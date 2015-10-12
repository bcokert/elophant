#!/usr/bin/env bash
ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/../..
RESOURCE_DIR=${ROOT_DIR}/resources
BUILD_DIR=${RESOURCE_DIR}/build
SCRIPT_DIR=${RESOURCE_DIR}/db-init-scripts
DOCKER_FILE_NAME=Dockerfile-db
OUTPUT_DIR=${ROOT_DIR}/release-db-tmp
REPOSITORY=bcokert
IMAGE=elophant-db

ERROR_TXT="\033[1m\033[41m\033[97mERROR:\033[0m"

echo "Cleaning any old local release files..."
rm -rf ${OUTPUT_DIR}
mkdir ${OUTPUT_DIR}

echo "Preparing build artifacts for docker imaging..."
cp -r ${SCRIPT_DIR} ${OUTPUT_DIR}
cp ${BUILD_DIR}/${DOCKER_FILE_NAME} ${OUTPUT_DIR}
mv ${OUTPUT_DIR}/${DOCKER_FILE_NAME} ${OUTPUT_DIR}/Dockerfile

echo "Checking that docker VM is available..."
if ! docker-machine ls | grep -q default; then
  echo -e "${ERROR_TXT} Docker VM is not started. Please run 'docker-machine create --driver virtualbox default'"
fi

echo "Connecting to Docker VM..."
eval "$(docker-machine env default)"

echo "Building Docker Image..."
VERSIONED_BUILD=v$(grep -o 'version\s*:=.\+' $ROOT_DIR/build.sbt | grep -o '\d.\d.\d')
LATEST_BUILD=latest
docker build -t ${REPOSITORY}/${IMAGE}:${VERSIONED_BUILD} ${OUTPUT_DIR}
docker build -t ${REPOSITORY}/${IMAGE}:${LATEST_BUILD} ${OUTPUT_DIR}
docker images | grep ${IMAGE}

echo "Cleaning up build artifacts..."
rm -rf ${OUTPUT_DIR}

echo "Checking that you are logged in to docker hub..."
if ! docker info | grep -q Username; then
  echo "You must login to push to docker Hub (you only need to do this once):"
  docker login
else
  echo "Succesfully logged in!"
fi

echo "Pushing docker images..."
docker push ${REPOSITORY}/${IMAGE}
