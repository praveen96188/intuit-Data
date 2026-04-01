/*$Header: coe_xplain.sql 2.2 2000/04/06 12:18:00               csierra coe $ */
SET term off;
/*=============================================================================
 OVERVIEW:
    Generates Explain Plan for one statement.  Includes table, index and
    index_column information (statistics).

 INSTRUCTIONS:
    Insert the statement under the 'Generates Explain Plan' section. End the
    statement with a semicolon ';'. Save and run this script.  

 NOTES:
 1. If PLAN_TABLE does not exist, please create it using the corresponding 
    script $ORACLE_HOME/rdbms/admin/utlxplan.sql
 2. The Explain Plan is spooled to file COE_XPLAIN.LST contains.  The original
    statement is spooled to COE_STATEMENT.LST
 3. Open the spooled files using WordPad, change the font to Courier New, style
    regular and size 8.  Set up the page to Portrait with all 4 margins 0.5 in.
 4. For the latest version of this script look in the following url or Metalink
    http://coe-svr4-pc.us.oracle.com/pmdocs/tools/coe_scripts/coe_xplain.sql
        
 DISCLAIMER:
    This script is provided for educational purposes only.  It is not supported
    by Oracle World Wide Technical Support.  The script has been tested and 
    appears to works as intended.  However, you should always test any script 
    before relying on it. 
    Proofread this script prior to running it!  Due to differences in the way 
    text editors, email and operating systems handle text formatting (spaces, 
    tabs and carriage returns), this script may not be in an executable state 
    when you first receive it.  Check over the script to ensure that errors of 
    this type are corrected. 
    This script can be given to customers.  Do not remove disclaimer paragraph.

 HISTORY:
    02-DEC-99 Created                                                   csierra
    21-JAN-00 Row Count(*) for Tables in Explain Plan is added          csierra
    17-FEB-00 Index summary is added                                    csierra
    20-MAR-00 Statistics information is enhanced and index_column added csierra
    06-APR-00 Operation Type and Order columns are incorporated         csierra
 =============================================================================*/

/* Generates new Explain Plan in emptied plan_table                           */
DELETE FROM plan_table;
SPOOL coe_statement.lst;
SET pages 1000;
SET lin 115;
SET sqlp '';
SET sqln off;
SET term on;
SET echo on;

explain plan set statement_id = 'COE_XPLAIN' for /* coe_xplain.sql 2.2
=======================================================================
Generate Explain Plan for statement below (ending with a semicolon ';') 
=======================================================================    
                                                                              */
SELECT ee.STATUS_CD, count(ee.EMPLOYEE_SEQ)
from pspadm.PSP_Employee ee,
     pspadm.PSP_company co
where co.COMPANY_SEQ = ee.COMPANY_FK
group by ee.STATUS_CD; 

SET echo off;
SET term off;
SPOOL OFF;

/* Computes the Order (Ord) column in Explain Plan (using the Remarks column) */
DECLARE
    coe_pointer     number := 0;  -- Row on PLAN_TABLE being processed.
    coe_next_order  number := 1;  -- To update Order (in Remarks column).
    coe_xplain      CONSTANT varchar2(30) := 'COE_XPLAIN'; -- statement_id.
    coe_parent_id   number;       -- To move pointer (only if needed).
    coe_curr_order  varchar2(80); -- Order in current row of PLAN_TABLE.
    coe_count_child number;       -- Number of children for a parent.
BEGIN
    LOOP
        SELECT parent_id, remarks            -- Reads PLAN_TABLE using pointer.
        INTO   coe_parent_id, coe_curr_order -- Starts on first row (id=0) and
        FROM   PLAN_TABLE                    -- works its way down.
        WHERE  id           = coe_pointer
        AND    statement_id = coe_xplain;
        IF  coe_curr_order is not null THEN  -- When row has already its Order:
            EXIT WHEN coe_pointer = 0;       -- Exit Loop if back at the Top.
            coe_pointer := coe_parent_id;    -- Else, move pointer to parent.
        ELSE                                 -- When row doesn't have Order yet:
            SELECT count(*)                  -- Determines if there is any
            INTO   coe_count_child           -- child for the current parent
            FROM   PLAN_TABLE                -- pending to receive Order.
            WHERE  parent_id    = coe_pointer
            AND    remarks     is null
            AND    statement_id = coe_xplain;
            IF  coe_count_child = 0 THEN     -- If no child is pending:
                UPDATE PLAN_TABLE            -- row gets updated with Order.
                SET    remarks      = to_char(coe_next_order)
                WHERE  id           = coe_pointer
                AND    statement_id = coe_xplain;
                coe_next_order := coe_next_order + 1; -- Order gets incremented.
            ELSE                             -- If at least one pending child:
                SELECT id                    -- Move pointer to first pending
                INTO   coe_pointer           -- child.
                FROM   PLAN_TABLE
                WHERE  parent_id    = coe_pointer
                AND    remarks     is null
                AND    rownum       = 1
                AND    statement_id = coe_xplain;
            END IF;
        END IF;
    END LOOP;
END;
/

