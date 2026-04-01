--CREATE DB LINKS REQUIRED FOR MIGRATION
--CAUTION : Do not change the db link names , that will cause the migration scripts to fail.
--CAUTION : Make sure to change the tns entry to point to right instance for prod run.

-- it needs to run as pspadm user

PROMPT CREATE DB LINK TO CRIS_PROD INSTANCE
CREATE DATABASE LINK CRIS_PROD
 CONNECT TO PSPMIG
 IDENTIFIED BY &&pspmig_cris_password
 USING &&crisprod_tnsname;

PROMPT CREATE DB LINK TO DRIVER TABLE 

CREATE DATABASE LINK DRIVER_LNK
 CONNECT TO DM_10_1
 IDENTIFIED BY &&dm_10_1_password
 USING &&siebelprod_tnsname;

PROMPT CREATE DB LINK TO SIEBEL_PROD INSTANCE

CREATE DATABASE LINK SIEBEL_PROD
 CONNECT TO PSPMIG
 IDENTIFIED BY &&pspmig_siebel_prod_passwd
 USING &&siebelprod_tnsname;
