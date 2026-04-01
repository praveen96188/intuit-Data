#!/bin/bash


read -p "Enter  gg hub " hub
if [ 'psp501' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 152430470825 -p id_rsa.pub -d ~/.ssh/ 
       ssh -t  ggpsppf501.sbg-psp-ppd.a.intuit.com -J ec2-52-38-34-141.us-west-2.compute.amazonaws.com "sudo su - oracle"   
elif [ 'ibob' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 152430470825 -p id_rsa.pub -d ~/.ssh/
       ssh -t ggpspibob1.sbg-psp-ppd.a.intuit.com  -J ec2-52-38-34-141.us-west-2.compute.amazonaws.com "sudo su - oracle"    
elif [ 'o2p1' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa
       eiamCli getAWSTempSSHCert -a 152430470825 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t ggppspo2p1.sbg-psp-ppd.a.intuit.com  -J ec2-52-38-34-141.us-west-2.compute.amazonaws.com "sudo su - oracle";
elif [ 'o2p2' = "$hub" ]; then
       ssh-add -D  
       ssh-add ~/.ssh/id_rsa 
       eiamCli getAWSTempSSHCert -a 152430470825 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t ggppspo2p2.sbg-psp-ppd.a.intuit.com -J ec2-52-38-34-141.us-west-2.compute.amazonaws.com "sudo su - oracle";
elif [ 'eastib' = "$hub" ]; then
       ssh-add -D
       ssh-add ~/.ssh/id_rsa
       eiamCli getAWSTempSSHCert -a 152430470825 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t  ip-10-121-87-180.us-east-2.compute.internal  -J ec2-3-133-253-70.us-east-2.compute.amazonaws.com  "sudo su - oracle";
elif [ 'eastue' = "$hub" ]; then
       ssh-add -D
       ssh-add ~/.ssh/id_rsa
       eiamCli getAWSTempSSHCert -a 152430470825 -p id_rsa.pub -d ~/.ssh/ ;
       ssh -t  ggpspe2eue.sbg-psp-ppd.a.intuit.com -J ec2-3-133-253-70.us-east-2.compute.amazonaws.com "sudo su - oracle";
else
    echo "Invalid: $hub"
fi


ssh -t  -J ec2-52-38-34-141.us-west-2.compute.amazonaws.com "sudo su - oracle";




       ssh-add -D
       ssh-add ~/.ssh/id_rsa
       eiamCli getAWSTempSSHCert -a 893547637742 -p olympus_rsa_psp_prd -d ~/.ssh/ ;
       ssh -t  psp-ppd-db-admin-ue2.vpc.internal -J  "sudo su - oracle";



export M2_HOME="/Users/pnarlagalla1/Downloads/apache-maven-3.9.9"
PATH="${M2_HOME}/bin:${PATH}"
export PATH


psp-ppd-admin-uw2

psp-ppd-admin-ue2



#!/bin/bash

read -p "Enter  gg hub " hub
echo "ppd-adm-uw2"
echo "ppd-adm-ue2"
echo "prd-adm-uw2"
echo "prd-adm-ue2"

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


