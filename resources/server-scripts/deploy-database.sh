#!/usr/bin/env bash
REPOSITORY=bcokert                  # The dockerhub repository to pull from
IMAGE=elophant-db                   # The image for the container
VERSION=latest                      # The version tag for the container
DATABASE_NAME=elophant_db_1         # The name of the container that runs postgres and allows connections. This can be destroyed
DATABASE_PORT_INTERNAL=5432         # The port that the database will listen for connections on
DATABASE_PORT_EXTERNAL=5432         # The port that the docker host will expose for the database
VOLUME_CONTAINER_NAME=elophant_data # The name of the container that persists the database

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
  echo "Volume container already created..."
else
  docker create -v /var/log/postgresql -v /var/lib/postgresql --name ${VOLUME_CONTAINER_NAME} ${REPOSITORY}/${IMAGE}:${VERSION} /bin/true
fi

echo "Starting a new database container..."
read -p "Please enter the password of the admin user (username 'postgres'): " -s ADMIN_PASS
echo
docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} -p ${DATABASE_PORT_INTERNAL}:${DATABASE_PORT_EXTERNAL} -e POSTGRES_PASSWORD=${ADMIN_PASS} --name ${DATABASE_NAME} ${REPOSITORY}/${IMAGE}:${VERSION}
