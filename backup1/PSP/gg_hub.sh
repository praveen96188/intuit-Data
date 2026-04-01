#!/bin/bash

read -p "Enter  gg hub " hub
if [ 'ppd-adm-uw2' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 152430470825 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t  psp-ppd-db-admin-uw2.vpc.internal -J ec2-52-38-34-141.us-west-2.compute.amazonaws.com ;
   
elif [ 'ppd-adm-ue2' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 152430470825 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t psp-ppd-db-admin-ue2.vpc.internal  -J ec2-3-133-253-70.us-east-2.compute.amazonaws.com ;
else
    echo "Invalid: $hub"
fi
