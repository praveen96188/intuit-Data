--login as master user and connect to database
--Postgres Staging
set search_path to pspadm;
CREATE EXTENSION dblink schema pspadm;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO pspbatch_rw_user;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text, text) TO pspbatch_rw_user;
GRANT EXECUTE ON FUNCTION dblink_exec(text,text) TO pspbatch_rw_user;
GRANT EXECUTE ON FUNCTION dblink_disconnect(text) TO pspbatch_rw_user;
CREATE SERVER loopback_dblink FOREIGN DATA WRAPPER dblink_fdw OPTIONS (host 'ppsp-stg-pitparmo.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com',port '5432', dbname 'pitparmo');
CREATE USER MAPPING FOR public SERVER loopback_dblink OPTIONS (user 'pspbatch_rw_user', password 'xxxxx');


---OLAP Staging
set search_path to pspadm;
CREATE EXTENSION dblink schema pspadm;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO pspbatch_rw_user;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text, text) TO pspbatch_rw_user;
GRANT EXECUTE ON FUNCTION dblink_exec(text,text) TO pspbatch_rw_user;
GRANT EXECUTE ON FUNCTION dblink_disconnect(text) TO pspbatch_rw_user;
CREATE SERVER loopback_dblink FOREIGN DATA WRAPPER dblink_fdw OPTIONS (host 'ppsp-stg-olap.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com',port '5432', dbname 'pstgolap');
CREATE USER MAPPING FOR public SERVER loopback_dblink OPTIONS (user 'pspbatch_rw_user', password 'xxxxxx');

