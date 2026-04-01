CREATE OR REPLACE PACKAGE BODY PK_DIYDDTOPSP_CONTROLLER AS
/******************************************************************************
   NAME:    PK_DIYDDTOPSP_CONTROLLER
   UPDATED: 10.22.2008 11:00 AM  
   PURPOSE: Provide an interface for Java to manage the migration state
            of DIY companies being transferred from the AS/400 to PSP.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        02.13.2008  EMR              Created this package body.
   1.1        06.12.2008  EMR              migrated from POS pc to 10g server,
                                             also added updated migration tables.
   1.2        06.27.2008  EMR              added additional controller apis
                                             for e2e migration.
   1.2.1      07.14.2008  EMR              added support for validation and 
                                             aligned framework with IOP      
   1.2.2      07.16.2008  EMR              allow multiple runs, filter out
                                             prior runs for get next, initialize
                                             and summary                    
   1.2.3      07.17.2008  EMR              added as400 migration queue table
                                             to initialize.
   1.2.4      07.18.2008  EMR              used new range for raise app err                                                                                                                                             
   1.2.5      07.23.2008  EMR              added update to as400 on cp or err   
   1.2.6      07.30.2008  EMR              removed PRAGMA AUTONOMOUS TXN, do
                                             not use. messes up gateway.
   1.2.7      07.30.2008  EMR              fixed as400 update time format.                                             
   1.2.8      09.15.2008  EMR              changed get next company cursor   
   1.2.9      09.30.2008  EMR              modified initialize query
   1.2.10     10.06.2008  EMR              fixed company name length in initialize
   1.2.11     10.22.2008  EMR              added purge all cache to initialize.
******************************************************************************/

/*
   ***************************************************************************
   PURPOSE: cursors

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -----------------------------------
   1.0        02.20.2008  EMR			   created GetCompanyToMigrate
   1.1        07.01.2008  EMR              removed order by
   ***************************************************************************
*/

  -- ------------------------------------------------------------------------
  -- CURSORS: return a list of companies ready to migrate from DIY to PSP
  -- ------------------------------------------------------------------------

  /* OLD CURSOR THAT DOES FULL TABLE SCANS

  CURSOR cur_companies_to_migrate
    (p_Interested_StateCD          IN  COMPANY_MIGRATION.MIGRATION_STATE_CD%TYPE)
  IS
  
    -- company will transition from current state to new state
	-- random queue
    -- removed order by because list will be 10k long and a sort every second
    -- with a list that big is unnecessary.  order in this case does not matter.
    
    SELECT TO_NUMBER(a.SOURCE_DB_COMPANY_ID) AS DIY_COMPANY_USERID,
           a.MIGRATION_STATE_CD              AS CURRENT_STATE
      FROM COMPANY_MIGRATION a
     WHERE a.MIGRATION_STATE_CD              = p_Interested_StateCD
       AND TRUNC(a.MIGRATION_SCHEDULED_DATE) = TRUNC(SYSDATE)
       FOR UPDATE OF a.MIGRATION_STATE_CD;

  */       

  CURSOR cur_companies_to_migrate
    (p_Interested_StateCD          IN  COMPANY_MIGRATION.MIGRATION_STATE_CD%TYPE)
  IS
    -- company will transition from current state to new state
	-- random queue
    -- removed order by because list will be 10k long and a sort every second
    -- with a list that big is unnecessary.  order in this case does not matter.
    --
    -- 09.15.2008 EMR
    --   changed query that was taking 4 seconds to now milliseconds.  same
    --   approach now between IOP and DIY.  remember to add index to make
    --   this query effective.
     
    SELECT TO_NUMBER(SOURCE_DB_COMPANY_ID) AS DIY_COMPANY_USERID,
           MIGRATION_STATE_CD              AS CURRENT_STATE
      FROM COMPANY_MIGRATION
     WHERE MIGRATION_STATE_CD = p_Interested_StateCD
       AND SOURCE_DB_COMPANY_ID = (
             SELECT MIN(SOURCE_DB_COMPANY_ID)
               FROM COMPANY_MIGRATION
              WHERE MIGRATION_STATE_CD = p_Interested_StateCD
           )
     FOR UPDATE OF MIGRATION_STATE_CD;


