ssh-add -D  
ssh-add ~/.ssh/id_rsa
eiamCli aws_ssh -a 152430470825 -p id_rsa.pub -d ~/.ssh/ 
ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:19001:ppsp-ppd-arc-new.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-34-141.us-west-2.compute.amazonaws.com
