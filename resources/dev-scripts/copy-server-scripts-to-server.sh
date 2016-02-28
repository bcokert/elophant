#!/usr/bin/env bash

ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/../..
SERVER_SCRIPTS_DIR=${ROOT_DIR}/resources/server-scripts

echo "Docker host full address: $1"
echo "Docker host secret key file: $2"

if [ -z "$1" ]; then echo "Missing docker host address as first argument (format: user@123.432.41.11)"; exit 1; fi
if [ -z "$2" ]; then echo "Missing secret key file (format: /path/to/key.pem)";  exit 1; fi

echo "Copying files over to server..."
sftp -i $2 $1 << EOF

cd /usr/local/bin/elophant
put ${SERVER_SCRIPTS_DIR}/*.sh

EOF
