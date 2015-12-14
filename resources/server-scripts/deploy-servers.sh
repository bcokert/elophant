#!/usr/bin/env bash
REPOSITORY=bcokert              # The dockerhub repository to pull from
WEB_IMAGE=elophant-server           # The image for the container
DB_IMAGE=elophant-db

### USAGE
function print_usage {
  echo "Usage:"
  echo "  deploy_servers.sh [-h|--help] [-d|--dry] [-p|--port] num_servers"
  echo
  echo "Options:"
  echo "  -h|--help            Display this help"
  echo "  --dry                Print out intentions, but do not take any actions"
  echo "  -d|--database        Deploy the database (will not wipe the data volume)"
  echo "  -S|--no-servers      Don't deploy the web servers"
  echo "  --prefix             The prefix for the names of each server container"
  echo "  -p|--port            The external port for the database, used for debugging"
  echo
  echo "Arguments:"
  echo "  num_servers          The number of servers to create"
}

### OPTIONS
DRY_RUN=false
INCLUDE_DATABASE=false
INCLUDE_SERVERS=true
SERVER_NAME_PREFIX=elophant_web
DATABASE_NAME=elophant_db
VOLUME_CONTAINER_NAME=elophant_data
DATABASE_PORT=5432

if [[ $# == 0 ]]; then print_usage; exit 1; fi
while [[ $# > 0 ]] ; do key="$1"
case ${key} in
    -h|--help) print_usage; exit 0;;
    --dry) DRY_RUN=true;;
    -p|--port) DATABASE_PORT=$2; shift;;
    -d|--database) INCLUDE_DATABASE=true;;
    -S|--no-servers) INCLUDE_SERVERS=false;;
    --prefix) SERVER_NAME_PREFIX=$2; shift;;
    -*) echo "Illegal Option: ${key}"; print_usage; exit 1;;
    *) break;
esac
shift
done

### REQUIRED
reNumber='^[0-9]+$'
if [ ${INCLUDE_SERVERS} = true ]; then
  if [[ $1 =~ $reNumber ]]; then
    NUM_SERVERS=$1
  else
    echo "First arg must be a number. Received: '$1'"; print_usage; exit 1
  fi
fi

if [ ${INCLUDE_DATABASE} = false ] && [ ${INCLUDE_SERVERS} = false ]; then
  echo "You must deploy either the database or servers or both"
  echo "Both:           'deploy-servers -d 4'"
  echo "Servers only:   'deploy-servers 4'"
  echo "Database only:  'deploy-servers -d -S'"
  exit 1
fi

if [ -z ${ELOPHANT_USER_PASSWORD} ] || [ -z ${ELOPHANT_SECRET} ] || [ -z ${ELOPHANT_ENV} ] || [ -z ${ELOPHANT_NETWORK} ] || ([ ${INCLUDE_DATABASE} = true ] && [ -z ${ELOPHANT_ADMIN_PASSWORD} ]); then
  echo "The following environment variables are required before servers can be deployed:"
  echo "ELOPHANT_USER_PASSWORD"
  echo "ELOPHANT_SECRET"
  echo "ELOPHANT_ENV"
  echo "ELOPHANT_NETWORK"
  if [ ${INCLUDE_DATABASE} = true ]; then
    echo "ELOPHANT_ADMIN_PASSWORD"
  fi
  exit 1;
fi

