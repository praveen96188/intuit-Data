
cd /u01/postgres/scripts; ./chk_active_session_pg.sh spsp-sys-db sysibobdb 
cd /u01/postgres/scripts; ./chk_db_max_tran_id_pg.sh spsp-sys-db sysibobdb 
cd /u01/postgres/scripts; ./chk_catalog_size_pg.sh spsp-sys-db sysibobdb 
cd /u01/postgres/scripts; ./chk_long_running_queries_pg.sh spsp-sys-db sysibobdb 
cd /u01/postgres/scripts; ./chk_blocking_queries_pg.sh spsp-sys-db sysibobdb 

#Monolithsys Postgres DB Monitoring
cd /u01/postgres/scripts; ./chk_active_session_pg.sh ppsp-sys-mon psyspg01 
cd /u01/postgres/scripts; ./chk_db_max_tran_id_pg.sh ppsp-sys-mon psyspg01 
cd /u01/postgres/scripts; ./chk_catalog_size_pg.sh ppsp-sys-mon psyspg01 
#cd /u01/postgres/scripts; ./chk_long_running_queries_pg.sh ppsp-sys-mon psyspg01 
cd /u01/postgres/scripts; ./chk_blocking_queries_pg.sh ppsp-sys-mon psyspg01 

#Monolithpds Postgres DB Monitoring
cd /u01/postgres/scripts; ./chk_active_session_pg_new.sh ppsp-pds-uw02 ppdspg02 
cd /u01/postgres/scripts; ./chk_db_max_tran_id_pg.sh ppsp-pds-uw02 ppdspg02 
cd /u01/postgres/scripts; ./chk_catalog_size_pg.sh ppsp-pds-uw02 ppdspg02 
#cd /u01/postgres/scripts; ./chk_long_running_queries_pg.sh ppsp-pds-uw02 ppdspg02 
cd /u01/postgres/scripts; ./chk_blocking_queries_pg.sh ppsp-pds-uw02 ppdspg02 

#Auditpds Postgres DB Monitoring
cd /u01/postgres/scripts; ./chk_active_session_pg_new.sh ppsp-pds-db pdsibobdb 
cd /u01/postgres/scripts; ./chk_db_max_tran_id_pg.sh ppsp-pds-db pdsibobdb 
cd /u01/postgres/scripts; ./chk_catalog_size_pg.sh ppsp-pds-db pdsibobdb 
cd /u01/postgres/scripts; ./chk_long_running_queries_pg.sh ppsp-pds-db pdsibobdb 
cd /u01/postgres/scripts; ./chk_blocking_queries_pg.sh ppsp-pds-db pdsibobdb 

#outbox and enity_public partition alert
cd /u01/postgres/scripts; ./outbox_entity_pub_part_check.sh ppsp-sys-mon psyspg01 
##cd /u01/postgres/scripts; ./outbox_entity_pub_part_check.sh ppsp-pds-uw02 ppdspg02 

