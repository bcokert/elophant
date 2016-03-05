#!/usr/bin/env bash

if ! grep -q ELOPHANT_DATABASE ~/.bash_profile; then
  read -p "What is the address (with port) of the database: " ELOPHANT_DATABASE
  echo
  echo "export ELOPHANT_DATABASE=${ELOPHANT_DATABASE}" >> ~/.bash_profile
fi

if ! grep -q ELOPHANT_SECRET ~/.bash_profile; then
  read -p "What is the secret token for the environment (can be any base64 string): " -s SECRET
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
  read -p "What is the name of the docker network for this environment: " NETWORK_NAME
  echo
  echo "export ELOPHANT_NETWORK=${NETWORK_NAME}" >> ~/.bash_profile
fi

echo "please run 'source ~/.bash_profile'"
