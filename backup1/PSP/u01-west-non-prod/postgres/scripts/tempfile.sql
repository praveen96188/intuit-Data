select * from pg_replication_slots where active='f';
      slot_name       |  plugin  | slot_type | datoid | database | temporary | active | active_pid | xmin | catalog_xmin | restart_lsn | confirmed_flush_lsn | wal_status | safe_wal_size | two_phase 
----------------------+----------+-----------+--------+----------+-----------+--------+------------+------+--------------+-------------+---------------------+------------+---------------+-----------
 my_temp_logical_slot | wal2json | logical   |  16401 | psyspg01 | f         | f      |            |      |    100248903 | 52/7A5220C8 | 52/7A522120         | reserved   |               | f
(1 row)

