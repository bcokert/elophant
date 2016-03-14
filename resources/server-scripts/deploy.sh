#!/usr/bin/env bash
REPOSITORY=bcokert
WEB_IMAGE=elophant-web
DB_IMAGE=elophant-db
CONSUL_SERVER_IMAGE=elophant-consul-server
CONSUL_CLIENT_IMAGE=elophant-consul-client
HAPROXY_IMAGE=elophant-haproxy
SERVER_NAME_PREFIX=elophant_web
DOCKER_MACHINE_NAME=default

### USAGE
function print_usage {
  echo "Usage:"
  echo "  deploy_servers.sh [-h|--help] [--dry] [-d|--database] [-p|--port] [-c|--consul] [-l|--lb num_lbs] [-S|--no-servers] num_servers"
  echo
  echo "Options:"
  echo "  -h|--help            Display this help"
  echo "  --dry                Print out intentions, but do not take any actions"
  echo "  -d|--database        Deploy the database (will not wipe the data volume)"
  echo "  -c|--consul          Whether to redeploy the consul servers"
  echo "  -l|--lb num          How many load balancers to deploy. Defaults to 0"
  echo "  -S|--no-servers      Don't deploy the web servers"
  echo "  -p|--port            The external port for the database, used for debugging"
  echo
  echo "Arguments:"
  echo "  num_servers          The number of servers to create"
  echo
  echo "Examples:"
  echo "  Dry run of everything: 'deploy-servers -c -d --dry 4'"
  echo "  5 Servers only:        'deploy-servers 5'"
  echo "  Database only:         'deploy-servers -d -S'"
  echo "  Consul servers only:   'deploy-servers -c -S'"
  echo "  Load Balancers only:   'deploy-servers -l -S'"
  echo "  All:                   'deploy-servers -c -l 2 -d 4'"
}

### OPTIONS
DRY_RUN=false
INCLUDE_DATABASE=false
INCLUDE_SERVERS=true
INCLUDE_CONSUL=false
INCLUDE_LOAD_BALANCERS=false
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
    -c|--consul) INCLUDE_CONSUL=true;;
    -l|--lb) INCLUDE_LOAD_BALANCERS=true; NUM_LOAD_BALANCERS=$2; shift;;
    -S|--no-servers) INCLUDE_SERVERS=false;;
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

if [ ${INCLUDE_LOAD_BALANCERS} = true ]; then
  if ! [[ ${NUM_LOAD_BALANCERS} =~ $reNumber ]]; then
    echo "Argument to -l must be a number. Received: '${NUM_LOAD_BALANCERS}'"; print_usage; exit 1
  fi
fi

if [ ${INCLUDE_DATABASE} = false ] && [ ${INCLUDE_SERVERS} = false ] && [ ${INCLUDE_CONSUL} = false ] && [ ${INCLUDE_LOAD_BALANCERS} = false ]; then
  echo "You must deploy at least one of: servers, database, consul cluster"
  echo "Servers only:   'deploy-servers 4'"
  echo "Database only:  'deploy-servers -d -S'"
  echo "Consul only:    'deploy-servers -c -S'"
  echo "Haproxy only:   'deploy-servers -l 2 -S'"
  echo "All:            'deploy-servers -c -l 2 -d 4"
  exit 1
fi

if [ -z ${ELOPHANT_USER_PASSWORD} ] || [ -z ${ELOPHANT_SECRET} ] || [ -z ${ELOPHANT_NETWORK} ] || ([ ${INCLUDE_DATABASE} = true ] && [ -z ${ELOPHANT_ADMIN_PASSWORD} ]); then
  echo "The following environment variables are required before servers can be deployed:"
  echo "ELOPHANT_USER_PASSWORD"
  echo "ELOPHANT_SECRET"
  echo "ELOPHANT_NETWORK"
  if [ ${INCLUDE_DATABASE} = true ]; then
    echo "ELOPHANT_ADMIN_PASSWORD"
  fi
  exit 1;
fi

