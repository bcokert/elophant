#!/usr/bin/env bash
REPOSITORY=bcokert              # The dockerhub repository to pull from
IMAGE=elophant-db               # The image for the container
VERSION=latest                  # The version tag for the container
DATABASE_NAME=elophant_db_1     # The name of the container that runs postgres and allows connections. This can be destroyed
DATABASE_PORT_INTERNAL=5432     # The port that the database will listen for connections on
DATABASE_PORT_EXTERNAL=5432     # The port that the docker host will expose for the database
DATA_VOLUME_NAME=elophant_data  # The name of the container that simply persists the folder that contains the data. This should not be destroyed
DATA_VOLUME_MOUNT_POINT=/dbdata # The directory path to where the data lives (this is mounted from the docker host)

ERROR_TXT="\033[1m\033[41m\033[97mERROR:\033[0m"

echo "Checking that you are logged in to docker hub..."
if ! docker info | grep -q Username; then
  echo "You must login to push to docker Hub (you only need to do this once):"
  docker login
else
  echo "Succesfully logged in!"
fi

echo "Pulling latest image..."
docker pull ${REPOSITORY}/${IMAGE}:${VERSION}

echo "Cleaning up any existing container..."
if docker ps -a | grep -q ${DATABASE_NAME}; then
  docker rm -f ${DATABASE_NAME}
else
  echo "No existing container to stop"
fi

echo "Creating Data Volume Container if it doesn't already exist..."
if docker ps -a | grep -q ${DATA_VOLUME_NAME}; then
  echo "Data volume already exists!"
else
  docker create -v ${DATA_VOLUME_MOUNT_POINT} --name ${DATA_VOLUME_NAME} ${REPOSITORY}/${IMAGE}:${VERSION} /bin/true
fi

echo "Starting a new database container..."
docker run -d -p ${DATABASE_PORT_INTERNAL}:${DATABASE_PORT_EXTERNAL} --name ${DATABASE_NAME} ${REPOSITORY}/${IMAGE}:${VERSION}
