ssh-add -D  
ssh-add ~/.ssh/id_rsa
eiamCli aws_ssh -a 893547637742 -p id_rsa.pub -d ~/.ssh/ 
ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:18911:psp-prod-uw02.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-226-158.us-west-2.compute.amazonaws.com


ssh -f -N -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:18912:psp-prod-ibob.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-226-158.us-west-2.compute.amazonaws.com


ssh -f -N -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:18913:psp-prod-ue021.cerpnqmbpq9a.us-east-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-3-130-125-83.us-east-2.compute.amazonaws.com

ssh -f -N -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:18914:psp-prod-ibobdr.cluster-ro-cerpnqmbpq9a.us-east-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-3-130-125-83.us-east-2.compute.amazonaws.com

ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:18915:psp-prod-uw02.cluster-ro-cjls0bohfgpq.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-226-158.us-west-2.compute.amazonaws.com
