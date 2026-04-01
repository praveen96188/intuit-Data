SELECT C.table_name as table_name,
       STRING_AGG(
                                                   'column:' || C.COLUMN_NAME ||
                                                   ',type:' || C.DATA_TYPE ||
                                                   ',max_length:' || coalesce(C.CHARACTER_MAXIMUM_LENGTH::text, '') ||
                                                   ',nullable:' || C.IS_NULLABLE ||
                                                   ',constraint:' || coalesce(TC.CONSTRAINT_TYPE, '') ||
                                                   ',indexes:' || coalesce(I.indexname, ''),  ' | '
           )        AS schema_details
FROM INFORMATION_SCHEMA.COLUMNS C
         LEFT JOIN
     INFORMATION_SCHEMA.KEY_COLUMN_USAGE KCU
     ON
                 C.TABLE_SCHEMA = KCU.TABLE_SCHEMA
             AND
                 C.TABLE_NAME = KCU.TABLE_NAME
             AND
                 C.COLUMN_NAME = KCU.COLUMN_NAME
         LEFT JOIN
     INFORMATION_SCHEMA.TABLE_CONSTRAINTS TC
     ON
                 KCU.CONSTRAINT_CATALOG = TC.CONSTRAINT_CATALOG
             AND
                 KCU.CONSTRAINT_SCHEMA = TC.CONSTRAINT_SCHEMA
             AND
                 KCU.CONSTRAINT_NAME = TC.CONSTRAINT_NAME
         LEFT JOIN
     pg_indexes I
     ON
             TC.CONSTRAINT_SCHEMA = I.SCHEMANAME
            AND TC.TABLE_NAME = I.TABLENAME
WHERE C.TABLE_SCHEMA = 'pspadm' and c.table_name = 'psp_financial_transaction_p0'
GROUP BY C.TABLE_NAME;

