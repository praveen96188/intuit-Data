#!/bin/sh

# Install colima
source $RC_FILE

if [ `echo $DOCKER_HOST | grep 'sock' | wc -l` != 0 ]; then
      echo "DOCKER_HOST is set in environment variables. Please remove it and other docker configuration and try again..."
fi

if [ `docker context show | grep 'default' | wc -l` = "1" ]; then
    echo "Docker Desktop is installed. Please uninstall and remove DOCKER configuration before running the script..."
    exit 1
fi

if [ ! -d "/opt/homebrew/bin/colima" ]
then
  echo "Installing colima & docker..."
  brew install colima docker
  brew install docker
fi

if [ `which docker | grep '/opt/homebrew/bin/docker' | wc -l` != "1" ]; then
  echo "Docker not installed properly. Please re run the script..."
  exit 1
fi

echo "Initializing colima machine. Please say YES for colima deletion."
colima delete
colima start -p colima_psp --arch x86_64 --cpu 4 --memory 6 --disk 60
docker context use colima-colima_psp

if [ `colima list | grep 'Running' | grep 'colima_psp' | wc -l` != "1" ]; then
  echo "Colima is not running. Please re run the script..."
  exit 1
fi

if [ `docker context show | grep 'colima' | wc -l` != "1" ]; then
    echo "Docker not running under colima context. Please re run the script..."
    exit 1
fi

if [ `docker images | grep -o "docker.intuit.com/ecosystem/payroll/qbdt-payroll-service/service/psp" | wc -l` = "0" ]
then
  if [ -f $HOME/oracle_psp_image.tar ]
  then
    echo "Found saved image. Loading..."
    docker load -i $HOME/oracle_psp_image.tar
  fi
fi
# Pull the db image from the repository
if [ `docker images | grep -o "docker.intuit.com/ecosystem/payroll/qbdt-payroll-service/service/psp" | wc -l` = "0" ]
then
  echo "No pre-existing image found"
  docker pull docker.intuit.com/ecosystem/payroll/qbdt-payroll-service/service/psp:latest
fi

if [ -f $HOME/oracle_psp_image.tar ]
then
  docker save --format oci-archive -o $HOME/oracle_psp_image.tar docker.intuit.com/ecosystem/payroll/qbdt-payroll-service/service/psp
fi

if [ `docker ps | grep -i "oracle-psp" | grep -o "(healthy)" | wc -l` -ne "0" ] #if a healthy container already exists
then
  ./dev/deploy_local/add_secondary_user.sh
  exit
fi

# Stopping any unhealthy containers
if [ `docker ps| grep -i "oracle-psp"| wc -l` -ne "0" ]
then
  echo "Stopping oracle-psp"
  docker stop oracle-psp
fi

# Removing any stopped containers
if [ `docker ps -a| grep -i "oracle-psp"| wc -l` -ne "0" ]
then
  echo "Removing oracle-psp"
  docker rm oracle-psp
fi


echo "running oracle-psp"
docker run -d -it --name oracle-psp -p 1521:1521 docker.intuit.com/ecosystem/payroll/qbdt-payroll-service/service/psp:latest


# Waiting for the container to become healthy...
while [ `docker ps | grep "(unhealthy)" | grep -o "oracle-psp" | wc -l` -ne "0" ]
do
  sleep 30
  echo "Waiting for the container to become healthy..."
done

./dev/deploy_local/add_secondary_user.sh
exit