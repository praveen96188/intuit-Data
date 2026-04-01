CREATE OR REPLACE PACKAGE BODY PK_IOPDDTOPSP_CONTROLLER AS
/******************************************************************************
   NAME:    PK_IOPDDTOPSP_CONTROLLER
   UPDATED: 10.10.2008 02:30 PM
   PURPOSE: Provide an interface for Java to manage the migration state
            of IOP companies being transferred from the PSE to PSP.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        02.13.2008  EMR              Created this package body.
   1.1        06.12.2008  EMR              migrated from POS pc to 10g server,
                                           also added updated migration tables.
   1.2        06.27.2008  EMR              added additional controller apis
                                           for e2e migration.
   2.0        07.08.2008  EMR              adapted framework for IOP.
   2.0.1      07.15.2008  EMR              changed api type to match company 
                                           table
   2.0.2      07.17.2008  EMR              used new range for raise app err                                           
   2.0.3      07.18.2008  EMR              changed comp validate signature   
   2.0.4      09.15.2008  EMR              modified get next company cursor
   2.0.5      10.10.2008  EMR              modified initialize to omit 8350.
******************************************************************************/

/*
   ***************************************************************************
   PURPOSE: cursors

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  -----------------------------------
   1.0        02.20.2008  EMR			   created GetCompanyToMigrate
   1.1        07.01.2008  EMR              removed order by
   1.2        09.15.2008  EMR              rewrote cursor for speed boost.
   ***************************************************************************
*/

  -- ------------------------------------------------------------------------
  -- CURSORS: return a list of companies ready to migrate from PSE to PSP
  -- ------------------------------------------------------------------------
  
  /* OLD CURSOR THAT DOES FULL TABLE SCANS

  V1 - 4 seconds
  
  CURSOR cur_companies_to_migrate
    (p_Interested_StateCD          IN  COMPANY_MIGRATION.MIGRATION_STATE_CD%TYPE)
  IS
    -- company will transition from current state to new state
	-- random queue
    -- removed order by because list will be 10k long and a sort every second
    -- with a list that big is unnecessary.  order in this case does not matter.
    SELECT a.SOURCE_DB_COMPANY_ID AS IOP_SRC_COMPANY_ID,
           a.MIGRATION_STATE_CD   AS CURRENT_STATE
      FROM COMPANY_MIGRATION a
     WHERE a.MIGRATION_STATE_CD = p_Interested_StateCD
       FOR UPDATE OF a.MIGRATION_STATE_CD;

  V2 - 9 milliseconds but does not work with for update
  
  CURSOR cur_companies_to_migrate
    (p_Interested_StateCD          IN  COMPANY_MIGRATION.MIGRATION_STATE_CD%TYPE)
  IS
    -- company will transition from current state to new state
	-- random queue
    -- removed order by because list will be 10k long and a sort every second
    -- with a list that big is unnecessary.  order in this case does not matter.
    SELECT IOP_SRC_COMPANY_ID,
           CURRENT_STATE
      FROM (
            SELECT ROWNUM               AS RECORDNUM,
                   SOURCE_DB_COMPANY_ID AS IOP_SRC_COMPANY_ID,
                   MIGRATION_STATE_CD   AS CURRENT_STATE
              FROM COMPANY_MIGRATION
             WHERE MIGRATION_STATE_CD = p_Interested_StateCD
           )
     WHERE RECORDNUM = 1
     FOR UPDATE OF CURRENT_STATE;
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
    
    SELECT SOURCE_DB_COMPANY_ID AS IOP_SRC_COMPANY_ID,
           MIGRATION_STATE_CD   AS CURRENT_STATE
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
   ***************************************************************************
*/

  PROCEDURE ExecuteStateTransition (
    p_DB_COMPANY_ID                IN  COMPANY_MIGRATION.SOURCE_DB_COMPANY_ID%TYPE,
    p_NEW_STATE_CD                 IN  VARCHAR2
  )
  IS

  BEGIN

    UPDATE COMPANY_MIGRATION
	   SET Migration_State_CD   = p_NEW_STATE_CD
	 WHERE SOURCE_DB_COMPANY_ID = p_DB_COMPANY_ID;

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
   2.0        07.08.2008  EMR              adapted framework for IOP,
                                           also merged validation package
   ***************************************************************************
