update backup_oos.oos_metadata x
 set pk_cols=
 (select string_agg(kcu.column_name, ', ') as key_columns
from information_schema.tables tab
         left join information_schema.table_constraints tco
                   on tco.table_schema = tab.table_schema
                       and tco.table_name = tab.table_name
                       and tco.constraint_type = 'PRIMARY KEY'
         left join information_schema.key_column_usage kcu
                   on kcu.constraint_name = tco.constraint_name
                       and kcu.constraint_schema = tco.constraint_schema
                       and kcu.constraint_name = tco.constraint_name
where tab.table_schema ='pspadm' and x.table_name=tab.table_name);

