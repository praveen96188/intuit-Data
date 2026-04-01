\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

\i pspadm_tables.sql
\i pspadm_hash_part_tables.sql
\i pspadm_range_part_tables.sql
\i pspadm_const_pk.sql
\i pspadm_Index.sql
\i pspadm_hash_create_index.sql
\i pspadm_range_create_index.sql
\i pspadm_hash_attach_index.sql
\i pspadm_range_attach_index.sql
\i pspadm_con_fk.sql
\i pspadm_check_constraints.sql


SELECT CURRENT_TIMESTAMP;