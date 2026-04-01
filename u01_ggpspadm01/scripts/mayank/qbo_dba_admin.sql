create user qbo_dba_admin identified by C2#KN2pYJn default tablespace users temporary tablespace temp;
grant connect to qbo_dba_admin;
grant resource to qbo_dba_admin;
grant select_catalog_role to qbo_dba_admin;
grant unlimited tablespace to qbo_dba_admin;
grant analyze any to qbo_dba_admin;
grant exempt access policy to qbo_dba_admin;
grant create job to qbo_dba_admin;
grant manage scheduler to qbo_dba_admin;

-- grant select on tables undergoing attribute clustering 
grant select on qbo_data.names_1 to qbo_dba_admin;
grant select on qbo_data.arapcreditpmntchargelinks_1 to qbo_dba_admin;
grant select on qbo_data.txdetails_1 to qbo_dba_admin;
grant select on qbo_data.txheaders_1 to qbo_dba_admin;
grant select on qbo_data.cashbasis_1 to qbo_dba_admin;
grant select on qbo_data.auditinfo_1 to qbo_dba_admin;

-- grant alter on objects undergoing attribute clustering
grant alter on qbo_data.names_1 to qbo_dba_admin;
grant alter on qbo_data.arapcreditpmntchargelinks_1 to qbo_dba_admin;
grant alter on qbo_data.txdetails_1 to qbo_dba_admin;
grant alter on qbo_data.txheaders_1 to qbo_dba_admin;
grant alter on qbo_data.cashbasis_1 to qbo_dba_admin;
grant alter on qbo_data.auditinfo_1 to qbo_dba_admin;
