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

if ! grep -q ELOPHANT_DB_BACKUP_FREQUENCY_DAYS ~/.bash_profile; then
  read -p "What is the frequency (in days) that you want to backup the datbase to s3? (0 for never): " BACKUP_FREQUENCY
  echo
  echo "export ELOPHANT_DB_BACKUP_FREQUENCY_DAYS=${BACKUP_FREQUENCY}" >> ~/.bash_profile
fi

if ((ELOPHANT_DB_BACKUP_FREQUENCY_DAYS > 0)) || ((BACKUP_FREQUENCY > 0)) ; then
  if ! grep -q ELOPHANT_AWS_SECRET_BACKUP_CREATE ~/.bash_profile; then
    read -p "What is aws s3 secret for the create user: " AWS_SECRET_BACKUP_CREATE
    echo
    echo "export ELOPHANT_AWS_SECRET_BACKUP_CREATE=${AWS_SECRET_BACKUP_CREATE}" >> ~/.bash_profile
  fi

  if ! grep -q ELOPHANT_AWS_KEY_BACKUP_CREATE ~/.bash_profile; then
    read -p "What is aws s3 key for the create user: " AWS_KEY_BACKUP_CREATE
    echo
    echo "export ELOPHANT_AWS_KEY_BACKUP_CREATE=${AWS_KEY_BACKUP_CREATE}" >> ~/.bash_profile
  fi
fi

echo "please run 'source ~/.bash_profile'"
