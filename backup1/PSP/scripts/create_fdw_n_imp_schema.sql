\o create_fdw_n_imp_schema.log
\prompt 'Enter Oracle RDS db name (like psphpp02): ' oracle_db
\prompt 'Enter Oracle RDS domain_name (like sbg-psp-prod.a.intuit.com): ' oracle_domain 
\prompt 'Enter Oracle RDS db sid (like psphpp02): ' oracle_sid
\prompt 'Enter Oracle RDS user ops_user''s password: ' ops_user_password 

CREATE EXTENSION if not exists oracle_fdw; 

select 'CREATE SERVER ora_sv_' || :'oracle_db' || ' FOREIGN DATA WRAPPER oracle_fdw OPTIONS (dbserver ''' || :'oracle_db' || '.' || :'oracle_domain' || ':1521/' || :'oracle_sid' || ''', isolation_level ''read_committed'');'; \gexec

select 'GRANT USAGE ON FOREIGN SERVER ora_sv_' || :'oracle_db' || ' TO postgres'; \gexec

select 'CREATE USER MAPPING FOR postgres SERVER ora_sv_' || :'oracle_db' || ' OPTIONS ( USER ''ops_user'', PASSWORD ''' || :'ops_user_password' || ''')'; \gexec

select 'CREATE SCHEMA imp_' || :'oracle_db' || '_pspadm'; \gexec 

select 'IMPORT FOREIGN SCHEMA "PSPADM" FROM SERVER ora_sv_' || :'oracle_db' ||  ' INTO imp_' || :'oracle_db' || '_pspadm'; \gexec

select 'select * from information_schema.foreign_tables where  foreign_table_schema = ''imp_' || :'oracle_db' || '_pspadm'' order by 3';  \gexec

\o
\! cat create_fdw_n_imp_schema.log

