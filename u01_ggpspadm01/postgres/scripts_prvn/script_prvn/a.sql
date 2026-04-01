DO $$
  DECLARE
    -- the ddl we're building
    v_table_ddl text;
    v_partition_key text := '';
    v_relopts text;
    v_temp  text := ''; 
    v_tablespace text;

    -- data about the target table
    v_table_oid int;

    -- records for looping
    v_column_record record;
    v_constraint_record record;
    v_index_record record;
  BEGIN
    -- grab the oid of the table; 
    SELECT c.oid INTO v_table_oid
    FROM pg_catalog.pg_class c
    LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    WHERE 1=1
      AND c.relkind in ('r','p')  
      AND c.relname = 'psp_financial_transaction' -- the table name
      AND n.nspname = 'pspadm'; -- the schema

    -- throw an error if table was not found
    IF (v_table_oid IS NULL) THEN
      RAISE EXCEPTION 'table does not exist';
    END IF;

    -- get user-defined tablespaces if applicable
    SELECT tablespace INTO v_temp FROM pg_tables WHERE schemaname = 'pspadm' and tablename = 'psp_financial_transaction' and tablespace IS NOT NULL;
    IF v_temp IS NULL THEN
      v_tablespace := 'TABLESPACE pg_default';
    ELSE
      v_tablespace := 'TABLESPACE ' || v_temp;
    END IF;
    
    -- also see if there are any SET commands for this table, ie, autovacuum_enabled=off, fillfactor=70
    WITH relopts AS (SELECT unnest(c.reloptions) relopts FROM pg_class c, pg_namespace n WHERE n.nspname = 'pspadm' and n.oid = c.relnamespace and c.relname = 'psp_financial_transaction') 
    SELECT string_agg(r.relopts, ', ') as relopts INTO v_temp from relopts r;
    IF v_temp IS NULL THEN
      v_relopts := '';
    ELSE
      v_relopts := ' WITH (' || v_temp || ')';
    END IF;

    -- start the create definition
    v_table_ddl := 'CREATE TABLE ' || 'pspadm' || '.' || 'psp_financial_transaction' || ' (' || E'\n';

    -- define all of the columns in the table
    FOR v_column_record IN
      SELECT
        c.column_name,
        c.data_type,
        c.character_maximum_length,
        c.is_nullable,
        c.column_default
      FROM information_schema.columns c
      WHERE (table_schema, table_name) = ('pspadm', 'psp_financial_transaction')
      ORDER BY ordinal_position
    LOOP
      v_table_ddl := v_table_ddl || '  ' -- note: two char spacer to start, to indent the column
        || v_column_record.column_name || ' '
        || v_column_record.data_type || CASE WHEN v_column_record.character_maximum_length IS NOT NULL THEN ('(' || v_column_record.character_maximum_length || ')') ELSE '' END || ' '
        || CASE WHEN v_column_record.is_nullable = 'NO' THEN 'NOT NULL' ELSE 'NULL' END
        || CASE WHEN v_column_record.column_default IS NOT null THEN (' DEFAULT ' || v_column_record.column_default) ELSE '' END
        || ',' || E'\n';
    END LOOP;

    -- drop the last comma before ending the create statement
    v_table_ddl = substr(v_table_ddl, 0, length(v_table_ddl) - 1) || E'\n';

    -- See if this is a partitioned table (pg_class.relkind = 'p') and add the partitioned key 
    SELECT pg_get_partkeydef(c1.oid) as partition_key INTO v_partition_key FROM pg_class c1 JOIN pg_namespace n ON (n.oid = c1.relnamespace) LEFT JOIN pg_partitioned_table p ON (c1.oid = p.partrelid) 
    WHERE n.nspname = 'pspadm' and n.oid = c1.relnamespace and c1.relname = 'psp_financial_transaction' and c1.relkind = 'p';

      IF v_partition_key IS NOT NULL AND v_partition_key <> '' THEN
        -- add partition clause
        -- NOTE:  cannot specify default tablespace for partitioned relations
        -- v_table_ddl := v_table_ddl || ') PARTITION BY ' || v_partition_key || ' ' || v_tablespace || ';' || E'\n';  
        v_table_ddl := v_table_ddl || ') PARTITION BY ' || v_partition_key || ';' || E'\n';  
      ELSEIF v_relopts <> '' THEN
        v_table_ddl := v_table_ddl || ') ' || v_relopts || ' ' || v_tablespace || ';' || E'\n';  
      ELSE
        -- end the create definition
        v_table_ddl := v_table_ddl || ') ' || v_tablespace || ';' || E'\n';    
      END IF;  


    -- end the create definition
    --v_table_ddl := v_table_ddl || ';' || E'\n';

    -- define all the constraints
    FOR v_constraint_record IN
      SELECT
        con.conname as constraint_name,
        con.contype as constraint_type,
        CASE
          WHEN con.contype = 'p' THEN 1 -- primary key constraint
          WHEN con.contype = 'u' THEN 2 -- unique constraint
          WHEN con.contype = 'f' THEN 3 -- foreign key constraint
          WHEN con.contype = 'c' THEN 4
          ELSE 5
        END as type_rank,
        pg_get_constraintdef(con.oid) as constraint_definition
      FROM pg_catalog.pg_constraint con
      JOIN pg_catalog.pg_class rel ON rel.oid = con.conrelid
      JOIN pg_catalog.pg_namespace nsp ON nsp.oid = connamespace
      WHERE nsp.nspname = 'pspadm'
      AND rel.relname = 'psp_financial_transaction'
      ORDER BY type_rank
    LOOP
      v_table_ddl := v_table_ddl || '  ' -- note: two char spacer to start, to indent the column
        || 'CONSTRAINT' || ' '
        || v_constraint_record.constraint_name || ' '
        || v_constraint_record.constraint_definition
        || ',' || E'\n';
    END LOOP;

    -- suffix create statement with all of the indexes on the table
    FOR v_index_record IN
      SELECT indexdef
      FROM pg_indexes
      WHERE (schemaname, tablename) = ('pspadm', 'psp_financial_transaction')
    LOOP
      v_table_ddl := v_table_ddl
        || v_index_record.indexdef
        || ';' || E'\n';
    END LOOP;
    RAISE NOTICE '%', v_table_ddl;
END;
$$;

