--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
Prompt Procedure PRC_REMOVE_COMPANY;
--
-- PRC_REMOVE_COMPANY  (Procedure)
--
CREATE OR REPLACE PROCEDURE prc_remove_company(uniqueid IN VARCHAR2, tablename IN VARCHAR2) IS
TYPE UniqueIdCollection IS TABLE OF VARCHAR2(1020);
TYPE ParentColumnNames IS TABLE OF VARCHAR2(50);
id_collection UniqueIdCollection;
CURSOR c1 IS SELECT table_name, constraint_name, constraint_type, r_constraint_name FROM user_constraints;

Type UserConstraints IS TABLE OF c1%ROWTYPE;
child_constraint_collection UserConstraints;
parent_constraint_collection UserConstraints;
TYPE ParentRec IS RECORD (tablename VARCHAR2(50), uniqueid VARCHAR2(1020));
TYPE ParentRecs IS TABLE OF ParentRec;
parent_recs ParentRecs := ParentRecs ();
TYPE ColumnNamesList IS TABLE OF VARCHAR2(50);
columnNames ColumnNamesList;
parent_id VARCHAR2(1020);
parent_columns ParentColumnNames;
delete_stmt_str VARCHAR2(200);
child_select_stmt_str VARCHAR2(1000);
parent_select_stmt_str VARCHAR2(3000);
select_stmt_str VARCHAR2(300);
--delete_stmt_str VARCHAR2(300);
x NUMBER := 1;

