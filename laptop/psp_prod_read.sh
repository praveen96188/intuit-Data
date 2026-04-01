#!/bin/bash

echo "prd-adm-uw2"
echo "prd-adm-ue2"

read -p "Enter Admin hub " hub
#read -p "Enter  CR Number " CR

if [ 'prd-adm-uw2' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 893547637742 -p id_rsa.pub -d ~/.ssh/   ;
       ssh -t  pnarlagalla1_ro@psp-prod-db-admin-uw2.vpc.internal -J pnarlagalla1_ro@ec2-52-38-226-158.us-west-2.compute.amazonaws.com ;
   
elif [ 'prd-adm-ue2' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 893547637742 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t pnarlagalla1_ro@psp-prod-db-admin-ue2.vpc.internal  -J pnarlagalla1_ro@ec2-3-130-125-83.us-east-2.compute.amazonaws.com ;
else
    echo "Invalid: $hub"
fi



