ssh-add -D  
ssh-add ~/.ssh/id_rsa
eiamCli aws_ssh -a 893547637742 -p id_rsa.pub -d ~/.ssh/ 
ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:1900:ppsp-stg-arc.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-226-158.us-west-2.compute.amazonaws.com

ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:1901:ppsp-stg-pitparmo.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-226-158.us-west-2.compute.amazonaws.com

ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:1902:psp-par-aud.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com:6543 pnarlagalla1_ro@ec2-52-38-226-158.us-west-2.compute.amazonaws.com