if which docker-machine | grep -q /*/docker-machine; then
  echo "Connecting to Docker VM..."
  eval "$(docker-machine env default)"
fi

if ! docker network ls | grep -q ${ELOPHANT_NETWORK}; then
  echo "The network must be running. Currently configured name is ${ELOPHANT_NETWORK}"; exit 1;
fi

echo "Checking that you are logged in to docker hub..."
if ! docker info | grep -q Username; then
  echo "You must login to push to docker Hub (you only need to do this once):"
  if [ ${DRY_RUN} = false ]; then
    docker login
  else
    echo "Dry Run: 'docker login'"
  fi
else
  echo "Successfully logged in!"
fi


### DB SERVER
if [ ${INCLUDE_DATABASE} = true ]; then
  echo "Deploying Database..."

  echo "Cleaning up any existing container..."
  if docker ps -a | grep -q ${DATABASE_NAME}; then
    if [ ${DRY_RUN} = false ]; then
      docker rm -f ${DATABASE_NAME}
    else
      echo "Dry Run: 'docker rm -f ${DATABASE_NAME}'"
    fi
  else
    echo "No existing container to stop"
  fi

  echo "Creating the data volume container..."
  if docker ps -a | grep -q ${VOLUME_CONTAINER_NAME}; then
    echo "Volume container already exists, just creating new database container..."
    if [ ${DRY_RUN} = false ]; then
      docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} --name ${DATABASE_NAME} ${REPOSITORY}/${DB_IMAGE}
    else
      echo "Dry Run: 'docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} --name ${DATABASE_NAME} ${REPOSITORY}/${DB_IMAGE}'"
    fi
  else
    echo "Volume container not found. Need to create this before starting the database container..."
    if [ ${DRY_RUN} = false ]; then
      docker create --name ${VOLUME_CONTAINER_NAME} ${REPOSITORY}/${DB_IMAGE} /bin/true
    else
      echo "Dry Run: 'docker create --name ${VOLUME_CONTAINER_NAME} ${REPOSITORY}/${DB_IMAGE} /bin/true'"
    fi

    echo "Volume created. Starting a database container to intialize data structure. This will take 10-15 seconds..."
    if [ ${DRY_RUN} = false ]; then
      docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} --name ${DATABASE_NAME} ${REPOSITORY}/${DB_IMAGE}
      sleep 10
    else
      echo "Dry Run: 'docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} --name ${DATABASE_NAME} ${REPOSITORY}/${DB_IMAGE}'"
    fi

    echo "Data structure ready. Running initialization scripts..."
    if [ ${DRY_RUN} = false ]; then
      docker run --rm --volumes-from ${VOLUME_CONTAINER_NAME} -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} --name setup_db ${REPOSITORY}/${DB_IMAGE} /elophant-scripts/main.sh
    else
      echo "Dry Run: 'docker run --rm --volumes-from ${VOLUME_CONTAINER_NAME} -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} --name setup_db ${REPOSITORY}/${DB_IMAGE} /elophant-scripts/main.sh'"
    fi

    # Re-perform the  steps of this script, now that the database is fully ready
    if [ ${DRY_RUN} = false ]; then
      docker rm -f ${DATABASE_NAME}
      docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} -p ${DATABASE_PORT}:5432 --net=${ELOPHANT_NETWORK} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} --name ${DATABASE_NAME} ${REPOSITORY}/${DB_IMAGE}
    else
      echo "Dry Run: 'docker rm -f ${DATABASE_NAME}'"
      echo "Dry Run: 'docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} --name ${DATABASE_NAME} ${REPOSITORY}/${DB_IMAGE}'"
    fi
  fi
fi



### WEB SERVERS
if [ ${INCLUDE_SERVERS} = true ]; then
  if ! docker ps -a | grep -q elophant-db.*Up; then
    echo "The database must be running"
    if [ ${DRY_RUN} = false ]; then
      exit 1;
    fi
  fi

  echo "Deploying Web Servers..."

  echo "Pulling latest server image..."
  if [ ${DRY_RUN} = false ]; then
    docker pull ${REPOSITORY}/${WEB_IMAGE}
  else
    echo "Dry Run: 'docker pull ${REPOSITORY}/${WEB_IMAGE}'"
  fi

  echo "Cleaning up any existing containers..."
  if docker ps -a | grep -q ${SERVER_NAME_PREFIX}; then
    if [ ${DRY_RUN} = false ]; then
      docker rm -f $(docker ps -a | grep ${SERVER_NAME_PREFIX} | cut -d ' ' -f1)
    else
      echo "Dry Run: 'docker rm -f docker rm -f $(docker ps -a | grep ${SERVER_NAME_PREFIX} | cut -d ' ' -f1)'"
    fi
  else
    echo "No existing service to remove"
  fi

  echo "Starting new server containers..."
  if [ ${DRY_RUN} = false ]; then
    for (( i=1; i<=${NUM_SERVERS}; i++ )); do
      docker run -d --net=${ELOPHANT_NETWORK} -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} -e ELOPHANT_SECRET=${ELOPHANT_SECRET} -e ELOPHANT_ENV=${ELOPHANT_ENV} --name ${SERVER_NAME_PREFIX}_${i} ${REPOSITORY}/${WEB_IMAGE}
    done
  else
    for (( i=1; i<=${NUM_SERVERS}; i++ )); do
      echo "Dry Run: 'docker run -d --net=${ELOPHANT_NETWORK} -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} -e ELOPHANT_SECRET=${ELOPHANT_SECRET} -e ELOPHANT_ENV=${ELOPHANT_ENV} --name ${SERVER_NAME_PREFIX}_${i} ${REPOSITORY}/${WEB_IMAGE}'"
    done
  fi
fi
