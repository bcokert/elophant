#!/usr/bin/env bash

# Constants
REPOSITORY=bcokert
DOCKER_MACHINE=default

ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/../..
RESOURCE_DIR=${ROOT_DIR}/resources

WEB_IMAGE=elophant-web
DB_IMAGE=elophant-db
HAPROXY_IMAGE=elophant-haproxy
CONSUL_SERVER_IMAGE=elophant-consul-server
CONSUL_CLIENT_IMAGE=elophant-consul-client
ALL_IMAGES="|${WEB_IMAGE}|${DB_IMAGE}|${HAPROXY_IMAGE}|${CONSUL_SERVER_IMAGE}|${CONSUL_CLIENT_IMAGE}|"

ERROR_TXT="\033[1m\033[41m\033[97mERROR:\033[0m"

# Usage Function
function print_usage {
  echo "Builds a specific docker image"
  echo
  echo "Usage:"
  echo "  build.sh [-h|--help] [-r|--release] [-C|--no-clean] image_name"
  echo
  echo "Options:"
  echo "  -h|--help           Display this help"
  echo "  -r|--release        Release after building"
  echo "  -C|--no-clean       Don't cleanup the release dir when finished"
  echo
  echo "Arguments:"
  echo "  image_name          The name of the image to build. Must be one of:"
  echo "    ${ALL_IMAGES}"
  echo
}

# Process Options
RELEASE=false
CLEAN=true

if [[ $# == 0 ]]; then print_usage; exit 1; fi
while [[ $# > 0 ]] ; do key="$1"
case ${key} in
  -h|--help) print_usage; exit 0;;
  -r|--release) RELEASE=true;;
  -C|--no-clean) CLEAN=false;;
  -*) echo -e "${ERROR_TXT} Illegal Option: ${key}"; print_usage; exit 1;;
  *) break;
esac
shift
done

# Process Arguments
IMAGE=false

if [[ ${ALL_IMAGES} == *"|$1|"* ]]; then
  IMAGE=$1
else
  echo -e "${ERROR_TXT} First arg must be an image name: '$1'"; print_usage; exit 1
fi

# Initialize Variables
CONTAINER_DIR=${RESOURCE_DIR}/container/${IMAGE}
OUTPUT_ROOT=${ROOT_DIR}/release-tmp
OUTPUT_DIR=${OUTPUT_ROOT}/${IMAGE}

# Check that docker-machine is running
echo "Checking that docker VM is available..."
if ! docker-machine ls | grep -q ${DOCKER_MACHINE}; then
  echo -e "${ERROR_TXT} Docker VM is not created. Please run 'docker-machine create --driver virtualbox ${DOCKER_MACHINE}'"
  exit 1
elif ! docker-machine ls | grep -q ${DOCKER_MACHINE}.*Running; then
  echo -e "${ERROR_TXT} Docker VM is not running. Please run 'docker-machine start ${DOCKER_MACHINE}'"
  exit 1
fi

# Connect to the docker-machine
echo "Connecting to Docker VM..."
eval "$(docker-machine env default)"

# Cleanup the output dir
echo "Cleaning any old default release files..."
rm -rf ${OUTPUT_DIR}
mkdir -p ${OUTPUT_DIR}

# Build and prepare the artifacts and config
echo "Building artifacts (${IMAGE})..."
cp -r ${CONTAINER_DIR}/* ${OUTPUT_DIR}

case ${IMAGE} in
  ${WEB_IMAGE})
    ${ROOT_DIR}/activator clean compile dist
    WEB_ARTIFACT_DIR=${ROOT_DIR}/target/universal
    WEB_ARTIFACT=elophant-*.zip
    WEB_ARTIFACT_EXTRACTED_DIR=elophant-*/
    cp ${WEB_ARTIFACT_DIR}/${WEB_ARTIFACT} ${OUTPUT_DIR}
    cd ${OUTPUT_DIR}
    unzip ${WEB_ARTIFACT} > /dev/null
    mv ${WEB_ARTIFACT_EXTRACTED_DIR} elophant-web
    rm ${WEB_ARTIFACT}
    cd ..
  ;;
  ${DB_IMAGE})
  ;;
  ${HAPROXY_IMAGE})
  ;;
  ${CONSUL_SERVER_IMAGE})
  ;;
  ${CONSUL_CLIENT_IMAGE})
  ;;
  *) echo -e "${ERROR_TXT} Illegal Image: ${IMAGE}"; print_usage; exit 1;
esac

# Build the Image
echo "Building Docker Image..."
LATEST_BUILD=latest
docker build -t ${REPOSITORY}/${IMAGE}:latest ${OUTPUT_DIR}
docker images | grep ${IMAGE}

# Cleanup the temp output dir
echo "Cleaning up build artifacts..."
if [ ${CLEAN} = true ]; then
  rm -rf ${OUTPUT_ROOT}
fi

# Deploy the image to registry
if [ ${RELEASE} = true ]; then
  echo "Checking that you are logged in to docker hub..."
  if ! docker info | grep -q Username; then
    echo "You must login to push to docker Hub (you only need to do this once):"
    docker login
  else
    echo "Succesfully logged in!"
  fi

  echo "Pushing docker images..."
  echo "  If this fails saying it's already in progress, try 'docker-machine restart default'"
  docker push ${REPOSITORY}/${IMAGE}:latest
fi
