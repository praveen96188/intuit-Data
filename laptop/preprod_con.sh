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
