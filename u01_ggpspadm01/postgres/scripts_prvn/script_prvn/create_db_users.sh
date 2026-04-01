#!/bin/sh
cluster=$1
pass="$2"
for cnt in {0..99}
do
username=$(printf ${cluster}'_UW2APP%02d' $cnt )
echo " CREATE USER  $username IDENTIFIED BY $pass  DEFAULT TABLESPACE users  TEMPORARY TABLESPACE temp  PROFILE GENERIC_APP_PROFILE  account lock;"
echo "grant connect, resource to $username;"
echo "grant select_catalog_role to $username ;"
echo "grant select any table, select any sequence, update any table, insert any table, delete any  table  to $username;"
echo "grant execute any procedure to $username;"
echo "grant execute any type to $username;"
echo "grant execute any indextype to $username;"
echo "grant alter session to $username;"
echo "grant unlimited tablespace to $username;"
echo "grant alter system to $username                   ;"
echo "grant execute on sys.dbms_session to $username    ;"
done
echo " CREATE USER  ${cluster}_UW2admin IDENTIFIED BY $pass  DEFAULT TABLESPACE users  TEMPORARY TABLESPACE temp  PROFILE GENERIC_APP_PROFILE account lock;"
echo " grant connect, resource to ${cluster}_UW2admin;"
echo " grant select_catalog_role to ${cluster}_UW2admin ;"
echo " grant select any table, select any sequence, update any table, insert any table, delete any  table  to ${cluster}_UW2admin;"
echo " grant execute any procedure to ${cluster}_UW2admin;"
echo " grant execute any type to ${cluster}_UW2admin;"
echo " grant execute any indextype to ${cluster}_UW2admin;"
echo " grant alter session to ${cluster}_UW2admin;"
echo " grant unlimited tablespace to ${cluster}_UW2admin;"
echo " grant create public database link to ${cluster}_UW2admin    ;"
echo " grant drop public database link to ${cluster}_UW2admin;"
echo " grant alter system to ${cluster}_UW2admin                   ;"
echo " grant execute on sys.dbms_session to ${cluster}_UW2admin    ;"
echo "exit "
