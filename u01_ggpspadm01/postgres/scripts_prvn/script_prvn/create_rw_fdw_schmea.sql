\o create_rw_fdw_schmea.log
\prompt 'Enter Oracle RDS db name (like psphpp02): ' oracle_db
\prompt 'Enter Oracle RDS domain_name (like sbg-psp-prod.a.intuit.com): ' oracle_domain
\prompt 'Enter Oracle RDS db sid (like psphpp02): ' oracle_sid
\prompt 'Enter Oracle RDS user ops_rw_user''s password: ' ops_rw_user_password

CREATE EXTENSION if not exists oracle_fdw;

select 'CREATE SERVER ora_rw_sv_' || :'oracle_db' || ' FOREIGN DATA WRAPPER oracle_fdw OPTIONS (dbserver ''' || :'oracle_db' || '.' || :'oracle_domain' || ':1521/' || :'oracle_sid' || ''', isolation_level ''read_committed'');'; \gexec

select 'GRANT USAGE ON FOREIGN SERVER ora_rw_sv_' || :'oracle_db' || ' TO postgres'; \gexec

select 'CREATE USER MAPPING FOR postgres SERVER ora_rw_sv_' || :'oracle_db' || ' OPTIONS ( USER ''ops_rw_user'', PASSWORD ''' || :'ops_rw_user_password' || ''')'; \gexec

select 'CREATE SCHEMA imp_rw_' || :'oracle_db' || '_pspadm'; \gexec

select 'IMPORT FOREIGN SCHEMA "PSPADM" FROM SERVER ora_rw_sv_' || :'oracle_db' ||  ' INTO imp_rw_' || :'oracle_db' || '_pspadm'; \gexec

select 'select * from information_schema.foreign_tables where  foreign_table_schema = ''imp_rw_' || :'oracle_db' || '_pspadm'' order by 3';  \gexec

\o
\! cat create_rw_fdw_schmea.log