/* COLUMN section                                                             */
SET feed off;
SET numf 999,999,999;
CLEAR columns;
CLEAR breaks;
COLUMN typ FORMAT a3 HEADING 'Ope|Typ';
COLUMN remarks FORMAT a3 HEADING 'Ord';
COLUMN query_plan FORMAT a102 HEADING 'Explain Plan (coe_xplain.sql 2.2)' wor;
COLUMN table_name FORMAT a30 HEADING 'Table';
COLUMN index_name FORMAT a30 HEADING 'Index';
COLUMN last_analyzed HEADING 'Last|Analyzed';
COLUMN num_rows HEADING '(A)|Number|of rows';
COLUMN num_rows_i HEADING 'Number|of rows';
COLUMN avg_row_len FORMAT b99,999,999 HEADING 'Average|Row|Length|(bytes)';
COLUMN distinct_keys HEADING '(B)|Distinct|Keys';
COLUMN num_distinct FORMAT b99,999,999 HEADING '(C)|Distinct|Values';
COLUMN rows_count HEADING 'Rows from|Count(*)';
COLUMN index_selectivity FORMAT b999.9 HEADING 'Index|Selectivity|(B/A)';
COLUMN column_selectivity FORMAT b999.9 HEADING 'Column|Selectivity|(C/A)';
COLUMN table_num FORMAT 99 HEADING 'Tab|Num';
COLUMN owner FORMAT a10 HEADING 'Object|Owner';
COLUMN uniqueness FORMAT a10 HEADING 'Uniqueness';
COLUMN column_name FORMAT a30 HEADING 'Column (ordered by position)';
COLUMN nullable FORMAT a8 HEADING 'Nullable';
COLUMN num_nulls FORMAT b99,999,999 HEADING 'Number of|Rows with|Null value';

/* Prepares coe_xplain_tables to receive new Explain Plan                     */
DROP table coe_xplain_tables;
CREATE TABLE COE_XPLAIN_TABLES (TABLE_NUM NUMBER, OWNER VARCHAR2(30),
                                TABLE_NAME VARCHAR2(30), ROWS_COUNT NUMBER);

/* Populates coe_xplain_tables with all tables referenced in Explain Plan     */
INSERT 
    INTO COE_XPLAIN_TABLES 
SELECT 
    NULL,         -- TABLE_NUM
    OBJECT_OWNER, -- OWNER
    OBJECT_NAME,  -- TABLE_NAME
    NULL          -- ROWS_COUNT
FROM 
    PLAN_TABLE 
WHERE 
    STATEMENT_ID = 'COE_XPLAIN' 
and OPERATION    = 'TABLE ACCESS'
UNION 
SELECT 
    NULL,        -- TABLE_NUM
    TABLE_OWNER, -- OWNER
    TABLE_NAME,  -- TABLE_NAME
    NULL         -- ROWS_COUNT
FROM 
    ALL_INDEXES
WHERE 
    (OWNER, 
     INDEX_NAME) 
IN  (SELECT distinct 
         OBJECT_OWNER, 
         OBJECT_NAME 
     FROM 
         PLAN_TABLE
     WHERE 
         STATEMENT_ID = 'COE_XPLAIN' 
     and OPERATION    = 'INDEX');

UPDATE 
    COE_XPLAIN_TABLES 
SET 
    TABLE_NUM = ROWNUM;

