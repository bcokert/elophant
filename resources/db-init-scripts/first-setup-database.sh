#!/usr/bin/env bash

echo "Changing ownership of database files to postgres admin user..."
chown -R postgres:postgres /var/lib/postgresql

echo "Logging in as postgres admin user for setup..."
su postgres <<EOSU

echo "Allowing hosts to connect from any ip address near the host..."
echo "listen_addresses='*'" >> /var/lib/postgresql/data/postgresql.conf

echo "Starting the server to run some commands..."
/usr/lib/postgresql/9.4/bin/postgres &
sleep 5

echo "Creating the base user and database..."
psql -c "CREATE USER elophantuser WITH PASSWORD '${ELOPHANT_USER_PASSWORD}'"
psql -c "CREATE DATABASE elophant OWNER elophantuser"

EOSU

echo "Finished database initial setup!"
