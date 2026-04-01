#!/bin/bash

#Set DB time zone to PST
psql --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -c "ALTER DATABASE $POSTGRES_DB SET TIMEZONE TO 'America/Los_Angeles'"
echo "**** DB timezone set to 'America/Los_Angeles' ****"

#Create Users and Schemas
psql --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f /tmp/CreateUsersSchemas.sql
echo "**** Users Created ****"

# Setup Monolith Schema inside same DB
cd /tmp/monolith
psql --username "psp_local" --dbname "$POSTGRES_DB" -f InstallDB.sql
echo "**** Monolith DB Setup done ****"

# Setup Audit Schema inside same DB
cd /tmp/audit
psql --username "psp_local1" --dbname "$POSTGRES_DB" -f InstallDB.sql
echo "**** Audit DB Setup done ****"

# Replacing custom pg hint configuration file
#cd /tmp
#cp postgresql.conf /var/lib/postgresql/data
