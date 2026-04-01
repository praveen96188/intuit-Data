SET PAGESIZE 50000
SET ECHO OFF
SET MARKUP HTML ON ENTMAP ON SPOOL ON PREFORMAT OFF
set FEEDBACK on
set HEADING ON
spool /tmp/oracle_obj_validation.html

prompt ' validate table count'
SELECT count(1) AS tables_cnt
  FROM all_tables
 WHERE owner = upper('pspadm');

prompt 'Validate views count'
SELECT count(1) AS views_cnt
  FROM all_views
 WHERE owner = upper('pspadm');

prompt 'validate sequence count'
SELECT count(1) AS sequence_cnt
  FROM all_sequences
 WHERE sequence_owner = upper('pspadm');

prompt ' Validate triggers count'
SELECT 
       owner AS schema_name, 
       trigger_name, 
       table_name, 
       triggering_event, 
       trigger_type
  FROM ALL_TRIGGERS
 WHERE owner = upper('pspadm')
ORDER BY trigger_name; 

prompt 'validate Primary key'
SELECT owner           AS schema_name, 
       table_name,
       constraint_name AS object_name,
       'PRIMARY KEY'   AS object_type
  FROM all_constraints
 WHERE owner = upper('pspadm') 
   AND constraint_type = 'P';

prompt 'validate type of index and column order for all tables for all indexes'
WITH cols AS (
    SELECT idx.owner AS schema_name, idx.table_name, idx.index_name, cols.column_name, cols.column_position, idx.uniqueness, decode(cols.descend, 'ASC', '', ' '||cols.descend) descend
      FROM ALL_INDEXES idx, ALL_IND_COLUMNS cols
     WHERE idx.owner = cols.index_owner AND idx.table_name = cols.table_name AND idx.index_name = cols.index_name
       AND idx.owner = upper('pspadm')
),
expr AS (
    SELECT extractValue(xs.object_value, '/ROW/TABLE_NAME')         AS table_name
    ,      extractValue(xs.object_value, '/ROW/INDEX_NAME')         AS index_name
    ,      extractValue(xs.object_value, '/ROW/COLUMN_EXPRESSION')  AS column_expression
    ,      extractValue(xs.object_value, '/ROW/COLUMN_POSITION')    AS column_position
    FROM (
         SELECT XMLTYPE(
              DBMS_XMLGEN.GETXML( 'SELECT table_name, index_name, column_expression, column_position FROM ALL_IND_EXPRESSIONS WHERE index_owner = upper(''pspadm'') '
                                ||' union all SELECT null, null, null, null FROM dual '
              )
           ) AS xml FROM DUAL
       ) x
    , TABLE(XMLSEQUENCE(EXTRACT(x.xml, '/ROWSET/ROW'))) xs
)
SELECT 
       cols.schema_name, 
       cols.table_name, 
       cols.index_name    AS object_name, 
       'INDEX'            AS object_type,
       replace('CREATE'|| decode(cols.uniqueness, 'UNIQUE', ' '||cols.uniqueness) || ' INDEX ' || cols.index_name || ' ON pspadm.' || cols.table_name || ' USING BTREE (' ||
            listagg(CASE WHEN cols.column_name LIKE 'SYS_N%' THEN expr.column_expression || cols.descend ELSE cols.column_name || cols.descend END, ', ') within group(order by cols.column_position) || ')', '"', '') AS condition_column
FROM cols
     LEFT OUTER JOIN expr ON cols.table_name = expr.table_name
           AND cols.index_name      = expr.index_name
           AND cols.column_position = expr.column_position
GROUP BY cols.schema_name, cols.table_name, cols.index_name, cols.uniqueness;

