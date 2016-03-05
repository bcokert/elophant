# Elophant
Elophant is a service that manages elo rankings across multiple games and players.

## Table of Contents
1. [Description](#description)
2. [Client Documentation](#client-documentation)
3. [Server Documentation](#server-documentation)

## Description

### Elo Rankings
Elo rankings are estimates of the skill of each player in a 2 player game.
For every game, a score is determined between 0 and 1 - usually 1 if the first player won, 0 if they lost, and 0.5 for a draw.
That score is used to update the rating of each player - the higher the score, the more the player goes up.
Elo rankings are measured in points, and points are always taken from one player and given to another.

### Updating a Ranking
If the first player gets 1 point, the second gets 0. If the first gets 0, the second gets 1.
If the first player gets 0.7, then there's 0.3 left for player two. The sum of the scores is always equal to 1.
To simplify things, only the score of the first player is sent - the second players score is just 1 minus that.

Each player has an existing rating, and that can be used to calculate their expected score.
This is the score that elo predicts would occur on average, assuming their rating is correct.

The expected score for player A (the first player) is calculated via:
```scala
ExpectedA = 1 / (1 + Math.pow(10, (ratingB - ratingA)/curveScalingFactor))
```
Where curveScalingFactor is just a factor used for the logical curve, and is usually set to 400.

And just like the real score, the expected score for the second player is just 1 - ExpectedA.
Once you have the expected score, you can calculate the change in playerA's rating, or the delta rating:
```scala
Math.round(kFactor * (score - getExpectedScore(ratingA, ratingB))).toInt
```
Where the kFactor is another scaling constant, and helps control how dynamic your delta's are - if you half the kFactor, you half the base value of a win.
Usually you pick the kFactor depending on the frequency of games and the accuracy of your ratings - pro tournaments will use a smaler value than amateur ones.

Finally, you just add the delta value to playerA's original rating, to get his new value.
Similarly, you subtract the delta value from playerB's rating.

## Client Documentation

### Access Control
All requests must provide an Auth-Token associated with their app.
This should be provided in the http header "Auth-Token":
```bash
curl -H "Auth-Token: 9afhds923haksdhkfjasdf89h23hjkashdf" -H "Content-Type: application/json" -X "POST" -d {"name": "foosball", "description": "The one with the balls"} localhost:9000/gameType
```

Each app has a set of access permissions associated with their auth token.
Every permission has a Type, and a Level.

The levels are NONE, READ, CREATE, UPDATE, and DELETE.
Those to the right imply those to the left, so UPDATE also grants CREATE and READ.
* NONE is the default permission, when none is otherwise found, and prevents most operations.
* READ allows GET operations with no side effects.
* CREATE allows operations that create new data but do not affect other data.
* UPDATE allows operations that operate on existing data.
* DELETE allows operations that can remove data.

For every endpoint group (Player, GameType, Rating, etc.) there is a corresponding Permission Type.
The minimum required Permission Level is listed alongside each endpoint.

### Redirects
Request URI's should not have a trailing slash. Any that do will be redirected to the version that does not have a slash.
Be sure to configure your client to follow redirects if you are unable to enforce this.

### Endpoints
All endpoints specify the url, query params, and if applicable, a valid request body.
The response body shown is always that of a successful response.

In the event of an error, all requests return an error response of the form:
```
{
    success: false,
    errorReasons: ["Issue number 1", "Issue number 2"]
}
```

#### Player

##### GET /player
Retrieve all Players
* Permission Type: PLAYER
* Permission Level: READ

Response Body:
```
[{
    id: 422,
    firstName: "Bob",
    lastName: "Dylan",
    email: "bob.dylan@website.com"
}, {
    ...
}]
```

##### GET /player/{id: Int}
Retrieve the player with the given id
* Permission Type: PLAYER
* Permission Level: READ

Response Body:
```
{
    id: 422,
    firstName: "Bob",
    lastName: "Dylan",
    email: "bob.dylan@website.com"
}
```

##### POST /player [json]
Create the player with the given data
* Permission Type: PLAYER
* Permission Level: CREATE

Request Body:
```
{
    firstName: "bob",
    lastName: "dylan",
    email: "test@test.com"
}
```

Response Body:
```
{
    id: 422,
    firstName: "bob",
    lastName: "dylan",
    email: "test@test.com"
}
```

##### DELETE /player/{id: Int}
Delete the player with the given id
* Permission Type: PLAYER
* Permission Level: DELETE

Response Body:
```
{
    success: true
}
```

#### Game Type

##### GET /gameType
Retrieve All GameTypes
* Permission Type: GAME_TYPE
* Permission Level: READ

Response Body:
```
[{
    id: 3,
    name: "Foosball",
    description: "The one with the balls"
}, {
    ...
}]
```

##### GET /gameType/{id: Int}
Retrieve the gameType with the given id
* Permission Type: GAME_TYPE
* Permission Level: READ

Response Body:
```
{
    id: 4,
    name: "Foosball",
    description: "The one with the balls"
}
```

##### POST /gameType [json]
Create the gameType with the given data
* Permission Type: GAME_TYPE
* Permission Level: CREATE

Request Body:
```
{
    name: "Funball",
    description: "The new one with the Fun balls!"
}
```

Response Body:
```
{
    id: 5,
    name: "Funball",
    description: "The new one with the Fun balls!"
}
```

##### DELETE /gameType/{id: Int}
Delete the gameType with the given id
* Permission Type: GAME_TYPE
* Permission Level: DELETE

Response Body:
```
{
    success: true
}
```

#### Rating

##### GET /eloRating?playerId=2&leagueId=4&gameTypeId=5
Retrieve All GameTypes
* Permission Type: RATING
* Permission Level: READ

Each query param is optional, and will further reduce the set of ratings returned.

Response Body:
```
[{
    eloRating: 1155,
    playerId: 66,
    gameTypeId: 6
}, {
    ...
}]
```

##### POST /gameResult [json]
Post the result of a game, which will update EloRatings
* Permission Type: RATING
* Permission Level: UPDATE

Score should be between 0 and 1.

Request Body:
```
{
    player1Id: 52,
    player2Id: 21,
    score: 1,
    gameTypeId: 6
}
```

Response Body:
```
[{
    eloRating: 1192,
    playerId: 52,
    gameTypeId: 6
}, {
    eloRating: 1177,
    playerId: 21,
    gameTypeId: 6
}]
```

## Server Documentation

### Developers

#### Setup
Regardless of what you're doing, the following setup is required for developing

##### Local Environment Variables
You need to setup local environment variables for use when building images, and running local instances.
Just run resources/client-scripts/setup.sh and it will walk you through the setup

##### Install Local docker host
To build and deploy, you need to run docker locally. Linux environments can be hosts themselves, and non-linux environments use a VM managed by a tool called docker-machine
* OSX - http://docs.docker.com/mac/step_one/
  * run docker-quickstart-terminal (first time setup)
  * run 'eval "$(docker-machine env default)"' (enable using docker directly from terminal)
* linux - curl -sSL https://get.docker.com/ | sh
  * Follow the steps it prints out to create a docker group and add your dev user to it
  
##### Forward Local Ports (OSX only)
On OSX, your containers run inside a VM. You need to forward ports to the containers inside your VM in order to connect from the outside (eg: psql into a local database).
* Open Virtual Box
* Right click default, go to Settings, then Network, then click "Port Forwarding" on the NAT adapter
* Add a new rule, leave the ip's blank, and set the internal and external ports to 5432 (for the database)
* Repeat for any other ports you want to open (you don't need to open 9000, since you'll be running the server via activator, not docker)

#### Running a Local Server
```bash
> ./activator run        # Run in dev mode, with nice handling of exceptions
> ./activator testProd   # Run in prod mode, which acts exactly like production, but uses the local config file
> ./activator stage      # Compile a deploy artifact that you can use to further test running in production (eg: creates log folder structure)
```

#### Running a Local Database
You can also setup a local version of the database. Just follow the Ops section for deploying a database, but on your local machine.
Then modify your application.conf to point to the local database:
```
db.url: "jdbc:postgresql://localhost:5432/elophant"
```

### Ops

#### Setup an Environment
The setup for Dev and Prod (and other) environments is the same, save for different environment variable values.

##### Setup Environment Variables
For the local environment (devs only) see their relevant Environment variable section, which uses a script
For all other environments, the following variables need to be set:
* Set ELOPHANT_USER_PASSWORD - password for regular access
* Set ELOPHANT_ADMIN_PASSWORD - password for admin access (only used to init database)
* Set ELOPHANT_SECRET - the play secret, should be generated via 'activator playGenerateSecret'

##### Install Docker
* Install docker 1.9.x via 'curl -sSL https://get.docker.com/ | sh'
* It should prompt you to setup groups and users. You should add any users that can manage images to the docker group. 

##### Configure ports
* Ensure port 9000 of the docker host is forwarded externally (this is the default port for the server)
* Ensure port 5432 of the docker host is forwarded externally (this is used by psql for database maintenance, and for cross environment database connections (eg your laptop to a different hosts database))
* All other ports are automatically configured between docker containers within the host

##### Install deployment scripts
* Copy all of the scripts in resources/server-scripts to the docker host, somewhere like /usr/local/bin. Ensure the docker group can run them.

#### Maintenance

##### Deploy Database
* If the data volume is already setup, just run /usr/local/bin/deploy-database.sh
* If not, it will prompt you to set it up, and provide the necessary instructions. This is a short manual process.

##### Deploy Server
* Ensure a database is running first
* Run /usr/local/bin/deploy-server.sh

##### Manage a Database
You can connect to the database using any tool. Usually it's easiest to use the basic postgres client:
```
> psql postgresql://0.0.0.0:5432/elophant -U elophantuser
```
Any changes made here will be seen in the next request by the server, since you are essential just another client

##### View server logs
All server containers print their logs to stdout, so you can view them with:
```
> docker logs container_name
```
All servers also output rotated logs in the run directory:
* /usr/local/lib/elophant-web/web/logs
Which you can find by running
```
> docker inspect elophant_web_1
```

##### Debug a servers configuration
You can get most info by inspecting a container, and complete control by creating a copied instance of the container
Connect to the docker host, then:
```
> docker inspect name_of_container | grep -A 8 \"Volumes\"
```
This will also tell you where to find the database volume and the logs volume.
To run a copied container, look at the last line of the deploy script for the server in /usr/local/bin.
Replace '-d' with '-ti --rm', and add '/bin/bash' to the end.
```
> docker run -ti --rm --link elophant_db_1:database -e ELOPHANT_USER_PASSWORD=${ELOPHANT_USER_PASSWORD} -e ELOPHANT_SECRET=${ELOPHANT_SECRET} bcokert/elophant-web:latest /bin/bash
```

#### Undo killing the database
Oops! I killed the database server! Does that mean everything is broken?

No! Elophant uses data containers to ensure the database is never accidentally removed. To actually remove it, you have to
* Destroy all containers referring to it
* Destroy the data container (never happens in a script, so would be manual)
* Pass in extra options to the destroy method telling it to remove the associated data (also would have to be manual, and docker would yell at you)

To restore the database if you've removed the servers (even the data container) just run /usr/local/bin/deploy-database.sh on the docker host
