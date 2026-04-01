#PSP Postgres Migration 

### Migration from PRAGMA Autonomous to DB_Link
*Steps to setup grant and create dblink in local

     Login to PSPADM (Super user) and Run below commands in admin console
        CREATE EXTENSION dblink schema psp_local;
        GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO psp_local;
        GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO psp_local;
        GRANT EXECUTE ON FUNCTION dblink_connect_u(text, text) TO psp_local;
        GRANT EXECUTE ON FUNCTION dblink_exec(text,text) TO psp_local;
        GRANT EXECUTE ON FUNCTION dblink_disconnect(text) TO psp_local;

        CREATE SERVER loopback_dblink FOREIGN DATA WRAPPER dblink_fdw OPTIONS (hostaddr '127.0.0.1',port '5432', dbname 'psp');
        CREATE USER MAPPING FOR public SERVER loopback_dblink OPTIONS (user 'psp_local', password 'psp_local');








