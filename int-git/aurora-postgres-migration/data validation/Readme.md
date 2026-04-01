
# OOS Data Fix between Oracle and Postgres

Steps to fix data in Postgres for Oracle to Postgres replication

1. create FDW
```sql
\i create_fdw_n_imp_schema.sql
```
2. create schema and objects to load OOS csv metadata
```sql
\i create_oos_metadata_schema.sql
```

3. Load OOS csv data to metadata table
```sql
sample_script_to_load_csv.sql
```
4. for each table from metadata table 
        for each operation type for this table
            backup existing records on postgres by selecting data via PK
            execute respective DML operation on postgres by selecting data from source via PK
            validata data between oracle and postgres
            commit
        done
   done 
```sql
sample_dml_generation_script.sql

```

5. Enhance automatio to backup existing records

