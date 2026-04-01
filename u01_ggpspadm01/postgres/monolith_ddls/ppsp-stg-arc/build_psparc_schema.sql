\timing
set search_path=psparc;
SELECT CURRENT_TIMESTAMP;

\i /u01/postgres/monolith_ddls/ppsp-stg-arc\ppspstgarc_tables.sql
\i /u01/postgres/monolith_ddls/ppsp-stg-arc/ppspstgarc_hash_part_tables.sql
\i /u01/postgres/monolith_ddls/ppsp-stg-arc/ppspstgarc_range_part_tables.sql
\i /u01/postgres/monolith_ddls/ppsp-stg-arc/ppspstgarc_const_pk.sql
\i /u01/postgres/monolith_ddls/ppsp-stg-arc/ppspstgarc_con_fk.sql
\i /u01/postgres/monolith_ddls/ppsp-stg-arc/create_index_fk_cols_all_tables.sql
\i /u01/postgres/monolith_ddls/ppsp-stg-arc/ppspstgarc_check_constraints.sql


SELECT CURRENT_TIMESTAMP;
