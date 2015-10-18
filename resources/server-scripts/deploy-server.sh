#!/usr/bin/env bash
REPOSITORY=bcokert              # The dockerhub repository to pull from
IMAGE=elophant-server           # The image for the container
VERSION=latest                  # The version tag for the container
SERVER_NAME=elophant_server_1   # The name of the container that runs the service
DATABASE_NAME=elophant_db_1     # The name of the database container. Used for linking
SERVER_PORT_INTERNAL=9000       # The port that the server is listening to for requests
SERVER_PORT_EXTERNAL=9000       # The port that the docker host will expose (Not the same as the gateway port)

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
read -p "Please enter the password of the database user (username 'elophantuser'): " -s USER_PASS
echo
docker run -d --link ${DATABASE_NAME}:database -p ${SERVER_PORT_INTERNAL}:${SERVER_PORT_EXTERNAL} -e ELOPHANT_USER_PASSWORD=${USER_PASS} --name ${SERVER_NAME} ${REPOSITORY}/${IMAGE}:${VERSION}
