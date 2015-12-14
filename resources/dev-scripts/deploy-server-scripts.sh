#!/usr/bin/env bash

ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/../..
SERVER_SCRIPTS_DIR=${ROOT_DIR}/resources/server-scripts

echo "Docker host: $1"
echo "SFTP Port: $2"
echo "Docker Group Id: $3"

if [ -z "$1" ]; then echo "Missing docker host address as first argument (format: user@123.432.41.11)"; exit 1; fi
if [ -z "$2" ]; then echo "Missing SFTP port as second argument (format: 9911)";  exit 1; fi
if [ -z "$3" ]; then echo "Missing Docker group id, which is needed to set permissions. Retrieve remotely via 'getent group docker | cut -d: -f3' (format: 991)";  exit 1; fi

echo "Copying files over to server..."
sftp -P $2 $1 << EOF

cd /usr/local/bin
put ${SERVER_SCRIPTS_DIR}/*.sh
chgrp $3 *.sh

EOF
