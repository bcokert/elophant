ROOTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/../..
RESOURCEDIR=$ROOTDIR/resources
BUILDDIR=$RESOURCEDIR/build
OUTPUTDIR="$ROOTDIR/release-tmp"

ERRORTXT="\033[1m\033[41m\033[97mERROR:\033[0m"

echo "Cleaning any old local release files..."
rm -rf $OUTPUTDIR
mkdir $OUTPUTDIR

echo "Building project..."
$ROOTDIR/activator clean compile stage

echo "Preparing build artifacts for docker imaging..."
cp -r $ROOTDIR/target/universal/stage $OUTPUTDIR
cp $BUILDDIR/Dockerfile $OUTPUTDIR

echo "Checking that docker VM is available..."
if ! docker-machine ls | grep -q default; then
  echo -e "$ERRORTXT Docker VM is not started. Please run 'docker-machine create --driver virtualbox default'"
fi

echo "Connecting to Docker VM..."
eval "$(docker-machine env default)"

echo "Building Docker Image..."
VERSIONEDBUILD=v$(grep -o 'version\s*:=.\+' $ROOTDIR/build.sbt | grep -o '\d.\d.\d')
LATESTBUILD=latest
docker build -t bcokert/elophant-server:$VERSIONEDBUILD $OUTPUTDIR
docker build -t bcokert/elophant-server:$LATESTBUILD $OUTPUTDIR
docker images | grep elophant

echo "Cleaning up bulid artifacts..."
rm -rf $OUTPUTDIR

echo "Checking that you are logged in to docker hub..."
if ! docker info | grep -q Username; then
  echo "You must login to push to docker Hub (you only need to do this once):"
  docker login
else
  echo "Succesfully logged in!"
fi

echo "Pushing docker images..."
docker push bcokert/elophant-server
