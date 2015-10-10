LATESTBUILD=latest

ERRORTXT="\033[1m\033[41m\033[97mERROR:\033[0m"

echo "Checking that you are logged in to docker hub..."
if ! docker info | grep -q Username; then
  echo "You must login to push to docker Hub (you only need to do this once):"
  docker login
else
  echo "Succesfully logged in!"
fi

echo "Pulling latest server image..."
docker pull bcokert/elophant-server:$LATESTBUILD

echo "Starting a new server container..."
docker run -d -p 9000:9000 bcokert/elophant-server:$LATESTBUILD /usr/local/lib/elophant-server/bin/elophant