/*
   ***************************************************************************
   PURPOSE: private apis to support this package

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -----------------------------------
   1.0        06.30.2008  EMR              created procedure for state update
   1.0.1      07.16.2008  EMR              added queue check
   1.0.2      10.14.2008  EMR              added local cache for payroll
   ***************************************************************************
*/

  PROCEDURE ExecuteStateTransition (
    p_DB_COMPANY_ID                IN  VARCHAR2,
    p_NEW_STATE_CD                 IN  VARCHAR2
  )
  IS
    v_temp_actual_date             COMPANY_MIGRATION.MIGRATION_ACTUAL_DATE%TYPE;                    
    
  BEGIN

    -- only stamp done when done.
    IF (p_NEW_STATE_CD = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD) OR
       (p_NEW_STATE_CD = PK_DIYDDTOPSP_CONST.gc_Error_StateCD)    THEN
      v_temp_actual_date := SYSDATE;
    ELSE
      v_temp_actual_date := NULL;
    END IF;
       
    UPDATE COMPANY_MIGRATION
	   SET Migration_State_CD    = p_NEW_STATE_CD,
           Migration_Actual_Date = v_temp_actual_date 
	 WHERE SOURCE_DB_COMPANY_ID = p_DB_COMPANY_ID;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE;
  END;


  PROCEDURE SetMigrationStateOnAS400 (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER,        -- IQCLIENT.CLI_USERID
    p_NEW_STATE_CD                 IN  VARCHAR2
  )
  IS
    v_temp_date                    NUMBER;
    
  BEGIN

    -- UPDATE THE AS400
    -- date format is 20080718020202
   
    v_temp_date := TO_NUMBER(TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS'));

    UPDATE DIY_MIG_QUEUE
       SET PSP_MIGRATIONS_STATE = p_NEW_STATE_CD,
           PSP_MIGRATE_DATE     = v_temp_date
     WHERE PSP_USERID = p_DIY_SRC_COMPANY_ID;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE;
  END;


  FUNCTION FN_GET_PRIOR_MIGRATION_STATE (
    p_DB_COMPANY_ID                IN  VARCHAR2
  ) RETURN COMPANY_MIGRATION.MIGRATION_STATE_CD%TYPE
  IS
    v_temp_state                   COMPANY_MIGRATION.MIGRATION_STATE_CD%TYPE;
    
  BEGIN

    BEGIN
    
      SELECT Migration_State_CD
        INTO v_temp_state
        FROM COMPANY_MIGRATION
       WHERE Source_DB_Company_ID = p_DB_COMPANY_ID;
       
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        v_temp_state := NULL;  
      WHEN OTHERS THEN
        RAISE;
    END;
    
    RETURN v_temp_state;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE;
  END;


/*
   ***************************************************************************
   PURPOSE: public apis to manage the state of migrating DIY company

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -----------------------------------
   1.0        02.20.2008  EMR			   created GetCompanyToMigrate
   1.1        06.27.2008  EMR              created SetCompanyToValidating
   ***************************************************************************
*/

  PROCEDURE GetCompanyToMigrate (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_DIY_SRC_COMPANY_ID           OUT NUMBER         -- IQCLIENT.CLI_USERID
  )
  IS
    rt_Company_To_Migrate          cur_companies_to_migrate%ROWTYPE;

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

	-- initialize cursor
    IF (cur_companies_to_migrate%ISOPEN) THEN
	  CLOSE cur_companies_to_migrate;
	END IF;

    OPEN cur_companies_to_migrate (
      PK_DIYDDTOPSP_CONST.gc_Ready_StateCD);

    FETCH cur_companies_to_migrate
	 INTO rt_Company_To_Migrate;

	-- return the company ready to migrate
    IF (cur_companies_to_migrate%FOUND) THEN

      -- AS400 userid is actually a number
      -- thus the cursor does a character to number conversion
      
	  p_DIY_SRC_COMPANY_ID := rt_Company_To_Migrate.DIY_COMPANY_USERID;
      
      ExecuteStateTransition (
        TO_CHAR(rt_Company_To_Migrate.DIY_COMPANY_USERID),  -- p_DB_COMPANY_ID
        PK_DIYDDTOPSP_CONST.gc_Migrating_StateCD            -- p_NEW_STATE_CD
      );      
      
      -- initialize the the key values.  more for unit testing.    
      UPDATE COMPANY_MIGRATION
	     SET Target_DB_Company_ID  = NULL,
             Migration_Actual_Date = NULL 
	   WHERE SOURCE_DB_COMPANY_ID  = TO_CHAR(rt_Company_To_Migrate.DIY_COMPANY_USERID);
      
    ELSE
	  p_DIY_SRC_COMPANY_ID := NULL;
      p_RETURN_CD          := -20050;
      p_RETURN_MSG         := SUBSTR ( ('Error: no DIY companies found to migrate.'), 1, 150);
	END IF;

    CLOSE cur_companies_to_migrate;

	COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      IF (cur_companies_to_migrate%ISOPEN) THEN
        CLOSE cur_companies_to_migrate;
      END IF;
      ROLLBACK;
      p_RETURN_CD  := -20051;
      p_RETURN_MSG := SUBSTR ( ('Error getting next company to migrate. ' || SQLERRM), 1, 150);
  END;


  PROCEDURE SetCompanyToSyncing (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER,        -- IQCLIENT.CLI_USERID
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    
  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    -- this is a step that java has to initiate to copy all straggler data
    -- that was created on the as400.  here it is just a state transition.

    ExecuteStateTransition (
      TO_CHAR(p_DIY_SRC_COMPANY_ID),             -- p_DB_COMPANY_ID
      PK_DIYDDTOPSP_CONST.gc_Syncing_StateCD     -- p_NEW_STATE_CD
    );      

	COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      p_RETURN_CD  := -20051;
      p_RETURN_MSG := SUBSTR ( ('Error setting company to synchronizing state for company ' || 
                                 p_DIY_SRC_COMPANY_ID || '. ' ||SQLERRM), 1, 500);
  END;
  

  PROCEDURE ValidateMigratedCompany (
    p_DIY_COMPANY_USERID           IN  NUMBER,       -- SOURCE ID: CLI_USERID
	p_PSP_COMPANY_GUID             IN  VARCHAR2,     -- TARGET ID: COMPANY_SEQ
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
	v_temp_RC                      NUMBER;
	v_temp_RC_MSG                  VARCHAR2(500);
    
  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    ExecuteStateTransition (
      TO_CHAR(p_DIY_COMPANY_USERID),               -- p_DB_COMPANY_ID
      PK_DIYDDTOPSP_CONST.gc_Validating_StateCD    -- p_NEW_STATE_CD
    );      

	PK_DIYDDTOPSP_VALIDATION.PR_Validate_DIY_Company (
      p_DIY_COMPANY_USERID,
	  p_PSP_COMPANY_GUID,
	  v_temp_RC,
	  v_temp_RC_MSG
    );

	IF (v_temp_RC = 0) THEN
      
      -- save the PSP id so that we can do global validations later.    
      UPDATE COMPANY_MIGRATION
	     SET Target_DB_Company_ID = p_PSP_COMPANY_GUID 
	   WHERE SOURCE_DB_COMPANY_ID = TO_CHAR(p_DIY_COMPANY_USERID);
    
	ELSIF (v_temp_RC = -1) THEN

      p_RETURN_CD  := -1;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Data inconsistencies were found for DIY company, AS400 ID =' ||
		 p_DIY_COMPANY_USERID || '. ' || 
         v_temp_RC || ': ' || v_temp_RC_MSG
		),
		1,
		500
	  );
      
    ELSIF (v_temp_RC = -2) THEN
    
      RAISE_APPLICATION_ERROR (
        pc_raise_app_err_cd,
        'UNEXPECTED ERROR WHILE VALIDATING DIY company, AS400 ID = ' ||
		 p_DIY_COMPANY_USERID || '. ' || v_temp_RC_MSG,
        FALSE
      );

	END IF;

    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      p_RETURN_CD  := -20051;
      p_RETURN_MSG := SUBSTR ( ('Error setting company to validating state for company ' || 
                                 p_DIY_COMPANY_USERID || '. ' ||SQLERRM), 1, 500);
  END;


  PROCEDURE SetCompanyMigrationToComplete (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER,        -- IQCLIENT.CLI_USERID
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    
  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    ExecuteStateTransition (
      TO_CHAR(p_DIY_SRC_COMPANY_ID),             -- p_DB_COMPANY_ID
      PK_DIYDDTOPSP_CONST.gc_Complete_StateCD    -- p_NEW_STATE_CD
    );    
    
    COMMIT; 
   
  
    SetMigrationStateOnAS400 (
      p_DIY_SRC_COMPANY_ID,
      PK_DIYDDTOPSP_CONST.gc_PSPMIGQUE_Complete_CD
    );
    
    COMMIT;
    
    
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      p_RETURN_CD  := -20051;
      p_RETURN_MSG := SUBSTR ( ('Error setting company to complete state for company ' || 
                                 p_DIY_SRC_COMPANY_ID || '. ' ||SQLERRM), 1, 500);
  END;
  

  PROCEDURE SetCompanyMigrationToError (
    p_DIY_SRC_COMPANY_ID           IN  NUMBER,        -- IQCLIENT.CLI_USERID
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    
  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    ExecuteStateTransition (
      TO_CHAR(p_DIY_SRC_COMPANY_ID),             -- p_DB_COMPANY_ID
      PK_DIYDDTOPSP_CONST.gc_Error_StateCD       -- p_NEW_STATE_CD
    );      
    
    COMMIT;
    
     
	SetMigrationStateOnAS400 (
      p_DIY_SRC_COMPANY_ID,
      PK_DIYDDTOPSP_CONST.gc_PSPMIGQUE_Error_CD
    );
    
    COMMIT;    

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      p_RETURN_CD  := -20051;
      p_RETURN_MSG := SUBSTR ( ('Error setting company to error state for company ' || 
                                 p_DIY_SRC_COMPANY_ID || '. ' ||SQLERRM), 1, 500);
  END;


  PROCEDURE ValidateEntireMigration (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
	v_temp_RC                      NUMBER;
	v_temp_RC_MSG                  VARCHAR2(500);

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

	PK_DIYDDTOPSP_VALIDATION.PR_Validate_DIY_Migration (
	  v_temp_RC,
	  v_temp_RC_MSG
    );

	IF (v_temp_RC <> 0) THEN

      p_RETURN_CD  := -1;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Data inconsistencies were found for the DIY DD migration.' || v_temp_RC_MSG
		),
		1,
		500
	  );

	END IF;
    
    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -20051;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Unexpected error while validating DIY DD migrated data. ' ||
		 SQLERRM
		),
		1,
		500
	  );
  END;


  PROCEDURE GetMigrationSummary (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    v_start_time                   DATE;
    v_last_migration_time          DATE;
    v_run_time                     NUMBER;

    v_total_to_migrate             NUMBER;
    v_pct_done                     NUMBER;
    v_num_remaining                NUMBER;
    v_num_threads                  NUMBER;
    
    v_num_ready                    NUMBER;
    v_num_migrating                NUMBER;
    v_num_syncing                  NUMBER;
    v_num_validating               NUMBER;
    v_num_complete                 NUMBER;
    v_num_error                    NUMBER;    

    v_min_mig_sec                  NUMBER;
    v_avg_mig_sec                  NUMBER;
    v_max_mig_sec                  NUMBER;
    v_avg_val_sec                  NUMBER;    
    
    v_hr_remaining                 NUMBER;
    v_min_remaining                NUMBER;
    v_when_done                    VARCHAR2(100);
    
  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';
    
    /* 
       DESIGN NOTES
       ----------------------------------------------
         compute total - migrated = # remaining
         compute % complete = migrated / total
         compute num errors
         compute min, max, average migration
         compute timing remaining = min/max/avg * # remaining
         compute clock time = min/max/avg timing remaining + sysdate
    
         1 day = 86400 seconds (24 hr x 60 min x 60 sec), 1440 minutes (24hr x 60 min), 24 hours
    */
  
    DBMS_OUTPUT.PUT_LINE ('*************************************************************');
    DBMS_OUTPUT.PUT_LINE ('*                     MIGRATION SUMMARY                     *');
    DBMS_OUTPUT.PUT_LINE ('*************************************************************');

    -- need to start with the very first company migrated.  there could be a
    -- delay between initialization and start of migration.

    -- added join to parent to filter out prior runs
    
    SELECT (
            SELECT MIN(a.Z_INS_DTTM)
              FROM MIGRATION_STATE_HIST a,
                   COMPANY_MIGRATION    b
             WHERE a.COMPANY_MIGRATION_GSEQ          = b.COMPANY_MIGRATION_GSEQ
               AND TRUNC(b.Migration_Scheduled_Date) = TRUNC(SYSDATE) 
               AND a.MIGRATION_STATE_CD              = PK_DIYDDTOPSP_CONST.gc_Migrating_StateCD
           ) AS Mig_Start_Time,
           (
            SELECT MAX(a.Z_INS_DTTM)
              FROM MIGRATION_STATE_HIST a,
                   COMPANY_MIGRATION    b
             WHERE a.COMPANY_MIGRATION_GSEQ          = b.COMPANY_MIGRATION_GSEQ
               AND TRUNC(b.Migration_Scheduled_Date) = TRUNC(SYSDATE)
               AND (
                    a.MIGRATION_STATE_CD = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD OR
                    a.MIGRATION_STATE_CD = PK_DIYDDTOPSP_CONST.gc_Error_StateCD
                   )
           ) AS Latest_Mig_Time
      INTO v_start_time,
           v_last_migration_time
      FROM DUAL;
      
    SELECT ROUND(((v_last_migration_time - v_start_time)*24), 2)
      INTO v_run_time
      FROM DUAL;
    
    DBMS_OUTPUT.PUT_LINE (CHR(0));
    DBMS_OUTPUT.PUT_LINE ('Current time                      : ' || TO_CHAR (SYSDATE,               'MM/DD/YY HH:MI AM'));
    DBMS_OUTPUT.PUT_LINE (CHR(0));    
    DBMS_OUTPUT.PUT_LINE ('First migration                   : ' || TO_CHAR (v_start_time,          'MM/DD/YY HH:MI AM'));
    DBMS_OUTPUT.PUT_LINE ('Latest migration                  : ' || TO_CHAR (v_last_migration_time, 'MM/DD/YY HH:MI AM'));
    DBMS_OUTPUT.PUT_LINE ('Runtime so far (hrs)              : ' || v_run_time);
    
    
    SELECT MAX(DECODE(
             Migration_State_CD,
             PK_DIYDDTOPSP_CONST.gc_Ready_StateCD, MIG_STATE_CNT,
             0
           )) AS READY_COUNT,
           MAX(DECODE(
             Migration_State_CD,
             PK_DIYDDTOPSP_CONST.gc_Migrating_StateCD, MIG_STATE_CNT,
             0
           )) AS MIGRATING_COUNT,
           MAX(DECODE(
             Migration_State_CD,
             PK_DIYDDTOPSP_CONST.gc_Syncing_StateCD, MIG_STATE_CNT,
             0
           )) AS SYNCING_COUNT,
           MAX(DECODE(
             Migration_State_CD,
             PK_DIYDDTOPSP_CONST.gc_Validating_StateCD, MIG_STATE_CNT,
             0
           )) AS VALIDATING_COUNT,
           MAX(DECODE(
             Migration_State_CD,
             PK_DIYDDTOPSP_CONST.gc_Complete_StateCD, MIG_STATE_CNT,
             0
           )) AS COMPLETE_COUNT,
           MAX(DECODE(
             Migration_State_CD,
             PK_DIYDDTOPSP_CONST.gc_Error_StateCD, MIG_STATE_CNT,
             0
           )) AS ERROR_COUNT
      INTO v_num_ready,
           v_num_migrating,
           v_num_syncing,
           v_num_validating,
           v_num_complete,
           v_num_error 
      FROM (      
            SELECT Migration_State_CD,
                   count(*) AS MIG_STATE_CNT
              FROM COMPANY_MIGRATION
             WHERE TRUNC(Migration_Scheduled_Date) = TRUNC(SYSDATE)
             GROUP 
                BY Migration_State_CD
           );

    SELECT v_num_ready      +  
           v_num_migrating  + 
           v_num_syncing    +
           v_num_validating + 
           v_num_complete   + 
           v_num_error
      INTO v_total_to_migrate
      FROM DUAL;
      
    SELECT ROUND(((v_num_complete + v_num_error) / v_total_to_migrate)*100, 2)
      INTO v_pct_done
      FROM DUAL;
    
    SELECT v_num_migrating + v_num_syncing + v_num_validating
      INTO v_num_threads
      FROM DUAL;
    
    v_num_remaining := v_num_ready; 

    DBMS_OUTPUT.PUT_LINE (CHR(0));
    DBMS_OUTPUT.PUT_LINE ('Total companies to migrate        : ' || v_total_to_migrate );
    DBMS_OUTPUT.PUT_LINE ('Percent complete                  : ' || v_pct_done || '%'  );
    DBMS_OUTPUT.PUT_LINE ('  Number migrated successfully    : ' || v_num_complete     ); -- cp
    DBMS_OUTPUT.PUT_LINE ('  Number migrated with errors     : ' || v_num_error        ); -- err   
    DBMS_OUTPUT.PUT_LINE ('  Number waiting to migrate       : ' || v_num_ready        ); -- rdy
    DBMS_OUTPUT.PUT_LINE ('  Number migrated but syncing     : ' || v_num_syncing      ); -- sync    
    DBMS_OUTPUT.PUT_LINE ('  Number migrated but validating  : ' || v_num_validating   ); -- val
    DBMS_OUTPUT.PUT_LINE ('  Number of migration threads     : ' || v_num_threads      ); -- mig + sync + val
  
    
    -- migration time in seconds
    -- to eliminate prior runs, only select items that were scheduled for today

    SELECT ROUND(MIN(MIG_SECS), 2),
           ROUND(AVG(MIG_SECS), 2),
           ROUND(MAX(MIG_SECS), 2),
           ROUND(AVG(VAL_SECS), 2)
      INTO v_min_mig_sec,
           v_avg_mig_sec,
           v_max_mig_sec,
           v_avg_val_sec         
      FROM (
            SELECT ((VALIDATING_TIME - MIGRATING_TIME )*24*60*60) AS MIG_SECS,
                   ((COMPLETE_TIME   - VALIDATING_TIME)*24*60*60) AS VAL_SECS
              FROM (
                    SELECT * 
                      FROM (
                            SELECT a.Company_Migration_Gseq,
                                   MAX(DECODE (
                                     a.Migration_State_Cd,
                                     PK_DIYDDTOPSP_CONST.gc_Ready_StateCD, a.z_ins_dttm
                                   )) AS READY_TIME,
                                   MAX(DECODE (
                                     a.Migration_State_Cd,
                                     PK_DIYDDTOPSP_CONST.gc_Migrating_StateCD, a.z_ins_dttm
                                   )) AS MIGRATING_TIME,
                                   MAX(DECODE (
                                     a.Migration_State_Cd,
                                     PK_DIYDDTOPSP_CONST.gc_Validating_StateCD, a.z_ins_dttm
                                   )) AS VALIDATING_TIME,
                                   MAX(DECODE (
                                     a.Migration_State_Cd,
                                     PK_DIYDDTOPSP_CONST.gc_Complete_StateCD,  a.z_ins_dttm,
                                     PK_DIYDDTOPSP_CONST.gc_Error_StateCD,     a.z_ins_dttm
                                   )) AS COMPLETE_TIME
                              FROM MIGRATION_STATE_HIST a,
                                   COMPANY_MIGRATION    b
                             WHERE a.COMPANY_MIGRATION_GSEQ          = b.COMPANY_MIGRATION_GSEQ
                               AND TRUNC(b.Migration_Scheduled_Date) = TRUNC(SYSDATE) 
                             GROUP
                                BY a.Company_Migration_Gseq
                           )
                     WHERE COMPLETE_TIME  IS NOT NULL
                       AND MIGRATING_TIME IS NOT NULL
                   )
           );
         
    DBMS_OUTPUT.PUT_LINE (CHR(0));
    DBMS_OUTPUT.PUT_LINE ('Fastest migration  time (seconds) : ' || ROUND(v_min_mig_sec));
    DBMS_OUTPUT.PUT_LINE ('Average migration  time (seconds) : ' || ROUND(v_avg_mig_sec));
    DBMS_OUTPUT.PUT_LINE ('Longest migration  time (seconds) : ' || ROUND(v_max_mig_sec));
    DBMS_OUTPUT.PUT_LINE ('Average validation time (seconds) : ' || ROUND(v_avg_val_sec));
    
    
    -- if num remaining is 0 then you are done.
    -- if num of threas is 0 then the java migration stopped and would cause
    -- a divide by 0 error.
    -- bottom line something must always be migrating to compute an end time.
    
    IF (v_num_threads > 0) AND (v_num_remaining > 0) THEN
     
      -- BEST CASE
      SELECT TRUNC (hr_remain)                                                 AS "Hours Remaining",
             ROUND((hr_remain - TRUNC(hr_remain)) * 60)                        AS "Minutes Remaining",
             TO_CHAR(SYSDATE + ROUND(min_remain) / 1440, 'MM/DD/YY HH:MI AM')  AS "When Done"
        INTO v_hr_remaining,
             v_min_remaining,
             v_when_done
        FROM (  
              SELECT (tot_remaining * avg_migrate_sec) / 60      AS min_remain,
                     (tot_remaining * avg_migrate_sec) / 60 / 60 AS hr_remain
                FROM (
                      SELECT v_num_remaining/v_num_threads AS tot_remaining,    -- variable
                             v_min_mig_sec                 AS avg_migrate_sec   -- variable
                        FROM DUAL
                     )  
             );
      
      DBMS_OUTPUT.PUT_LINE (CHR(0));
      DBMS_OUTPUT.PUT_LINE ('FASTEST CASE SCENARIO ....');
      DBMS_OUTPUT.PUT_LINE ('Hours remaining                   : ' || v_hr_remaining);
      DBMS_OUTPUT.PUT_LINE ('Minutes remaining                 : ' || v_min_remaining);
      DBMS_OUTPUT.PUT_LINE ('When Done                         : ' || v_when_done);
      
        
      -- AVERAGE CASE
      SELECT TRUNC (hr_remain)                                                 AS "Hours Remaining",
             ROUND((hr_remain - TRUNC(hr_remain)) * 60)                        AS "Minutes Remaining",
             TO_CHAR(SYSDATE + ROUND(min_remain) / 1440, 'MM/DD/YY HH:MI AM')  AS "When Done"
        INTO v_hr_remaining,
             v_min_remaining,
             v_when_done
        FROM (  
              SELECT (tot_remaining * avg_migrate_sec) / 60      AS min_remain,
                     (tot_remaining * avg_migrate_sec) / 60 / 60 AS hr_remain
                FROM (
                      SELECT v_num_remaining/v_num_threads AS tot_remaining,    -- variable
                             v_avg_mig_sec                 AS avg_migrate_sec   -- variable
                        FROM DUAL
                     )  
             );
      
      DBMS_OUTPUT.PUT_LINE (CHR(0));
      DBMS_OUTPUT.PUT_LINE ('AVERAGE CASE SCENARIO ....');
      DBMS_OUTPUT.PUT_LINE ('Hours remaining                   : ' || v_hr_remaining);
      DBMS_OUTPUT.PUT_LINE ('Minutes remaining                 : ' || v_min_remaining);
      DBMS_OUTPUT.PUT_LINE ('When Done                         : ' || v_when_done);
      
      
      -- LONGEST CASE
      SELECT TRUNC (hr_remain)                                                 AS "Hours Remaining",
             ROUND((hr_remain - TRUNC(hr_remain)) * 60)                        AS "Minutes Remaining",
             TO_CHAR(SYSDATE + ROUND(min_remain) / 1440, 'MM/DD/YY HH:MI AM')  AS "When Done"
        INTO v_hr_remaining,
             v_min_remaining,
             v_when_done
        FROM (  
              SELECT (tot_remaining * avg_migrate_sec) / 60      AS min_remain,
                     (tot_remaining * avg_migrate_sec) / 60 / 60 AS hr_remain
                FROM (
                      SELECT v_num_remaining/v_num_threads AS tot_remaining,    -- variable
                             v_max_mig_sec                 AS avg_migrate_sec   -- variable
                        FROM DUAL
                     )  
             );
      
      DBMS_OUTPUT.PUT_LINE(CHR(0));
      DBMS_OUTPUT.PUT_LINE ('LONGEST CASE SCENARIO ....');
      DBMS_OUTPUT.PUT_LINE ('Hours remaining                   : ' || v_hr_remaining);
      DBMS_OUTPUT.PUT_LINE ('Minutes remaining                 : ' || v_min_remaining);
      DBMS_OUTPUT.PUT_LINE ('When Done                         : ' || v_when_done);

    ELSE
    
      IF (v_pct_done = 100) THEN
        DBMS_OUTPUT.PUT_LINE(CHR(0));
        DBMS_OUTPUT.PUT_LINE('Hey dude, it appears that the migration has finished, go home.');
      ELSIF (v_pct_done < 100) THEN
        DBMS_OUTPUT.PUT_LINE(CHR(0));
        DBMS_OUTPUT.PUT_LINE('Hey dude, it appears that the java app has stopped migrating, call Steve.');
      ELSE 
        DBMS_OUTPUT.PUT_LINE(CHR(0));
        DBMS_OUTPUT.PUT_LINE('Hey dude, how can you be more than 100 percent done?' || v_pct_done );
      END IF;

    END IF;

    DBMS_OUTPUT.PUT_LINE(CHR(0));
    DBMS_OUTPUT.PUT_LINE('Seeing a smile on Andy''s face when migration is done : priceless');    
	
  EXCEPTION
    WHEN OTHERS THEN
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Unexpected error while summarizing IOP migration. ' ||
		 SQLERRM
		),
		1,
		500
	  );
  END;


  PROCEDURE InitializeMigrationQueue (
    p_MIGRATION_PHASE_ID           IN  COMPANY_MIGRATION.MIGRATION_PHASE_ID%TYPE,
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    v_prior_migration_state        COMPANY_MIGRATION.MIGRATION_STATE_CD%TYPE;
    b_warning_complete_ind         BOOLEAN := FALSE;  -- a company has migrated

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';
    
    -- 10.22.2008 EMR
    -- wipe the local cache clean.

    PK_DIYDDTOPSP_UTILS.PR_PURGE_ALL_TEMP_CACHE;
    
	
    FOR i IN (
      SELECT TO_CHAR(AS400_COMPANY_ID) AS AS400_COMPANY_ID,
             TRIM(COMPANY_NAME)        AS COMPANY_NAME
        FROM (
              SELECT /*+ NO_MERGE */
                     a.PSP_USERID     AS AS400_COMPANY_ID,
                     b.CLI_LegalName  AS COMPANY_NAME
                FROM DIY_MIG_QUEUE a,
                     DIY_IQCLIENT  b
               WHERE a.PSP_USERID           = b.CLI_USERID
                 AND a.PSP_MIGRATIONS_STATE = PK_DIYDDTOPSP_CONST.gc_PSPMIGQUE_Ready_CD
             )         
    )
    LOOP
    
      v_prior_migration_state := FN_GET_PRIOR_MIGRATION_STATE (i.AS400_COMPANY_ID);
      
      IF (v_prior_migration_state IS NULL) THEN

        -- has not migrated so insert the company
        INSERT INTO COMPANY_MIGRATION (
          COMPANY_MIGRATION_GSEQ, 
  	      MIGRATION_STATE_CD, 
  	      SOURCE_DB_COMPANY_ID, 
          COMPANY_LEGAL_NAME, 
  	      DD_SERVICE_CD, 
  	      MIGRATION_PHASE_ID, 
          MIGRATION_SCHEDULED_DATE,
          MIGRATION_ACTUAL_DATE,
          TARGET_DB_COMPANY_ID 
        )
        VALUES (
           SEQ_COMPANY_MIGRATION.NEXTVAL,
           PK_DIYDDTOPSP_CONST.gc_Ready_StateCD,
           i.AS400_COMPANY_ID,
           SUBSTR(i.COMPANY_NAME, 1, 80),
           PK_DIYDDTOPSP_CONST.gc_DD_SERVICE_DIY,
           p_MIGRATION_PHASE_ID,
  	       SYSDATE,
           NULL,
           NULL
         );
     
      ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Idle_StateCD)     THEN

        -- update to ready and migration date
        UPDATE COMPANY_MIGRATION
           SET MIGRATION_STATE_CD       = PK_DIYDDTOPSP_CONST.gc_Ready_StateCD,
  	           MIGRATION_PHASE_ID       = p_MIGRATION_PHASE_ID, 
               MIGRATION_SCHEDULED_DATE = SYSDATE,
               MIGRATION_ACTUAL_DATE    = NULL,
               TARGET_DB_COMPANY_ID     = NULL
         WHERE SOURCE_DB_COMPANY_ID = i.AS400_COMPANY_ID; 
           
      ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Ready_StateCD)    THEN

        -- update phase and migration date
        UPDATE COMPANY_MIGRATION
           SET MIGRATION_PHASE_ID       = p_MIGRATION_PHASE_ID, 
               MIGRATION_SCHEDULED_DATE = SYSDATE,
               MIGRATION_ACTUAL_DATE    = NULL,
               TARGET_DB_COMPANY_ID     = NULL
         WHERE SOURCE_DB_COMPANY_ID = i.AS400_COMPANY_ID; 
        
      ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD) THEN
        
        -- set warning flag and do nothing with the company
        b_warning_complete_ind := TRUE;
                
      ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Error_StateCD)    THEN

        -- update to ready and migration date
        UPDATE COMPANY_MIGRATION
           SET MIGRATION_STATE_CD       = PK_DIYDDTOPSP_CONST.gc_Ready_StateCD,
  	           MIGRATION_PHASE_ID       = p_MIGRATION_PHASE_ID, 
               MIGRATION_SCHEDULED_DATE = SYSDATE,
               MIGRATION_ACTUAL_DATE    = NULL,
               TARGET_DB_COMPANY_ID     = NULL
         WHERE SOURCE_DB_COMPANY_ID = i.AS400_COMPANY_ID; 
        
      ELSE
      
        -- if any company is migrating, validing or syncing, then that is bad.
        -- will help prevent accidental initializations.
        RAISE_APPLICATION_ERROR (
          pc_raise_app_err_cd,
          'Error: trying to initialize while migrations in progress',
          FALSE
        );
          
      END IF;            
      
    END LOOP;

    
    IF (b_warning_complete_ind) THEN
    
      p_RETURN_CD  := -20053;
      p_RETURN_MSG := 'Initialization completed with warnings. ' ||
                      'Attempted to schedule a company that was already migrated.';
    END IF;    

    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
	  ROLLBACK;
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Unexpected error populating DIY migration queue. ' ||
		 SQLERRM
		),
		1,
		500
	  );
  END;


  PROCEDURE IsDIYDDCompanyMigrating (
    p_DIY_COMPANY_USERID           IN  NUMBER,
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_ISCOMPANYMIGRATING           OUT VARCHAR2    -- 'TRUE' or 'FALSE'
  )
  IS
    bv_migrating_true              VARCHAR2(10) := 'TRUE';
    bv_migrating_false             VARCHAR2(10) := 'FALSE';
    v_prior_migration_state        COMPANY_MIGRATION.MIGRATION_STATE_CD%TYPE;
    
  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';
	
    v_prior_migration_state := FN_GET_PRIOR_MIGRATION_STATE (
                                 TO_CHAR(p_DIY_COMPANY_USERID)
                               );
      
    IF (v_prior_migration_state = NULL) THEN
    
      p_ISCOMPANYMIGRATING := bv_migrating_false;

    ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Idle_StateCD)       THEN

      p_ISCOMPANYMIGRATING := bv_migrating_false;    

    ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Ready_StateCD)      THEN
    
      p_ISCOMPANYMIGRATING := bv_migrating_true;    

    ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Migrating_StateCD)  THEN
    
      p_ISCOMPANYMIGRATING := bv_migrating_true;    

    ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Syncing_StateCD)    THEN
    
      p_ISCOMPANYMIGRATING := bv_migrating_true;    

    ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Validating_StateCD) THEN
    
      p_ISCOMPANYMIGRATING := bv_migrating_true;                      

    ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Complete_StateCD)   THEN
    
      p_ISCOMPANYMIGRATING := bv_migrating_false;    
        
    ELSIF (v_prior_migration_state = PK_DIYDDTOPSP_CONST.gc_Error_StateCD)      THEN
    
      p_ISCOMPANYMIGRATING := bv_migrating_false;    

    ELSE
    
      p_ISCOMPANYMIGRATING := bv_migrating_false;    
      
    END IF;            
      
  EXCEPTION
    WHEN OTHERS THEN
	  ROLLBACK;
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Unexpected error while reading DIY DD migration queue. ' ||
		 SQLERRM
		),
		1,
		500
	  );
  END;


END PK_DIYDDTOPSP_CONTROLLER; 
/

