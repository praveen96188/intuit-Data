ssh-add -D  
ssh-add ~/.ssh/id_rsa
eiamCli aws_ssh -a 893547637742 -p id_rsa.pub -d ~/.ssh/  
ssh -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:18101:psp-prod-ibob.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-226-158.us-west-2.compute.amazonaws.com
