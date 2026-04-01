--sample script to load csv data to OOS metadata table
\COPY backup_oos.oos_metadata(op_type, status, table_name, pk_seq, company_fk) FROM '/Users/mchoubey/Downloads/mergedOOSRepair.csv' DELIMITER ',' CSV HEADER;
