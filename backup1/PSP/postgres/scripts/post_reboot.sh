sudo -i <<'EOF'
whoami
mkdir -p /l/ ; 
cp /u01/orcl /l/; 
EOF
sudo su  - ec2-user  <<'EOF'
whoami
crontab /u01/scripts/crontab.txt; 
EOF
