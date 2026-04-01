create user datalake_parity with password 'xxxxxx';
GRANT CONNECT on DATABASE pspapg02 TO datalake_parity;
GRANT USAGE ON SCHEMA pspadm TO datalake_parity;
GRANT SELECT ON ALL TABLES IN SCHEMA pspadm TO datalake_parity;

create user datalake_bootstrap with password 'xxxxxx';
GRANT CONNECT on DATABASE pspapg02 TO datalake_bootstrap;
GRANT USAGE ON SCHEMA pspadm TO datalake_bootstrap;
GRANT rds_replication to datalake_bootstrap;

create user datalake_debezium with password 'xxxxxx';
GRANT CONNECT on DATABASE pspapg02 TO datalake_debezium;
GRANT USAGE ON SCHEMA pspadm TO datalake_debezium;
GRANT rds_replication to datalake_debezium;
