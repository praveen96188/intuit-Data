#!/bin/sh

secondaryUserInDB() {
  sqlplus sys/Oradoc_db1@ORCLCDB as sysdba <<EOF | grep "PSP_LOCAL1" | wc -l | xargs
    SELECT username, account_status, EXPIRY_DATE FROM dba_users WHERE username='PSP_LOCAL1';
    exit;
EOF
}
dbNotListening() {
  sqlplus sys/Oradoc_db1@ORCLCDB as sysdba <<EOF | grep "ORA-" | wc -l | xargs
      exit;
EOF
}

source $RC_FILE

if [ "$(secondaryUserInDB)" != "0" ]
then
  echo "Secondary User PSP_LOCAL1 already exists. Exiting start_oracle.sh"
  exit
fi

continue_polling=1

while [ "$(dbNotListening)" != "0" ]
do
  sleep 10
  echo "Waiting for database container to start listening..."
done

#debug
#echo "Adding secondary user after 240 seconds"
#sleep 240

# Create the secondary user PSP_LOCAL1
sqlplus sys/Oradoc_db1@ORCLCDB as sysdba <<EOF
ALTER SESSION SET "_ORACLE_SCRIPT"=true;
EXEC DBMS_XDB.SETHTTPPORT(8888);
ALTER SYSTEM SET PROCESSES=250 SCOPE=SPFILE;
CREATE USER PSP_LOCAL1
IDENTIFIED BY PSP_LOCAL1
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP
PROFILE DEFAULT
ACCOUNT UNLOCK;
GRANT CONNECT TO PSP_LOCAL1;
GRANT RESOURCE TO PSP_LOCAL1;
GRANT DBA TO PSP_LOCAL1;
ALTER USER PSP_LOCAL1 DEFAULT ROLE ALL;
GRANT CREATE SYNONYM TO PSP_LOCAL1;
GRANT CREATE MATERIALIZED VIEW TO PSP_LOCAL1;
GRANT UNLIMITED TABLESPACE TO PSP_LOCAL1;
GRANT CREATE VIEW TO PSP_LOCAL1;
GRANT CREATE PUBLIC SYNONYM TO PSP_LOCAL1;
GRANT CREATE TABLE TO PSP_LOCAL1;
GRANT CREATE TYPE TO PSP_LOCAL1;
GRANT CREATE TRIGGER TO PSP_LOCAL1;
GRANT CREATE PROCEDURE TO PSP_LOCAL1;
GRANT CREATE ROLE TO PSP_LOCAL1;
GRANT CREATE DATABASE LINK TO PSP_LOCAL1;
GRANT CREATE SEQUENCE TO PSP_LOCAL1;
ALTER profile DEFAULT limit PASSWORD_REUSE_TIME unlimited;
ALTER profile DEFAULT limit PASSWORD_LIFE_TIME  unlimited;
SELECT username, account_status, EXPIRY_DATE FROM dba_users WHERE username='PSP_LOCAL';
SELECT username, account_status, EXPIRY_DATE FROM dba_users WHERE username='PSP_LOCAL1';
quit
EOF

