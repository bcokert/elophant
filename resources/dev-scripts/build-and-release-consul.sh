#!/usr/bin/env bash
ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/../..
RESOURCE_DIR=${ROOT_DIR}/resources
CONSUL_CONFIG_DIR=${RESOURCE_DIR}/consul.d
CONSUL_SCRIPTS_DIR=${RESOURCE_DIR}/consul-scripts
BUILD_DIR=${RESOURCE_DIR}/build
DOCKER_FILE_NAME=Dockerfile-consul
OUTPUT_DIR=${ROOT_DIR}/release-consul-tmp
REPOSITORY=bcokert
IMAGE=elophant-consul

ERROR_TXT="\033[1m\033[41m\033[97mERROR:\033[0m"

echo "Checking that docker VM is available..."
if ! docker-machine ls | grep -q default; then
  echo -e "${ERROR_TXT} Docker VM is not created. Please run 'docker-machine create --driver virtualbox default'"
  exit 1
elif ! docker-machine ls | grep -q default.*Running; then
  echo -e "${ERROR_TXT} Docker VM is not running. Please run 'docker-machine start default'"
  exit 1
fi

echo "Cleaning any old default release files..."
rm -rf ${OUTPUT_DIR}
mkdir ${OUTPUT_DIR}

echo "Preparing build artifacts for docker imaging..."
cp -r ${CONSUL_CONFIG_DIR} ${OUTPUT_DIR}
cp ${CONSUL_SCRIPTS_DIR}/* ${OUTPUT_DIR}
cp ${BUILD_DIR}/${DOCKER_FILE_NAME} ${OUTPUT_DIR}
mv ${OUTPUT_DIR}/${DOCKER_FILE_NAME} ${OUTPUT_DIR}/Dockerfile

echo "Connecting to Docker VM..."
eval "$(docker-machine env default)"

echo "Building Docker Image..."
LATEST_BUILD=latest
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
echo "If this fails saying it's already in progress, try 'docker-machine restart default'"
docker push ${REPOSITORY}/${IMAGE}
