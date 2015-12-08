#!/usr/bin/env bash
REPOSITORY=bcokert                      # The dockerhub repository to pull from
IMAGE=elophant-db                       # The image for the container
VERSION=latest                          # The version tag for the container
DATABASE_NAME=elophant_db_1             # The name of the container that runs postgres and allows connections. This can be destroyed
DATABASE_PORT_INTERNAL=5432             # The port that the database will listen for connections on
DATABASE_PORT_EXTERNAL=5432             # The port that the docker host will expose for the database
VOLUME_CONTAINER_NAME=elophant_data     # The name of the container that persists the database
SETUP_CONTAINER_NAME=intitial_db_setup  # The name of the container that runs the database initialization

ERROR_TXT="\033[1m\033[41m\033[97mERROR:\033[0m"

echo "Checking that you are logged in to docker hub..."
if ! docker info | grep -q Username; then
  echo "You must login to pull from docker Hub (you only need to do this once):"
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

echo "Creating the data volume container..."
if docker ps -a | grep -q ${VOLUME_CONTAINER_NAME}; then
  echo "Volume container already exists, just creating new database container..."
  docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} -p ${DATABASE_PORT_INTERNAL}:${DATABASE_PORT_EXTERNAL} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} --name ${DATABASE_NAME} ${REPOSITORY}/${IMAGE}:${VERSION}
else
  echo "Volume container not found. Need to create this before starting the database container..."
  docker create --name ${VOLUME_CONTAINER_NAME} ${REPOSITORY}/${IMAGE}:${VERSION} /bin/true

  echo "Volume created. Starting a database container to intialize data structure. This will take 10-15 seconds..."
  docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} -p ${DATABASE_PORT_INTERNAL}:${DATABASE_PORT_EXTERNAL} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} --name ${DATABASE_NAME} ${REPOSITORY}/${IMAGE}:${VERSION}

  sleep 10
  echo "Data structure ready. Running initialization scripts..."
  docker run --rm --volumes-from ${VOLUME_CONTAINER_NAME} -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} --name ${SETUP_CONTAINER_NAME} ${REPOSITORY}/${IMAGE}:${VERSION} /elophant-scripts/main.sh

  # Re-perform the  steps of this script, now that the database is fully ready
  docker rm -f ${DATABASE_NAME}
  docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} -p ${DATABASE_PORT_INTERNAL}:${DATABASE_PORT_EXTERNAL} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} --name ${DATABASE_NAME} ${REPOSITORY}/${IMAGE}:${VERSION}
fi
