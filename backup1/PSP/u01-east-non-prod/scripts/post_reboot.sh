sudo sed -i 's|/bin/nologin|/bin/bash|g' /etc/passwd
sudo -i <<'EOF'
whoami
mkdir -p /l/ ; 
cp /u01/scripts/orcl /l/; 
echo 'l dir created and copied orcl file  to l';
EOF
sudo su  - ec2-user  <<'EOF'
whoami
crontab /u01/scripts/crontab.txt; 
echo 'cron job setup completed';
EOF

# Check if the 'expect' package is installed
if ! rpm -q expect > /dev/null 2>&1; then
    echo "'expect' package not found, installing..."
    sudo yum install -y expect
else
    echo "'expect' package is already installed."
fi

# Switch to ec2-user and check if 'psycopg2-binary' is installed
echo "Switching to ec2-user..."
sudo -u ec2-user bash << EOF
    # Check if 'psycopg2-binary' is installed
    if ! python3 -m pip show psycopg2-binary > /dev/null 2>&1; then
        echo "'psycopg2-binary' not found, installing..."
        python3 -m pip install psycopg2-binary
    else
        echo "'psycopg2-binary' is already installed."
    fi
EOF

sudo su  - ec2-user  <<'EOF'
python3 -m pip install --upgrade pip
pip install pandas
pip install pexpect
EOF


sudo su  - ec2-user  <<'EOF'
whoami
#Monolithpds Postgres DB Monitoring
cd /u01/postgres/scripts; ./chk_db_max_tran_id_pg.sh ppsp-pds-uw02dr ppdspg02 
cd /u01/postgres/scripts; ./chk_catalog_size_pg.sh ppsp-pds-uw02dr ppdspg02 
cd /u01/postgres/scripts; ./chk_long_running_queries_pg.sh ppsp-pds-uw02dr ppdspg02 
cd /u01/postgres/scripts; ./chk_blocking_queries_pg.sh ppsp-pds-uw02dr ppdspg02 

#Auditpds Postgres DB Monitoring
cd /u01/postgres/scripts; ./chk_active_session_pg.sh ppsp-pds-dbdr pdsibobdb 
cd /u01/postgres/scripts; ./chk_db_max_tran_id_pg.sh ppsp-pds-dbdr pdsibobdb 
cd /u01/postgres/scripts; ./chk_catalog_size_pg.sh ppsp-pds-dbdr pdsibobdb 
cd /u01/postgres/scripts; ./chk_long_running_queries_pg.sh ppsp-pds-dbdr pdsibobdb 
cd /u01/postgres/scripts; ./chk_blocking_queries_pg.sh ppsp-pds-dbdr pdsibobdb 

EOF