BEGIN
 child_select_stmt_str := 'SELECT table_name,constraint_name,constraint_type,r_constraint_name FROM user_constraints'||
 				 			' WHERE r_constraint_name IN (SELECT constraint_name FROM user_constraints ' ||
			  					   		   			  	 	   'WHERE table_name = ''' ||tablename||''')'||
											 'AND table_name NOT IN' ||
											 '(''PSP_COMPANY'')';

 --DBMS_OUTPUT.PUT_LINE(child_select_stmt_str);
 EXECUTE IMMEDIATE child_select_stmt_str BULK COLLECT INTO child_constraint_collection;

 FOR i IN 1..child_constraint_collection.count
    LOOP
    	DECLARE
			BEGIN
			--DBMS_OUTPUT.PUT_LINE(child_constraint_collection(i).table_name);
			--DBMS_OUTPUT.PUT_LINE(child_constraint_collection(i).constraint_name);
			SELECT ucc.COLUMN_NAME BULK COLLECT INTO columnNames FROM user_constraints uc inner join User_cons_columns ucc on ucc.constraint_name = uc.constraint_name
				   WHERE uc.CONSTRAINT_TYPE='R' AND uc.TABLE_NAME = child_constraint_collection(i).table_name
				   AND ucc.TABLE_NAME = child_constraint_collection(i).table_name
				   AND uc.R_CONSTRAINT_NAME=child_constraint_collection(i).r_constraint_name
				   AND ucc.COLUMN_NAME NOT IN ('REALM_ID');

			--DBMS_OUTPUT.PUT_LINE('ColumnName:' ||columnNames(1) );
			-- identify the rows to be deleted from child table
			select_stmt_str := 'SELECT ' || SUBSTR(child_constraint_collection(i).table_name, 5) || '_SEQ FROM '
							   ||child_constraint_collection(i).table_name ||' WHERE ' ||
							   columnNames(1) || '=''' || uniqueid || '''';
			FOR j IN 2..ColumnNames.count
			   LOOP
			       --DBMS_OUTPUT.PUT_LINE('ColumnName:' ||columnNames(j) );
				   select_stmt_str := select_stmt_str || ' OR ' || columnNames(j) || '=''' || uniqueid || '''';
			   END LOOP;

			--DBMS_OUTPUT.PUT_LINE(select_stmt_str);
			EXECUTE IMMEDIATE select_stmt_str BULK COLLECT INTO id_collection;


		    --DBMS_OUTPUT.PUT_LINE('Count: '||id_collection.count);

			FOR j IN 1..id_collection.count
			   LOOP
			       prc_remove_company( id_collection(j), child_constraint_collection(i).table_name);
			   END LOOP;

			EXCEPTION
	  			WHEN NO_DATA_FOUND THEN
		     		 --DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ EXCEPTION');
			 		 NULL;
			    WHEN OTHERS THEN
		     		 -- this is to delete the Association tables data
					 delete_stmt_str := 'DELETE FROM '
							   ||child_constraint_collection(i).table_name ||' WHERE ' ||
							   columnNames(1) || '=''' || uniqueid || '''';
					FOR j IN 2..ColumnNames.count
					   LOOP
					       --DBMS_OUTPUT.PUT_LINE('ColumnName:' ||columnNames(j) );
						   delete_stmt_str := delete_stmt_str || ' OR ' || columnNames(j) || '=''' || uniqueid || '''';
					   END LOOP;
					EXECUTE IMMEDIATE delete_stmt_str;
		END;

    END LOOP;

 -- identify the rows to be deleted from the parent table, exclude the tables with static data
 parent_select_stmt_str := 'SELECT table_name,constraint_name,constraint_type,r_constraint_name FROM user_constraints' ||
		 				   		   ' WHERE constraint_name IN (SELECT r_constraint_name FROM user_constraints' ||
  					   		   			  	 ' WHERE table_name = ''' ||tablename||''')' ||
											 'AND table_name NOT IN' ||
											 '(''PSP_ACTION_EVENT'', ''PSP_BANK_HOLIDAY'', ''PSP_BANK_ACCOUNT'', ''PSP_DEPOSIT_FREQUENCY'', ''PSP_EVENT_TYPE'', ''PSP_FEE'', ''PSP_FINANCIAL_TXN_ACTION'', ''PSP_FUNDING_MODEL'',' ||
											 '''PSP_INTUIT_BANK_ACCOUNT'', ''PSP_INTUIT_BANK_ACC_TXN_TYPE'', ''PSP_INTUIT_BA_BT_FT'', ''PSP_LEDGER_ACCOUNT'',' ||
											 '''PSP_LEDGER_ACCOUNT_ACTION'', ''PSP_NACHAFILE'', ''PSP_OFFER'', ''PSP_OFFERING'', ''PSP_OFFERING_SVCCHG'', ''PSP_OFFERING_SVCCHG_GRP'', ''PSP_OFFERING_SVC_ASSOC'', ''PSP_OFFER_SVCCHG_ASSOC'', ''PSP_OFFLOAD_BATCH'', ''PSP_OFFLOAD_GROUP'', ''PSP_PAYROLL_FREQUENCY'', ''PSP_PAYROLL_RUN_ACTION'',' ||
											 '''PSP_POSTING_RULE'', ''PSP_SERVICE'', ''PSP_SERVICE_STATUS'', ''PSP_SVCSTAT_SRCSYS_ASSOC'', ''PSP_SVCSTAT_SVC_ASSOC'', ''PSP_SVCSTAT_SYSCAP_ASSOC'', ''PSP_SVCSTAT_TXNTYPE_ASSOC'', ''PSP_SYSTEM_CAPABILITY'',' ||
											 '''PSP_SOURCE_PAYROLL_PARAMETER'', ''PSP_SOURCE_SYSTEM'', ''PSP_SYSTEM_PARAMETER'', ''PSP_TRANSACTION_STATE'',' ||
											 '''PSP_TRANSACTION_TYPE'', ''PSP_OFFERING'', ''PSP_COMPANY'')';


 --DBMS_OUTPUT.PUT_LINE(parent_select_stmt_str);
 EXECUTE IMMEDIATE parent_select_stmt_str BULK COLLECT INTO parent_constraint_collection;
 --DBMS_OUTPUT.PUT_LINE('EXECUTED parent select');

 FOR j IN 1..parent_constraint_collection.count
    LOOP
	DECLARE
		BEGIN

    	--identify the parent column names
		SELECT ucc.COLUMN_NAME BULK COLLECT INTO parent_columns FROM user_constraints uc inner join User_cons_columns ucc on ucc.constraint_name = uc.constraint_name
		   WHERE uc.CONSTRAINT_TYPE='R' AND uc.TABLE_NAME = tablename
		   AND ucc.TABLE_NAME = tablename
		   AND uc.R_CONSTRAINT_NAME=parent_constraint_collection(j).constraint_name;

		FOR k IN 1..parent_columns.count
 	 		LOOP
	 			--DBMS_OUTPUT.PUT_LINE('Parent Column: ' || parent_columns(k));
				select_stmt_str := 'SELECT '|| parent_columns(k) || ' FROM ' ||
						   tablename || ' WHERE ' || SUBSTR(tablename, 5) || '_SEQ = ''' || uniqueid || '''';
				--DBMS_OUTPUT.PUT_LINE(select_stmt_str);
			    EXECUTE IMMEDIATE select_stmt_str INTO parent_id;

				--DBMS_OUTPUT.PUT_LINE(parent_id);
				--DBMS_OUTPUT.PUT_LINE('Count: ' || parent_recs.count);
				--DBMS_OUTPUT.PUT_LINE('X: ' || x);
				parent_recs.EXTEND;
				--DBMS_OUTPUT.PUT_LINE('Count: ' || parent_recs.count);
				parent_recs(x).tablename := parent_constraint_collection(j).table_name;
				parent_recs(x).uniqueid := parent_id;
				--DBMS_OUTPUT.PUT_LINE('Assigning parent recs: ' || parent_recs(x).tablename);
				x := x + 1;
			END LOOP;

		EXCEPTION
	  			WHEN NO_DATA_FOUND THEN
		     		 --DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ EXCEPTION');
			 		 NULL;
		END;

	END LOOP;

 -- delete the current entry
 delete_stmt_str := 'DELETE FROM ' ||  tablename ||
	                ' WHERE ' || SUBSTR(tablename, 5) || '_SEQ=''' || uniqueid || '''';
 --DBMS_OUTPUT.PUT_LINE(delete_stmt_str);
 EXECUTE IMMEDIATE delete_stmt_str;
 --DBMS_OUTPUT.PUT_LINE('#### ' || parent_recs.count);
 -- delete the entries from parent table
 FOR l IN 1..parent_recs.count
 	 LOOP
	 	prc_remove_company( parent_recs(l).uniqueid, parent_recs(l).tablename);
	 END LOOP;

 EXCEPTION
		  WHEN NO_DATA_FOUND
		     THEN
			     --DBMS_OUTPUT.PUT_LINE('@@@@@@@@@ EXCEPTION');
				 RETURN;

 END;

/

SHOW ERRORS;

Prompt Column MIGRATION_STATUS;
ALTER TABLE PSP_COMPANY
 ADD (MIGRATION_STATUS  VARCHAR2(255 CHAR));

Prompt Column MIGRATE_IND;
ALTER TABLE PSP_COMPANY DROP COLUMN MIGRATE_IND;

ALTER TABLE PSP_COMPANY
 ADD CONSTRAINT C_PSP_COMPANY1
 CHECK (MIGRATION_STATUS IN('MigratingToAS400', 'MigratedToAS400', 'MigratingFromAS400', 'MigratedFromAS400', 'NotAMigratedCompany', 'MigratedFromPSE'));

 select 'finished DBUpgradeFrom_1.9.3.0_To_1.9.4.0.sql ' || to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') from dual