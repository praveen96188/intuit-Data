ssh-add -D  
ssh-add ~/.ssh/id_rsa
eiamCli aws_ssh -a 152430470825 -p id_rsa.pub -d ~/.ssh/ 
ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:17010:ppsp-pds-db.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:5432 pnarlagalla1@ec2-52-38-34-141.us-west-2.compute.amazonaws.com

ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:17001:spsp-sys-db.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-34-141.us-west-2.compute.amazonaws.com

ssh -f -N -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:17002:ppsp-pds-dbdr.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-3-133-253-70.us-east-2.compute.amazonaws.com

ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:17003:ppsp-sys-mon.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-34-141.us-west-2.compute.amazonaws.com

ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:17004:ppsp-pds-uw02.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-34-141.us-west-2.compute.amazonaws.com

ssh -f -N  -o ServerAliveInterval=45 -i ~/.ssh/id_rsa  -L 127.0.0.1:17005:ppsp-ppd-arc.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:5432 pnarlagalla1_ro@ec2-52-38-34-141.us-west-2.compute.amazonaws.com

