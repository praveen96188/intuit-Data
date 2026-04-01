\timing
set search_path=psparc;
SELECT CURRENT_TIMESTAMP;

\i psparc_tables.sql
\i psparc_hash_part_tables.sql
\i psparc_range_part_tables.sql
\i psparc_const_pk.sql
\i psparc_con_fk.sql
\i create_index_fk_cols_all_tables.sql
\i psparc_check_constraints.sql


SELECT CURRENT_TIMESTAMP;
