#!/bin/bash

echo "ppd-adm-uw2"
echo "ppd-adm-ue2"
echo "prd-adm-uw2"
echo "prd-adm-ue2"

read -p "Enter  gg hub " hub
if [ 'ppd-adm-uw2' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 152430470825 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t  psp-ppd-db-admin-uw2.vpc.internal -J ec2-52-38-34-141.us-west-2.compute.amazonaws.com "sudo su - ec2-user";
   
elif [ 'ppd-adm-ue2' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 152430470825 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t psp-ppd-db-admin-ue2.vpc.internal  -J ec2-3-133-253-70.us-east-2.compute.amazonaws.com "sudo su - ec2-user";

elif [ 'prd-adm-uw2' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 893547637742 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t  psp-prod-db-admin-uw2.vpc.internal -J ec2-52-38-226-158.us-west-2.compute.amazonaws.com "sudo su - ec2-user";
   
elif [ 'prd-adm-ue2' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 893547637742 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t psp-prod-db-admin-ue2.vpc.internal  -J ec2-3-130-125-83.us-east-2.compute.amazonaws.com "sudo su - ec2-user";
else
    echo "Invalid: $hub"
fi
