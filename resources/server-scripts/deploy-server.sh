#!/usr/bin/env bash
REPOSITORY=bcokert              # The dockerhub repository to pull from
IMAGE=elophant-server           # The image for the container
VERSION=latest                  # The version tag for the container
SERVER_NAME=elophant_server_1   # The name of the container that runs the service
SERVER_PORT_INTERNAL=9000       # The port that the server is listening to for requests
SERVER_PORT_EXTERNAL=9000       # The port that the docker host will expose (Not the same as the gateway port)
SERVER_START_SCRIPT=/usr/local/lib/elophant-server/bin/elophant # The script that will start the server

ERROR_TXT="\033[1m\033[41m\033[97mERROR:\033[0m"

echo "Checking that you are logged in to docker hub..."
if ! docker info | grep -q Username; then
  echo "You must login to push to docker Hub (you only need to do this once):"
  docker login
else
  echo "Succesfully logged in!"
fi

echo "Pulling latest server image..."
docker pull ${REPOSITORY}/${IMAGE}:${VERSION}

echo "Cleaning up any existing container..."
if docker ps -a | grep -q ${SERVER_NAME}; then
  docker rm -f ${SERVER_NAME}
else
  echo "No existing service to remove"
fi

echo "Starting a new server container..."
docker run -d -p ${SERVER_PORT_INTERNAL}:${SERVER_PORT_EXTERNAL} --name ${SERVER_NAME} ${REPOSITORY}/${IMAGE}:${VERSION} ${SERVER_START_SCRIPT}
