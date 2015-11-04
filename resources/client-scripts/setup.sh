#!/usr/bin/env bash
ELOPHANT_GATEWAY=174.1.53.209
ELOPHANT_DB_PORT=19213
ELOPHANT_ENV=local

ERROR_TXT="\033[1m\033[41m\033[97mERROR:\033[0m"

read -p "Do you want the config variables to be added to your bash profile? (y/n): " SHOULD_SAVE
echo
if [ ${SHOULD_SAVE} = "y" ]; then
  if ! grep -q ELOPHANT_ENV ~/.bash_profile; then
    echo "export ELOPHANT_ENV=${ELOPHANT_ENV}" >> ~/.bash_profile
  fi

  if ! grep -q ELOPHANT_GATEWAY ~/.bash_profile; then
    echo "export ELOPHANT_GATEWAY=${ELOPHANT_GATEWAY}" >> ~/.bash_profile
  fi

  if ! grep -q ELOPHANT_DB_PORT ~/.bash_profile; then
    echo "export ELOPHANT_DB_PORT=${ELOPHANT_DB_PORT}" >> ~/.bash_profile
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

  echo "please run 'source ~/.bash_profile'"
else
  echo "Please manually set these environment variables:"
  echo "ELOPHANT_ENV=${ELOPHANT_ENV}"
  echo "ELOPHANT_GATEWAY=${ELOPHANT_GATEWAY}"
  echo "ELOPHANT_USER_PASSWORD=${DB_PASS}"
  echo "ELOPHANT_ADMIN_PASSWORD=${ADMIN_PASS}"
fi
