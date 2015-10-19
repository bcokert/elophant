# Elophant


## Architecture
There are 3 main environments that Elophant is setup for. Throught the documentation they are:
Local: The local environment where you typically develop elophant
Dev: The dev environment that simulates production, and homes the database that the local environment uses
Production: The production environment

All environments make use of a docker host, which runs the containers that represent every other piece of the architecture (except possible gateways).
The environment machine runs docker (making it a docker host) or runs a VM that runs docker (making the VM the docker host).
All code is build and packed into docker images, which are pushed to docker hub. These are then pulled and run on a docker host.

TODO: Provide a sketch of the setup on macs and on deploy hosts to make it easier to understand
TODO: Add machines and ports to sketch


## Installation, Setup, and Deployment
This section deals with setting up each environment, as well as deploying to each one

### Setting up a local environment
Locally you can run an elophant server, and by default it will connect to the dev database.
#### Setup Environment Variables
* Run resources/client-scripts/setup.sh
* If using a gateway between the internet and your dev docker host, ensure it is forwarding port 19213 external to 5432 of the docker host
* If not, manually change environment variable ELOPHANT_DB_PORT to 5432, and ELOPHANT_GATEWAY to the address of your docker host
#### Install local docker host
To build and deploy, you need to run docker locally. Linux environments can be hosts themselves, and non-linux environments use a VM managed by a tool called docker-machine
* OSX - http://docs.docker.com/mac/step_one/
** After installation, run resources/client-scripts/build-and-release-server.sh, which will guide you through setting up docker-machine
** To use docker, either use the docker-quickstart-terminal, or run '$(docker-machine env default)' in your terminal, then use 'docker' as per usual
* linux - curl -sSL https://get.docker.com/ | sh
** Follow the steps it prints out to create a docker group and add your dev user to it

### Setting up a dev environment
A dev environment (or any environment) for elophant is simply a docker host. You can likely configure the host however you want, but the following requirements must be met:
#### Basics
* You need at least 1 account that has root access or sufficient sudo access
* You'll likely need to setup an ssh server, user accounts, and a docker group
* Install docker 1.9.x via 'curl -sSL https://get.docker.com/ | sh'
#### Configure ports
* Ensure port 9000 of the docker host is forwarded externally (this is the default port for the server)
* Ensure port 5432 of the docker host is forwarded externally (this is used by the local server when it connects to the dev database)
* All other ports are automatically configured between docker containers within the host
#### Install deployment scripts
* Copy all of the scripts in resources/server-scripts to the docker host, somewhere like /usr/local/bin. Ensure the docker group can run them
#### Setup Environment variables
* Set ELOPHANT_USER_PASSWORD, which should be the same password as the user used for the database (see the database setup section below)
* Set ELOPHANT_ENV=dev, which allows the server to choose configuration based on the environment (local, dev, production)
* Set ELOPHANT_SECRET, which is the scala play secret. You can create one per environment via './activator playGenerateSecret'
#### Setup database for the first time
* Normally you'll just deploy the service or db service, but the first time you setup you also need some additional setup
* Locally, run resources/client-scripts/build-and-release-db.sh (or use an existing db image)
* On the docker host, run /usr/local/bin/deploy-database.sh
* On the docker host, run /usr/local/bin/first-setup-database.sh, and follow the instructions in the container it brings up
#### Setup server
* Once the database is setup, the server is trivial to run
* Locally, run resources/client-scripts/build-and-release-server.sh (or use an existing server image)
* On the docker host, run /usr/local/bin/deploy-server.sh
* Test it by going to your exposed address on port 9000

### Setting up a production environment
Setting up a production environment is exactly the same as setting up the dev environment, just with different values.
Ideally you'll also change the passwords, secrets, and user accounts used. The only essential different step is:
#### Setup Environment variables
* Set ELOPHANT_ENV=production


## Development

### Build and run the server
By default the dev server will serve to localhost:9000
```
> ./activator clean compile run   # Runs the server locally in the current process
> ./activator stage   # Creates an artifact in ./target/universal/stage/bin that you can run to simulate deploying the server locally
```

## Maintenance

### Re-start/Re-deploy a server
Typically each server has an associated client-script to build and release, and another server-script to deploy
#### Web Server
* Locally, run resources/client-scripts/build-and-release-server.sh (or use an existing server image)
* On the docker host, run /usr/local/bin/deploy-server.sh
#### Database Server
* Locally, run resources/client-scripts/build-and-release-db.sh (or use an existing db image)
* On the docker host, run /usr/local/bin/deploy-database.sh

### View the database
You view the database by running an interactive database service (the service is different than the data itself)
Connect to the docker host, then:
```
> docker run -ti --rm --volumes-from elophant_data -e POSTGRES_PASSWORD=<ADMIN_PASSWORD_FROM_SETTING_UP_DATABASE> bcokert/elophant-db:latest /bin/bash
> # In the container:
> su postgres
> /var/lib/postgres/9.4/bin/postgres &
> psql
> \connect elophant
```
You are now connected to the psql interface, and can run standard postgres commands to query/update the database

### View server logs
Since all servers are just containers that run a single process, they can print their logs to stdout
TODO: Provide data containers to expose other system logs
Connect to the docker host, then:
```
> docker logs name_of_container
```

### Debug a servers configuration
You can get most info by inspecting a container, and complete control by creating a copied instance of the container
Connect to the docker host, then:
```
> docker inspect name_of_container
```
To run a copied container, look at the last line of the deploy script for the server in /usr/local/bin.
TODO: Make this easier via clone scripts
Fill in the relevant environment variables, and replace '-d' with '-ti --rm', and add '/bin/bash' to the end.
The arguments to -e are variables defined on the docker host, so no need to replace those
For example:
```
> docker run -ti --rm --link elophant_db_1:database -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} -e ELOPHANT_SECRET=${ELOPHANT_SECRET} -e ELOPHANT_ENV=${ELOPHANT_ENV} bcokert/elophant-server:latest /bin/bash
```

### Undo killing the database
Oops! I killed the database server! Does that mean everything is broken?

No! Elophant uses data containers to ensure the database is never accidentally removed. To actually remove it, you have to
* Destroy all containers referring to it
* Destroy the data container (never happens in a script, so would be manual)
* Pass in extra options to the destroy method telling it to remove the associated data (also would have to be manual, and docker would yell at you)

To restore the database if you've removed the servers (even the data container) just run /usr/local/bin/deploy-database.sh on the docker host

If you also passed in the extra option, then essentially you've just done a sudo rm -rf /. Congratulations!

### Backup the database
You can backup the database by just copying and archiving the data volume. The location of the volume can be found with docker inspect
TODO: Make this easier by providing a script
TODO: On the docker hosts, run a cron script that does this periodically

## Usage
For the most part using Elophant just means interacting with the rest interface. To work with the database, see the maintenance section and 'Setting up a dev environment' section.
TODO: Provide a quickstart tutorial for the rest interface once it's made