if which docker-machine | grep -q /*/docker-machine; then
  echo "Connecting to Docker VM..."
  eval "$(docker-machine env ${DOCKER_MACHINE_NAME})"
fi

if ! docker network ls | grep -q ${ELOPHANT_NETWORK}; then
  docker network create ${ELOPHANT_NETWORK}
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



### CONSUL SERVERS
if [ ${INCLUDE_CONSUL} = true ]; then

  echo "Deploying Consul Servers..."

  echo "Pulling latest consul server image..."
  if [ ${DRY_RUN} = false ]; then
    docker pull ${REPOSITORY}/${CONSUL_SERVER_IMAGE}
  else
    echo "Dry Run: 'docker pull ${REPOSITORY}/${CONSUL_SERVER_IMAGE}'"
  fi

  echo "Cleaning up any existing containers..."
  if docker ps -a | grep -q ${CONSUL_SERVER_IMAGE}; then
    if [ ${DRY_RUN} = false ]; then
      docker rm -f $(docker ps -a | grep ${CONSUL_SERVER_IMAGE} | cut -d ' ' -f1)
    else
      echo "Dry Run: 'docker rm -f docker rm -f $(docker ps -a | grep ${CONSUL_SERVER_IMAGE} | cut -d ' ' -f1)'"
    fi
  else
    echo "No existing consul service to remove"
  fi

  echo "Starting new consul server containers..."
  if [ ${DRY_RUN} = false ]; then
    for (( i=1; i<=5; i++ )); do
      docker run -d --net=${ELOPHANT_NETWORK} -p 850${i}:8500 -e NETWORK=${ELOPHANT_NETWORK} -e CONSUL_NODE_NUMBER=${i} --name elophant_consul_server_${i} ${REPOSITORY}/${CONSUL_SERVER_IMAGE}:latest
    done
  else
    for (( i=1; i<=5; i++ )); do
      echo "Dry Run: docker run -d --net=${ELOPHANT_NETWORK} -p 850${i}:8500 -e NETWORK=${ELOPHANT_NETWORK} -e CONSUL_NODE_NUMBER=${i} --name elophant_consul_server_${i} ${REPOSITORY}/${CONSUL_SERVER_IMAGE}:latest"
    done
  fi

  echo "Giving consul servers a few seconds to elect someone..."
  if [ ${DRY_RUN} = false ]; then
    sleep 5
  fi

  echo "  The ui is available on each server, under port 850X, where X is the server number"
  echo "  Eg: http://docker.host.address:8502/  is the ui served by host 2"

  echo "Finished deploying consul cluster!"
fi



### CONSUL CLIENTS
if [ ${INCLUDE_CONSUL} = true ]; then

  echo "Deploying Consul Clients..."

  echo "Pulling latest consul client image..."
  if [ ${DRY_RUN} = false ]; then
    docker pull ${REPOSITORY}/${CONSUL_CLIENT_IMAGE}
  else
    echo "Dry Run: 'docker pull ${REPOSITORY}/${CONSUL_CLIENT_IMAGE}'"
  fi

  echo "Cleaning up any existing containers..."
  if docker ps -a | grep -q ${CONSUL_CLIENT_IMAGE}; then
    if [ ${DRY_RUN} = false ]; then
      docker rm -f $(docker ps -a | grep ${CONSUL_CLIENT_IMAGE} | cut -d ' ' -f1)
    else
      echo "Dry Run: 'docker rm -f docker rm -f $(docker ps -a | grep ${CONSUL_CLIENT_IMAGE} | cut -d ' ' -f1)'"
    fi
  else
    echo "No existing consul client to remove"
  fi

  echo "Starting new consul client containers..."
  if [ ${DRY_RUN} = false ]; then
    docker run -d --net=${ELOPHANT_NETWORK} -e DOCKER_HOST=tcp://127.0.0.1:2376 -e CONSUL_NODE_NAME=elophant_consul_client -e CONSUL_SERVERS="elophant_consul_server_1 elophant_consul_server_2 elophant_consul_server_3 elophant_consul_server_4 elophant_consul_server_5" --name elophant_consul_client ${REPOSITORY}/${CONSUL_CLIENT_IMAGE}:latest
  else
    echo "Dry Run: docker run -d --net=${ELOPHANT_NETWORK} -e CONSUL_NODE_NAME=elophant_consul_client -e CONSUL_SERVERS=\"elophant_consul_server_1 elophant_consul_server_2 elophant_consul_server_3 elophant_consul_server_4 elophant_consul_server_5\" --name elophant_consul_client ${REPOSITORY}/${CONSUL_CLIENT_IMAGE}:latest"
  fi

  echo "Finished deploying consul clients!"
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
      docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -p 5432:5432 -e ELOPHANT_AWS_KEY_BACKUP_CREATE=${ELOPHANT_AWS_KEY_BACKUP_CREATE} -e ELOPHANT_AWS_SECRET_BACKUP_CREATE=${ELOPHANT_AWS_SECRET_BACKUP_CREATE} -e ELOPHANT_DB_BACKUP_FREQUENCY_DAYS=${ELOPHANT_DB_BACKUP_FREQUENCY_DAYS} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} -e CONSUL_CLIENT_ADDRESS=elophant_consul_client:8500 -e CONSUL_SERVICE_ADDRESS=${DATABASE_NAME} -e CONSUL_SERVICE_PORT=5432 -e CONSUL_SERVICE_NAME=elophant-db --name ${DATABASE_NAME} ${REPOSITORY}/${DB_IMAGE}:latest
    else
      echo "Dry Run: 'docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -p 5432:5432 -e ELOPHANT_AWS_KEY_BACKUP_CREATE=${ELOPHANT_AWS_KEY_BACKUP_CREATE} -e ELOPHANT_AWS_SECRET_BACKUP_CREATE=${ELOPHANT_AWS_SECRET_BACKUP_CREATE} -e ELOPHANT_DB_BACKUP_FREQUENCY_DAYS=${ELOPHANT_DB_BACKUP_FREQUENCY_DAYS} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} -e CONSUL_CLIENT_ADDRESS=elophant_consul_client:8500 -e CONSUL_SERVICE_ADDRESS=${DATABASE_NAME} -e CONSUL_SERVICE_PORT=5432 -e CONSUL_SERVICE_NAME=elophant-db --name ${DATABASE_NAME} ${REPOSITORY}/${DB_IMAGE}:latest'"
    fi
  else
    echo "Volume container not found. Need to create this before starting the database container..."
    if [ ${DRY_RUN} = false ]; then
      docker create --name ${VOLUME_CONTAINER_NAME} ${REPOSITORY}/${DB_IMAGE} /bin/true
    else
      echo "Dry Run: 'docker create --name ${VOLUME_CONTAINER_NAME} ${REPOSITORY}/${DB_IMAGE} /bin/true'"
    fi

    echo "Volume created. Initializing database. This will take 10 seconds..."
    if [ ${DRY_RUN} = false ]; then
      docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} --name database_init ${REPOSITORY}/${DB_IMAGE}:latest
      sleep 10
      docker rm -f database_init
      docker run --rm --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} --name database_init ${REPOSITORY}/${DB_IMAGE}:latest /usr/bin/db-init/main.sh
    else
      echo "Dry Run: 'docker run --rm --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -e INIT_MODE=true -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} --name database_init ${REPOSITORY}/${DB_IMAGE}:latest'"
    fi

    echo "Done intializing. Creating database container..."
    if [ ${DRY_RUN} = false ]; then
      docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -p 5432:5432 -e ELOPHANT_AWS_KEY_BACKUP_CREATE=${ELOPHANT_AWS_KEY_BACKUP_CREATE} -e ELOPHANT_AWS_SECRET_BACKUP_CREATE=${ELOPHANT_AWS_SECRET_BACKUP_CREATE} -e ELOPHANT_DB_BACKUP_FREQUENCY_DAYS=${ELOPHANT_DB_BACKUP_FREQUENCY_DAYS} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} -e CONSUL_CLIENT_ADDRESS=elophant_consul_client:8500 -e CONSUL_SERVICE_ADDRESS=${DATABASE_NAME} -e CONSUL_SERVICE_PORT=5432 -e CONSUL_SERVICE_NAME=elophant-db --name ${DATABASE_NAME} ${REPOSITORY}/${DB_IMAGE}:latest
      sleep 10
    else
      echo "Dry Run: 'docker run -d --volumes-from ${VOLUME_CONTAINER_NAME} --net=${ELOPHANT_NETWORK} -p 5432:5432 -e ELOPHANT_AWS_KEY_BACKUP_CREATE=${ELOPHANT_AWS_KEY_BACKUP_CREATE} -e ELOPHANT_AWS_SECRET_BACKUP_CREATE=${ELOPHANT_AWS_SECRET_BACKUP_CREATE} -e ELOPHANT_DB_BACKUP_FREQUENCY_DAYS=${ELOPHANT_DB_BACKUP_FREQUENCY_DAYS} -e POSTGRES_PASSWORD=${ELOPHANT_ADMIN_PASSWORD} -e CONSUL_CLIENT_ADDRESS=elophant_consul_client:8500 -e CONSUL_SERVICE_ADDRESS=${DATABASE_NAME} -e CONSUL_SERVICE_PORT=5432 -e CONSUL_SERVICE_NAME=elophant-db --name ${DATABASE_NAME} ${REPOSITORY}/${DB_IMAGE}:latest'"
    fi
  fi
fi



### HA PROXY Servers
if [ ${INCLUDE_LOAD_BALANCERS} = true ]; then
  echo "Deploying Load Balancers..."

  echo "Pulling latest load balancer image..."
  if [ ${DRY_RUN} = false ]; then
    docker pull ${REPOSITORY}/${HAPROXY_IMAGE}
  else
    echo "Dry Run: 'docker pull ${REPOSITORY}/${HAPROXY_IMAGE}'"
  fi

  echo "Cleaning up any existing containers..."
  if docker ps -a | grep -q "elophant_haproxy"; then
    if [ ${DRY_RUN} = false ]; then
      docker rm -f $(docker ps -a | grep "${HAPROXY_IMAGE}" | cut -d ' ' -f1)
    else
      echo "Dry Run: 'docker rm -f docker rm -f $(docker ps -a | grep \"${HAPROXY_IMAGE}\" | cut -d ' ' -f1)'"
    fi
  else
    echo "No existing load balancers to remove"
  fi

  echo "Starting new load balancer containers..."
  if [ ${DRY_RUN} = false ]; then
    for (( i=1; i<=${NUM_LOAD_BALANCERS}; i++ )); do
      docker run -d --net=${ELOPHANT_NETWORK} -p 8${i}:80 -e CONSUL_CLIENT_ADDRESS=elophant_consul_client:8500 -e CONSUL_SERVICE_ADDRESS=elophant_haproxy_${i} -e CONSUL_SERVICE_PORT=8${i} -e CONSUL_SERVICE_NAME=elophant-haproxy -e HEALTH_CHECK_ADDRESS=http://elophant_haproxy_${i}:11911 --name elophant_haproxy_${i} ${REPOSITORY}/${HAPROXY_IMAGE}:latest
    done
  else
    for (( i=1; i<=${NUM_LOAD_BALANCERS}; i++ )); do
      echo "Dry Run: 'docker run -d --net=${ELOPHANT_NETWORK} -p 8${i}:80 -e CONSUL_CLIENT_ADDRESS=elophant_consul_client:8500 -e CONSUL_SERVICE_ADDRESS=elophant_haproxy_${i} -e CONSUL_SERVICE_PORT=8${i} -e CONSUL_SERVICE_NAME=elophant-haproxy -e HEALTH_CHECK_ADDRESS=http://elophant_haproxy_${i}:11911 --name elophant_haproxy_${i} ${REPOSITORY}/${HAPROXY_IMAGE}:latest'"
    done
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
      docker run -d --net=${ELOPHANT_NETWORK} -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} -e ELOPHANT_SECRET=${ELOPHANT_SECRET} -e ELOPHANT_DATABASE=${ELOPHANT_DATABASE} -e CONSUL_CLIENT_ADDRESS=elophant_consul_client:8500 -e CONSUL_SERVICE_ADDRESS=${SERVER_NAME_PREFIX}_${i} -e CONSUL_SERVICE_PORT=9000 -e CONSUL_SERVICE_NAME=elophant-web -e HEALTH_CHECK_ADDRESS=http://elophant_web_${i}:9000/amiup --name ${SERVER_NAME_PREFIX}_${i} ${REPOSITORY}/${WEB_IMAGE}:latest
      if [ ${i} -eq 1 ]; then
        sleep 5 # Try to avoid case where 2 web servers simultaneously apply evolutions
      fi
    done
  else
    for (( i=1; i<=${NUM_SERVERS}; i++ )); do
      echo "Dry Run: 'docker run -d --net=${ELOPHANT_NETWORK} -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} -e ELOPHANT_SECRET=${ELOPHANT_SECRET} -e CONSUL_CLIENT_ADDRESS=elophant_consul_client:8500 -e CONSUL_SERVICE_ADDRESS=${SERVER_NAME_PREFIX}_${i} -e CONSUL_SERVICE_PORT=9000 -e CONSUL_SERVICE_NAME=elophant-web -e HEALTH_CHECK_ADDRESS=http://elophant_web_${i}:9000/amiup --name ${SERVER_NAME_PREFIX}_${i} ${REPOSITORY}/${WEB_IMAGE}:latest'"
    done
  fi
fi
