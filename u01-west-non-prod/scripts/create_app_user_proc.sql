create or replace procedure create_app_admin_db_users(p_username varchar2, p_password varchar2)
   AUTHID CURRENT_USER 
as
begin
  -- create user
  execute immediate  'CREATE user '||p_username||' IDENTIFIED BY "'||p_password||'" DEFAULT TABLESPACE USERS TEMPORARY TABLESPACE TEMP PROFILE GENERIC_APP_PROFILE' ;
  -- grant quota on tablespace 
  execute immediate  'ALTER USER '||p_username||' QUOTA UNLIMITED ON USERS' ;
  -- grant roles
  execute immediate 'GRANT CONNECT TO '||p_username||'' ;
  execute immediate 'GRANT RESOURCE TO '||p_username||'' ;
  execute immediate 'GRANT SELECT_CATALOG_ROLE TO '||p_username||'' ;

  -- grant sys privileges 
  execute immediate 'GRANT EXECUTE ANY INDEXTYPE TO '||p_username||'' ;
  execute immediate 'GRANT EXECUTE ANY TYPE TO '||p_username||'' ;
  execute immediate 'GRANT EXECUTE ANY PROCEDURE TO '||p_username||'' ;
  execute immediate 'GRANT SELECT ANY SEQUENCE TO '||p_username||'' ;
  execute immediate 'GRANT DELETE ANY TABLE TO '||p_username||'' ;
  execute immediate 'GRANT UPDATE ANY TABLE TO '||p_username||'' ;
  execute immediate 'GRANT INSERT ANY TABLE TO '||p_username||'' ;
  execute immediate 'GRANT SELECT ANY TABLE TO '||p_username||'' ;
  execute immediate 'GRANT UNLIMITED TABLESPACE TO '||p_username||'' ;
  execute immediate 'GRANT ALTER SESSION TO '||p_username||'' ;

  -- default all role
  execute immediate 'ALTER USER '||p_username||' DEFAULT ROLE ALL' ;
   
end;
/