/* Creates and executes spool file to update coe_xplain_tables with count(*)  */
SET hea off;
SPOOL coe_xplain_tables.sql;
SET lin 300;
SELECT 
    'UPDATE COE_XPLAIN_TABLES CXT SET ROWS_COUNT=(SELECT '||
    'COUNT(*) FROM '||OWNER||'.'||TABLE_NAME||') WHERE CXT.OWNER='''||OWNER||
    ''' AND CXT.TABLE_NAME='''||TABLE_NAME||''';'
FROM 
    COE_XPLAIN_TABLES;
SPOOL OFF;
@coe_xplain_tables.sql;

/* Creates spool file with Explain Plan and Statistics                        */
SET hea on;
SPOOL coe_xplain.lst;
SET lin 115;
SET term on;

/* Explain Plan                                                               */
SELECT 
    DECODE(OPERATION,'SORT','SET','VIEW','SET','HASH JOIN','S/R',
           'ROW') typ,  -- Operation Type
    REMARKS, -- Processing Order
    lpad(' ',LEVEL,rpad(' ',80,'....!'))||OPERATION||' '|| -- Operation
    DECODE(OPTIONS,NULL,'',DECODE(SUBSTR(OPTIONS,1,4),'FULL', 
           '***('||OPTIONS||')*** ','('||OPTIONS||') '))|| -- Options
    DECODE(OBJECT_OWNER,null,'','OF '''||OBJECT_OWNER||'.')|| -- Owner
    DECODE(OBJECT_NAME,null,'',OBJECT_NAME||''' ')|| -- Object Name
    DECODE(OBJECT_TYPE,null,'','('||OBJECT_TYPE||') ')|| -- Object Type
    DECODE(OPTIMIZER,null,'',DECODE(ID,0,'Opt_Mode:')||OPTIMIZER||' ')|| 
    DECODE(POSITION,NULL,'',DECODE(ID,0,'Tot_Cost:'||POSITION|| -- Total Cost
           ' CBO has been used.  '))|| -- Indication that CBO was used
    DECODE(COST,NULL,'',DECODE(ID,0,'','cost='||COST||' '))|| -- Operation Cost
    DECODE(CARDINALITY,null,'','rows='||CARDINALITY) -- Expected Rows
    query_plan -- Explain Plan
FROM 
    PLAN_TABLE 
WHERE 
    STATEMENT_ID = 'COE_XPLAIN'
CONNECT BY 
    PRIOR ID     = PARENT_ID 
AND STATEMENT_ID = 'COE_XPLAIN'
START WITH 
    ID           = 0 
AND STATEMENT_ID = 'COE_XPLAIN';
SET hea off;
SELECT 'Note: rows=expected rows (cardinality)' FROM DUAL;
SET hea on;

/* Tables */
SELECT 
    CXT.TABLE_NUM,         -- Table Number
    CXT.OWNER,             -- Table Owner
    CXT.TABLE_NAME,        -- Table Name
    CXT.ROWS_COUNT,        -- Count(*) on Table
    AT.LAST_ANALYZED,      -- Last Analyzed
    AT.NUM_ROWS,           -- Number of Rows in Table according to Analyze
    AT.AVG_ROW_LEN         -- Average Row Length in bytes
FROM 
    COE_XPLAIN_TABLES CXT, -- All Tables referenced in Explain Plan
    ALL_TABLES        AT
WHERE 
    CXT.OWNER      = AT.OWNER 
AND CXT.TABLE_NAME = AT.TABLE_NAME
ORDER BY
    CXT.TABLE_NUM;

/* Indexes */
BREAK ON TABLE_NUM SKIP 1;
SELECT 
    CXT.TABLE_NUM,          -- Table Number
    AI.OWNER,               -- Index Owner
    AI.INDEX_NAME,          -- Index Name
    AI.UNIQUENESS,          -- UNIQUE or NONUNIQUE
    AI.LAST_ANALYZED,       -- Last Analyzed
    AI.NUM_ROWS NUM_ROWS_I, -- Number of Rows in Index according to Analyze
    AI.DISTINCT_KEYS,       -- Number of Distinct code combinations in Index
    AI.DISTINCT_KEYS/NVL(AT.NUM_ROWS,1)*100 
       INDEX_SELECTIVITY    -- The higher the better.  100% is the best.
FROM 
    COE_XPLAIN_TABLES CXT,  -- All Tables referenced in Explain Plan
    ALL_INDEXES       AI, 
    ALL_TABLES        AT
WHERE 
    CXT.OWNER      = AI.TABLE_OWNER 
AND CXT.TABLE_NAME = AI.TABLE_NAME
AND CXT.OWNER      = AT.OWNER 
AND CXT.TABLE_NAME = AT.TABLE_NAME
ORDER BY 
    CXT.TABLE_NUM,
    AI.OWNER,
    AI.INDEX_NAME;

/* Index-Columns */
BREAK ON TABLE_NUM SKIP 1 ON INDEX_NAME SKIP 1;
SELECT
    CXT.TABLE_NUM,         -- Table Number
    AIC.INDEX_NAME,        -- Index Name
    AIC.COLUMN_NAME,       -- Column Name (ordered by column_position)
    DECODE(ATC.NULLABLE,'N','NOT NULL','NULL') 
        NULLABLE,          -- NULL or NOT NULL
    ATC.NUM_NULLS,         -- Number of Rows with NULL value in column
    ATC.NUM_DISTINCT,      -- Number of Distinct values on this column
    ATC.NUM_DISTINCT/NVL(AT.NUM_ROWS,1)*100 
        COLUMN_SELECTIVITY -- The higher the better.  100% is the best.
FROM 
    COE_XPLAIN_TABLES CXT, -- All Tables referenced in Explain Plan
    ALL_IND_COLUMNS   AIC, 
    ALL_TAB_COLUMNS   ATC,
    ALL_TABLES        AT
WHERE 
    CXT.OWNER       = AIC.TABLE_OWNER 
AND CXT.TABLE_NAME  = AIC.TABLE_NAME
AND AIC.TABLE_OWNER = ATC.OWNER 
AND AIC.TABLE_NAME  = ATC.TABLE_NAME
AND AIC.COLUMN_NAME = ATC.COLUMN_NAME 
AND CXT.OWNER       = AT.OWNER 
AND CXT.TABLE_NAME  = AT.TABLE_NAME
ORDER BY 
   CXT.TABLE_NUM,
   AIC.INDEX_NAME,
   AIC.COLUMN_POSITION;

/* Finishing                                                                  */
COMMIT;
SPOOL OFF;
SET sqlp SQL>;
SET sqln ON;
SET feed 6;
SET pages 24;
SET lin 115;
CLEAR columns;
CLEAR breaks;
/* The-End                                                                    */

