#!/usr/bin/env bash
ELOPHANT_ENV=local

read -p "Do you want the config variables to be added to your bash profile? (y/n): " SHOULD_SAVE
echo
if [ ${SHOULD_SAVE} = "y" ]; then
  if ! grep -q ELOPHANT_ENV ~/.bash_profile; then
    echo "export ELOPHANT_ENV=${ELOPHANT_ENV}" >> ~/.bash_profile
  fi

  if ! grep -q ELOPHANT_GATEWAY ~/.bash_profile; then
    read -p "What is the dev ip address (if behind a gateway, enter that): " -s GATEWAY
    echo
    echo "export ELOPHANT_GATEWAY=${GATEWAY}" >> ~/.bash_profile
  fi

  if ! grep -q ELOPHANT_DB_PORT ~/.bash_profile; then
    read -p "What is the dev database port (if behind a gateway, enter that): " -s DB_PORT
    echo
    echo "export ELOPHANT_DB_PORT=${DB_PORT}" >> ~/.bash_profile
  fi

  if ! grep -q ELOPHANT_SECRET ~/.bash_profile; then
    read -p "What is the secret token for the local environment (can be any base64 string): " -s SECRET
    echo
    echo "export ELOPHANT_SECRET=${SECRET}" >> ~/.bash_profile
  fi

  if ! grep -q ELOPHANT_USER_PASSWORD ~/.bash_profile; then
    read -p "What is the password of the database user (username 'elophantuser'): " -s DB_PASS
    echo
    echo "export ELOPHANT_USER_PASSWORD=${DB_PASS}" >> ~/.bash_profile
  fi

  if ! grep -q ELOPHANT_ADMIN_PASSWORD ~/.bash_profile; then
    read -p "What is the password of the database admin (username 'postgres'): " -s ADMIN_PASS
    echo
    echo "export ELOPHANT_ADMIN_PASSWORD=${ADMIN_PASS}" >> ~/.bash_profile
  fi

  if ! grep -q ELOPHANT_NETWORK ~/.bash_profile; then
    read -p "What is the name of the docker network for the local environment: " -s NETWORK_NAME
    echo
    echo "export ELOPHANT_NETWORK=${NETWORK_NAME}" >> ~/.bash_profile
  fi

  echo "please run 'source ~/.bash_profile'"
else
  echo "Please manually set these environment variables:"
  echo "ELOPHANT_ENV=${ELOPHANT_ENV}"
  echo "ELOPHANT_GATEWAY=${ELOPHANT_GATEWAY}"
  echo "ELOPHANT_USER_PASSWORD=${DB_PASS}"
  echo "ELOPHANT_ADMIN_PASSWORD=${ADMIN_PASS}"
fi
