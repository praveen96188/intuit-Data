#!/bin/sh

# Install podman
source $RC_FILE
if [ ! -d "/usr/local/Cellar/podman" ]
then
  echo "Installing podman..."
  brew install podman
fi

if [ `podman machine list | wc -l` = "1" ] #if no vm exists
then
  echo "Initializing machine"
  podman machine init --cpus=4 --disk-size=60 --memory=6096
fi

if [ `podman machine list | grep -o "Currently running" | wc -l` = "0" ] #if no vm is currently running
then
  #TODO handle "Error can't delete..." using kill -9 `ps -ec | grep qemu | awk '{print $1}'`
  podman machine stop
  printf "y\n" | podman machine rm >> /dev/null 2>&1
  podman machine init --cpus=4 --disk-size=60 --memory=6096
  # initialize qemu with invalid options before machine start so that socks files are already there when podman tries to access them after macbook reboot.
  /usr/local/bin/qemu-system-x86_64 -machine q35,accel=hvf:tcg -cpu host -display none INVALID_OPTION >> /dev/null 2>&1 # discard all stdout and stderr logs since it is expected to fail with invalid options .
  podman machine start
fi

if [ `podman images | grep -o "docker.intuit.com/ecosystem/payroll/qbdt-payroll-service/service/psp" | wc -l` = "0" ]
then
  if [ -f $HOME/oracle_psp_image.tar ]
  then
    echo "Found saved image. Loading..."
    podman load -i $HOME/oracle_psp_image.tar
  fi
fi
# Pull the db image from the repository
if [ `podman images | grep -o "docker.intuit.com/ecosystem/payroll/qbdt-payroll-service/service/psp" | wc -l` = "0" ]
then
  echo "No pre-existing image found"
  podman pull docker.intuit.com/ecosystem/payroll/qbdt-payroll-service/service/psp:latest
fi

if [ -f $HOME/oracle_psp_image.tar ]
then
  podman save --format oci-archive -o $HOME/oracle_psp_image.tar docker.intuit.com/ecosystem/payroll/qbdt-payroll-service/service/psp
fi

if [ `podman ps | grep -i "oracle-psp" | grep -o "(healthy)" | wc -l` -ne "0" ] #if a healthy container already exists
then
  ./dev/deploy_local/add_secondary_user.sh
  exit
fi

# Stopping any unhealthy containers
if [ `podman ps| grep -i "oracle-psp"| wc -l` -ne "0" ]
then
  echo "Stopping oracle-psp"
  podman stop oracle-psp
fi

# Removing any stopped containers
if [ `podman ps -a| grep -i "oracle-psp"| wc -l` -ne "0" ]
then
  echo "Removing oracle-psp"
  podman rm oracle-psp
fi


echo "running oracle-psp"
podman run -d -it --name oracle-psp -p 1521:1521 docker.intuit.com/ecosystem/payroll/qbdt-payroll-service/service/psp:latest


# Waiting for the container to become healthy...
while [ `podman ps | grep "(unhealthy)" | grep -o "oracle-psp" | wc -l` -ne "0" ]
do
  sleep 30
  echo "Waiting for the container to become healthy..."
done

./dev/deploy_local/add_secondary_user.sh
exit
