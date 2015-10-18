#!/usr/bin/env bash
REPOSITORY=bcokert                  # The dockerhub repository to pull from
IMAGE=elophant-db                   # The image for the container
VERSION=latest                      # The version tag for the container
CONTAINER_NAME=intitial_db_setup    # The name of the container that runs postgres and allows connections. This can be destroyed
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

echo "Ensuring the data container exists..."
if docker ps -a | grep -q ${VOLUME_CONTAINER_NAME}; then
  echo "Volume container exists..."
else
  echo "${ERROR_TXT} The first setup script depends on the data container existing and the server having been run once."
  echo "${ERROR_TXT} Please run deploy-database.sh first, then run this script."
  exit 1
fi

echo "Running a temporary container to setup the database..."
echo "Within this container is where you'll setup. These basic commands should be enough, but you can customize db's and"
echo "users as well:"
echo
echo "su postgres"
echo "PATH=/usr/lib/postgresql/9.4/bin:\$PATH"
echo "echo \"listen_addresses='*'\" >> /var/lib/postgresql/data/postgresql.conf"
echo "/usr/lib/postgresql/9.4/bin/postgres & # run the server in this temporary container so that we can add users and data"
echo "psql"
echo "CREATE USER elophantuser WITH PASSWORD 'xxx'"
echo "CREATE DATABASE elophant OWNER elophantuser"
echo "pg_ctl stop # Exit postgres. The data was saved to a volume, so the real service will have access to it"
docker run -ti --rm --volumes-from ${VOLUME_CONTAINER_NAME} --name ${CONTAINER_NAME} ${REPOSITORY}/${IMAGE}:${VERSION} /bin/bash