*/

  PROCEDURE GetCompanyToMigrate (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2,
    p_IOP_SRC_COMPANY_ID           OUT COMPANY.SOURCE_COMPANY_ID%TYPE
  )
  IS
    PRAGMA                         AUTONOMOUS_TRANSACTION;
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
      PK_IOPDDTOPSP_CONST.gc_Ready_StateCD);

    FETCH cur_companies_to_migrate
	 INTO rt_Company_To_Migrate;

	-- return the company ready to migrate
    IF (cur_companies_to_migrate%FOUND) THEN

	  p_IOP_SRC_COMPANY_ID := rt_Company_To_Migrate.IOP_SRC_COMPANY_ID;

      ExecuteStateTransition (
        rt_Company_To_Migrate.IOP_SRC_COMPANY_ID,  -- p_DB_COMPANY_ID
        PK_IOPDDTOPSP_CONST.gc_Migrating_StateCD   -- p_NEW_STATE_CD
      );

    ELSE
	  p_IOP_SRC_COMPANY_ID := NULL;
      p_RETURN_CD          := -20050;
      p_RETURN_MSG         := SUBSTR ( ('Error: no IOP companies found to migrate.'), 1, 150);
	END IF;

    CLOSE cur_companies_to_migrate;

	COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      IF (cur_companies_to_migrate%ISOPEN) THEN
        CLOSE cur_companies_to_migrate;
      END IF;
      ROLLBACK;
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR ( ('Error getting next company to migrate. ' || SQLERRM), 1, 150);
  END;


  -- ------------------------------------------------------------------------
  -- This is the world famous validator.  It validates company data. Wow.
  -- ------------------------------------------------------------------------

  PROCEDURE ValidateMigratedCompany (
    p_IOP_SRC_COMPANY_ID           IN  COMPANY.SOURCE_COMPANY_ID%TYPE,
	p_PSP_COMPANY_GUID             IN  VARCHAR2,
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    PRAGMA                         AUTONOMOUS_TRANSACTION;
	v_temp_RC                      NUMBER;
	v_temp_RC_MSG                  VARCHAR2(500);
    
    v_pse_company_gseq             COMPANY.COMPANY_GSEQ%TYPE;

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';
    
    ExecuteStateTransition (
      p_IOP_SRC_COMPANY_ID,                      -- p_DB_COMPANY_ID
      PK_IOPDDTOPSP_CONST.gc_Validating_StateCD  -- p_NEW_STATE_CD
    );
    
    SELECT Company_Gseq
      INTO v_pse_company_gseq
      FROM COMPANY a
     WHERE Source_Company_ID = p_IOP_SRC_COMPANY_ID;
    
	PK_IOPDDTOPSP_VALIDATION.PR_Validate_IOP_Company (
      v_pse_company_gseq,
	  p_PSP_COMPANY_GUID,
	  v_temp_RC,
	  v_temp_RC_MSG
    );

	IF (v_temp_RC = -1) THEN

      p_RETURN_CD  := -1;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Data inconsistencies were found for IOP company, PSE ID =' ||
		 v_pse_company_gseq || '. ' || 
         v_temp_RC || ': ' || v_temp_RC_MSG
		),
		1,
		500
	  );
    ELSIF (v_temp_RC = -2) THEN
    
      RAISE_APPLICATION_ERROR (
        pc_raise_app_err_cd,
        'UNEXPECTED ERROR WHILE VALIDATING IOP company, PSE ID = ' ||
		 v_pse_company_gseq || '. ' || v_temp_RC_MSG,
        FALSE
      );

	END IF;

    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
	  ROLLBACK;
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR (SQLERRM,	1, 500);
  END;


  PROCEDURE ValidateEntireMigration (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    PRAGMA                         AUTONOMOUS_TRANSACTION;  
	v_temp_RC                      NUMBER;
	v_temp_RC_MSG                  VARCHAR2(500);

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

	PK_IOPDDTOPSP_VALIDATION.PR_Validate_IOP_Migration (
	  v_temp_RC,
	  v_temp_RC_MSG
    );

	IF (v_temp_RC <> 0) THEN

      p_RETURN_CD  := -1;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Data inconsistencies were found for IOP migration.' || v_temp_RC_MSG
		),
		1,
		500
	  );

	END IF;
    
    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Unexpected error while validating IOP migrated data. ' ||
		 SQLERRM
		),
		1,
		500
	  );
  END;


  PROCEDURE SetCompanyMigrationToComplete (
    p_IOP_SRC_COMPANY_ID           IN  COMPANY.SOURCE_COMPANY_ID%TYPE,
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    PRAGMA                         AUTONOMOUS_TRANSACTION;

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    ExecuteStateTransition (
      p_IOP_SRC_COMPANY_ID,                      -- p_DB_COMPANY_ID
      PK_IOPDDTOPSP_CONST.gc_Complete_StateCD    -- p_NEW_STATE_CD
    );

	COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR ( ('Error setting company to complete state for company ' ||
                                 p_IOP_SRC_COMPANY_ID || '. ' || SQLERRM), 1, 500);
  END;


  PROCEDURE SetCompanyMigrationToError (
    p_IOP_SRC_COMPANY_ID           IN  COMPANY.SOURCE_COMPANY_ID%TYPE,
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
    PRAGMA                         AUTONOMOUS_TRANSACTION;

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';

    ExecuteStateTransition (
      p_IOP_SRC_COMPANY_ID,                      -- p_DB_COMPANY_ID
      PK_IOPDDTOPSP_CONST.gc_Error_StateCD       -- p_NEW_STATE_CD
    );

	COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR ( ('Error setting company to error state for company ' ||
                                 p_IOP_SRC_COMPANY_ID || '. ' || SQLERRM), 1, 500);
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

    /* OLD QUERY
    
    SELECT MIN(Z_INS_DTTM), 
    	   MAX(Z_INS_DTTM),
		   ROUND((MAX(Z_INS_DTTM) - MIN(Z_INS_DTTM))*24)
      INTO v_start_time,
           v_last_migration_time,
		   v_run_time
      FROM COMPANY_MIGRATION;
      
    */
    
    -- need to start with the very first company migrated.  there could be a
    -- delay between initialization and start of migration.
    
    SELECT (
            SELECT MIN(Z_INS_DTTM)
              FROM MIGRATION_STATE_HIST
             WHERE MIGRATION_STATE_CD = PK_IOPDDTOPSP_CONST.gc_Migrating_StateCD
           ) AS Mig_Start_Time,
           (
            SELECT MAX(Z_INS_DTTM)
              FROM MIGRATION_STATE_HIST
             WHERE MIGRATION_STATE_CD = PK_IOPDDTOPSP_CONST.gc_Complete_StateCD
                OR MIGRATION_STATE_CD = PK_IOPDDTOPSP_CONST.gc_Error_StateCD
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
    
    
    /* OLD QUERY
    
    SELECT PSP_COUNT,
           PSE_COUNT,
  	       ROUND((PSP_COUNT / PSE_COUNT) * 100, 2) AS PCT_COMPLETE,
  		   (PSE_COUNT - PSP_COUNT)                 AS COMP_REMAINING
  	  INTO v_num_psp,
           v_num_pse,
           v_pct_done,
           v_num_remaining
      FROM (
            SELECT (SELECT COUNT(*) FROM PSP_COMPANY@PSPMIGRATION.WORLD) PSP_COUNT,
                   (SELECT COUNT(*) FROM COMPANY)                        PSE_COUNT
              FROM DUAL
  	     );

    SELECT COUNT(*)
      INTO v_num_migrated
      FROM COMPANY_MIGRATION
     WHERE MIGRATION_STATE_CD = PK_IOPDDTOPSP_CONST.gc_Complete_StateCD;
  		 
    SELECT COUNT(*)
      INTO v_num_errors
      FROM COMPANY_MIGRATION
     WHERE MIGRATION_STATE_CD = PK_IOPDDTOPSP_CONST.gc_Error_StateCD;
    
    */
    
    SELECT MAX(DECODE(
             Migration_State_CD,
             PK_IOPDDTOPSP_CONST.gc_Ready_StateCD, MIG_STATE_CNT,
             0
           )) AS READY_COUNT,
           MAX(DECODE(
             Migration_State_CD,
             PK_IOPDDTOPSP_CONST.gc_Migrating_StateCD, MIG_STATE_CNT,
             0
           )) AS MIGRATING_COUNT,
           MAX(DECODE(
             Migration_State_CD,
             PK_IOPDDTOPSP_CONST.gc_Validating_StateCD, MIG_STATE_CNT,
             0
           )) AS VALIDATING_COUNT,
           MAX(DECODE(
             Migration_State_CD,
             PK_IOPDDTOPSP_CONST.gc_Complete_StateCD, MIG_STATE_CNT,
             0
           )) AS COMPLETE_COUNT,
           MAX(DECODE(
             Migration_State_CD,
             PK_IOPDDTOPSP_CONST.gc_Error_StateCD, MIG_STATE_CNT,
             0
           )) AS ERROR_COUNT
      INTO v_num_ready,
           v_num_migrating,
           v_num_validating,
           v_num_complete,
           v_num_error 
      FROM (      
            SELECT Migration_State_CD,
                   count(*) AS MIG_STATE_CNT
              FROM COMPANY_MIGRATION
             GROUP 
                BY Migration_State_CD
           );

    SELECT v_num_ready + v_num_migrating + v_num_validating + v_num_complete + v_num_error
      INTO v_total_to_migrate
      FROM DUAL;
      
    SELECT ROUND(((v_num_complete + v_num_error) / v_total_to_migrate)*100, 2)
      INTO v_pct_done
      FROM DUAL;
    
    SELECT v_num_migrating + v_num_validating
      INTO v_num_threads
      FROM DUAL;
    
    v_num_remaining := v_num_ready; 

    DBMS_OUTPUT.PUT_LINE (CHR(0));
    DBMS_OUTPUT.PUT_LINE ('Total companies to migrate        : ' || v_total_to_migrate );
    DBMS_OUTPUT.PUT_LINE ('Percent complete                  : ' || v_pct_done || '%'  );
    DBMS_OUTPUT.PUT_LINE ('  Number migrated successfully    : ' || v_num_complete     ); -- cp
    DBMS_OUTPUT.PUT_LINE ('  Number migrated with errors     : ' || v_num_error        ); -- err   
    DBMS_OUTPUT.PUT_LINE ('  Number waiting to migrate       : ' || v_num_ready        ); -- rdy
    DBMS_OUTPUT.PUT_LINE ('  Number migrated but validating  : ' || v_num_validating   ); -- val
    DBMS_OUTPUT.PUT_LINE ('  Number of migration threads     : ' || v_num_threads      ); -- mig + val
  
    
    -- migration time in seconds
    
    /* OLD QUERY
    SELECT MIN(mig_time_sec), 
           AVG(mig_time_sec), 
    	   MAX(mig_time_sec)
      INTO v_min_sec,
           v_avg_sec,
           v_max_sec
      FROM (	
            SELECT mig_start,
                   mig_end,
          	       ROUND((mig_end - mig_start)*24*60*60) AS mig_time_sec,
          	       stmig_gseq,
          	       endmig_gseq,
          	       rn
              FROM ( 	   
                    SELECT a.Z_UPD_DTTM             AS mig_start,
                           b.Z_INS_DTTM             AS mig_end,
          	               a.COMPANY_MIGRATION_GSEQ AS stmig_gseq,
          	               b.COMPANY_MIGRATION_GSEQ AS endmig_gseq,
          	               ROW_NUMBER() OVER (
    					     PARTITION BY a.COMPANY_MIGRATION_GSEQ 
    						 ORDER     BY b.COMPANY_MIGRATION_GSEQ
    					   ) AS rn
                      FROM COMPANY_MIGRATION a,
                           COMPANY_MIGRATION b	
                     WHERE a.COMPANY_MIGRATION_GSEQ < b.COMPANY_MIGRATION_GSEQ
                   )
             WHERE rn = 1   
           );
    */
    
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
                            SELECT Company_Migration_Gseq,
                                   MAX(DECODE (
                                     Migration_State_Cd,
                                     PK_IOPDDTOPSP_CONST.gc_Ready_StateCD, z_ins_dttm
                                   )) AS READY_TIME,
                                   MAX(DECODE (
                                     Migration_State_Cd,
                                     PK_IOPDDTOPSP_CONST.gc_Migrating_StateCD, z_ins_dttm
                                   )) AS MIGRATING_TIME,
                                   MAX(DECODE (
                                     Migration_State_Cd,
                                     PK_IOPDDTOPSP_CONST.gc_Validating_StateCD, z_ins_dttm
                                   )) AS VALIDATING_TIME,
                                   MAX(DECODE (
                                     Migration_State_Cd,
                                     PK_IOPDDTOPSP_CONST.gc_Complete_StateCD,  z_ins_dttm,
                                     PK_IOPDDTOPSP_CONST.gc_Error_StateCD, z_ins_dttm
                                   )) AS COMPLETE_TIME
                              FROM MIGRATION_STATE_HIST
                             GROUP
                                BY Company_Migration_Gseq
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
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';
	
    -- 10.10.2008 EMR
    --   company 8350 was a bogus company record created as a result of 
    --   a toplink production issue in July 2008.  It will not, I repeat,
    --   will not be migrated or counted in the global validation.
    --   src id = PROD_127140871 DUPLICATE COMP RECORD - CANCELED
	
    INSERT INTO COMPANY_MIGRATION (
      COMPANY_MIGRATION_GSEQ, 
  	  MIGRATION_STATE_CD, 
  	  SOURCE_DB_COMPANY_ID, 
      COMPANY_LEGAL_NAME, 
  	  DD_SERVICE_CD, 
  	  MIGRATION_PHASE_ID, 
      MIGRATION_SCHEDULED_DATE 
    )
    SELECT SEQ_COMPANY_MIGRATION.NEXTVAL,
           PK_IOPDDTOPSP_CONST.gc_Ready_StateCD,
           Source_Company_ID,
           Legal_Name,
           PK_IOPDDTOPSP_CONST.gc_DD_SERVICE_IOP,
           '1',
  	       SYSDATE
      FROM COMPANY
     WHERE COMPANY_GSEQ <> PK_IOPDDTOPSP_CONST.gc_Bogus_Company_8350;

    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
	  ROLLBACK;
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Unexpected error populating IOP migration queue. ' ||
		 SQLERRM
		),
		1,
		500
	  );
  END;


  PROCEDURE PurgeMigrationLogs (
    p_RETURN_CD                    OUT NUMBER,
    p_RETURN_MSG                   OUT VARCHAR2
  )
  IS
	v_temp_RC                      NUMBER;
	v_temp_RC_MSG                  VARCHAR2(500);
	
	v_temp_cd                      SYSPARAM_VAL.SYSPARAM_VAL%TYPE;

  BEGIN

    -- assume all is well until proven wrong
    p_RETURN_CD  := 0;
    p_RETURN_MSG := '';
	
	
	-- this is a flag to control purging.  mainly done so
	-- we don't do bad things like erasing in prod.
	BEGIN
	
	  SELECT SYSPARAM_VAL
	    INTO v_temp_cd
	    FROM SYSPARAM_VAL
	   WHERE SYSPARAM_CD = pc_purge_flag;
	   
	EXCEPTION
	  WHEN NO_DATA_FOUND THEN
	    v_temp_cd := PK_IOPDDTOPSP_CONST.gc_FALSE;
	  WHEN OTHERS THEN
	    RAISE;
	END;

    -- purge is controlled by a sysparam value
	-- so that we do not accidentally delete stuff in prod.	
	IF (v_temp_cd = PK_IOPDDTOPSP_CONST.gc_TRUE) THEN
	
      DELETE FROM MIGRATION_OBJECT_ASSOC;
      DELETE FROM MIGRATION_STATE_HIST;
      DELETE FROM MIGRATION_EVENT_LOG;
      DELETE FROM COMPANY_MIGRATION;

      COMMIT;
	  
	END IF;

  EXCEPTION
    WHEN OTHERS THEN
	  ROLLBACK;
      p_RETURN_CD  := -2;
      p_RETURN_MSG := SUBSTR (
	    (
		 'Unexpected error while purging migration logs. ' ||
		 SQLERRM
		),
		1,
		500
	  );
  END;


END PK_IOPDDTOPSP_CONTROLLER; 
/