prompt 'validate check constraints'
WITH ref AS (
 SELECT   extractValue(xs.object_value, '/ROW/OWNER')            AS schema_name
   ,      extractValue(xs.object_value, '/ROW/TABLE_NAME')       AS table_name
   ,      extractValue(xs.object_value, '/ROW/CONSTRAINT_NAME')  AS object_name
   ,      extractValue(xs.object_value, '/ROW/SEARCH_CONDITION') AS condition_column
   ,      extractValue(xs.object_value, '/ROW/COLUMN_NAME')      AS column_name
  FROM (
         SELECT XMLTYPE(
              DBMS_XMLGEN.GETXML('SELECT cons.owner, cons.table_name, cons.constraint_name, cons.search_condition, cols.column_name
                           FROM ALL_CONSTRAINTS cons, ALL_CONS_COLUMNS cols
                           WHERE cons.owner =  cols.owner AND cons.table_name =  cols.table_name AND cons.constraint_name = cols.constraint_name
                             AND cons.owner = upper(''pspadm'') AND cons.constraint_type = ''C'' '
                             )
           ) AS xml FROM DUAL
       ) x
   , TABLE(XMLSEQUENCE(EXTRACT(x.xml, '/ROWSET/ROW'))) xs
)
SELECT schema_name||'.'||table_name             AS table_name,
       object_name,
       constraint_type,
       trim(upper(replace(check_condition, '"', ''))) AS check_condition
  FROM (
         SELECT 
                schema_name, 
                table_name, 
                object_name, 
                'CHECK'                AS constraint_type, 
                condition_column       AS check_condition
         FROM ref
         UNION
         SELECT 
                owner                  AS schema_name, 
                table_name, 
                'SYS_C0000'||column_id AS object_name, 
                'CHECK'                AS constraint_type, 
                '"'||column_name||'" IS NOT NULL' AS check_condition
         FROM all_tab_columns tcols where owner = upper('pspadm') and nullable = 'N'
         AND NOT EXISTS ( SELECT 1 FROM ref WHERE ref.table_name = tcols.table_name
                                    AND ref.schema_name = tcols.owner
                                    AND ref.column_name = tcols.column_name
                                    AND ref.condition_column = '"'||tcols.column_name||'" IS NOT NULL')
        /* ALL_TAB_COLUMNS contains Tables and Views. Add below to exclude Views NOT NULL constraints */
        AND NOT EXISTS ( SELECT 1 FROM ALL_VIEWS vw WHERE vw.view_name = tcols.table_name
                                   AND vw.owner = tcols.owner
                       )
);

prompt 'validate foreign keys'
SELECT 
       c.child_tab_owner           AS schema_name, 
       c.table_name, 
       c.constraint_name           AS object_name, 
       'FOREIGN KEY'               AS object_type,
       'FOREIGN KEY ('|| cc.fk_column || ') REFERENCES ' || p.parent_tab_owner || '.' || p.table_name || '('|| pc.ref_column ||') NOT VALID' AS condition_column
FROM ( SELECT owner child_tab_owner, table_name, constraint_name, r_constraint_name FROM ALL_CONSTRAINTS WHERE owner = upper('pspadm') AND constraint_type = 'R') c,
     ( SELECT owner parent_tab_owner, table_name, constraint_name FROM ALL_CONSTRAINTS WHERE owner = upper('pspadm') AND constraint_type IN('P', 'U') ) p,
     ( SELECT owner, table_name, constraint_name, listagg(column_name, ', ') WITHIN group(ORDER BY position) fk_column
         FROM ALL_CONS_COLUMNS WHERE owner = upper('pspadm') GROUP BY owner, table_name, constraint_name ) cc,
     ( SELECT owner, table_name, constraint_name, listagg(column_name, ', ') WITHIN group(ORDER BY position) ref_column
         FROM ALL_CONS_COLUMNS WHERE owner = upper('pspadm') GROUP BY owner, table_name, constraint_name ) pc
WHERE c.r_constraint_name = p.constraint_name
  AND c.table_name = cc.table_name AND c.constraint_name = cc.constraint_name AND c.child_tab_owner = cc.owner
  AND p.table_name = pc.table_name AND p.constraint_name = pc.constraint_name AND p.parent_tab_owner = pc.owner;

