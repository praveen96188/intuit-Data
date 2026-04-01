# configure database
echo "Call configDockerDB.sh to configure database"
/bin/bash /home/oracle/setup/configDockerDB.sh

echo "Call pspDB.sh to configure database"
/bin/bash /home/oracle/setup/pspDB.sh