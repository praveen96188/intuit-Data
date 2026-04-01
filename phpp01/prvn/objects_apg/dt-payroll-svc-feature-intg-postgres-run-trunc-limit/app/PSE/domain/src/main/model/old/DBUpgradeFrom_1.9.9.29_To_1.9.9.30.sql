--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
Prompt Column IS_FEE_EVENT_CREATION_COMPLETE;
ALTER TABLE PSP_OFFLOAD_BATCH
 ADD (IS_FEE_EVENT_CREATION_COMPLETE  NUMBER(1));

Prompt Table PSP_BATCH_JOB_SETUP;
--
-- PSP_BATCH_JOB_SETUP  (Table) 
--
CREATE TABLE PSP_BATCH_JOB_SETUP
(
  BATCH_JOB_SETUP_SEQ         VARCHAR2(255 CHAR) NOT NULL,
  VERSION                     NUMBER(19)        NOT NULL,
  CREATOR_ID                  VARCHAR2(30 CHAR),
  CREATED_DATE                TIMESTAMP(6)      NOT NULL,
  MODIFIER_ID                 VARCHAR2(30 CHAR),
  MODIFIED_DATE               TIMESTAMP(6)      NOT NULL,
  REALM_ID                    NUMBER(19)        DEFAULT -1                    NOT NULL,
  JOB_TIMER_EXPRESSION        VARCHAR2(4000 CHAR),
  JOB_PROCESSOR_CLASS_NAME    VARCHAR2(4000 CHAR),
  DLY_BW_RETRIES_TIMER_EXPR   VARCHAR2(4000 CHAR),
  JOB_TYPE                    VARCHAR2(255 CHAR),
  MAX_RETRIES                 NUMBER(10),
  IS_AUTOMATICALLY_SCHEDULED  NUMBER(1)
)
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

Prompt Procedure PRC_OFFLOAD;
--
-- PRC_OFFLOAD  (Procedure) 
--
CREATE OR REPLACE PROCEDURE PRC_OFFLOAD
   (
	p_offload_group_cd  IN VARCHAR2,  -- 'STD' under normal conditions
	p_offload_date      IN TIMESTAMP, -- UTC Date
	p_ccd_file_name     IN VARCHAR2,
	p_ppd_file_name     IN VARCHAR2,
	p_user_id           IN VARCHAR2,  -- For audit purposes
	p_app_server_date   IN TIMESTAMP, -- UTC Date
	p_offload_batch_id  OUT VARCHAR2  -- psp_offload_batch.offload_batch_seq
   )
IS

    -- these two variables are used in all SQL statements to populate date fields,
	-- the UTC date is used to populate SPCF audit fields created_date and modified_date

	v_psp_date TIMESTAMP; -- current system date and time adjusted by PSPDate offset
	v_utc_date TIMESTAMP; -- current system UTC date and time


	PROCEDURE create_offload_batch
	IS
	   v_offload_group_id VARCHAR2(100);
	BEGIN

	   SELECT OFFLOAD_GROUP_SEQ INTO v_offload_group_id FROM PSP_OFFLOAD_GROUP WHERE OFFLOAD_GROUP_CD = p_offload_group_cd;

	   SELECT FN_FORMAT_SYSGUID(SYS_GUID()) INTO p_offload_batch_id FROM DUAL;

	   -- Create OffloadBatch
	   INSERT INTO PSP_OFFLOAD_BATCH (
		   OFFLOAD_BATCH_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, STATUS_CD, STATUS_EFFECIVE_DATE, OFFLOAD_DATE, OFFLOAD_GROUP_FK, IS_FEE_EVENT_CREATION_COMPLETE)
	   VALUES (p_offload_batch_id, 0, p_user_id, v_utc_date, p_user_id, v_utc_date, -1, 'Completed', v_psp_date, p_offload_date, v_offload_group_id, 0);


	   -- Associate MMTxns with Offload Batch and set their status to Executed
            UPDATE  psp_money_movement_transaction mmt
               SET offload_batch_fk = p_offload_batch_id,
                   status = 'Executed',
                   VERSION = VERSION + 1,
                    MODIFIER_ID = p_user_id,
            			   MODIFIED_DATE = v_utc_date
                   WHERE  mmt.offload_batch_fk IS NULL
                         AND mmt.initiation_date = p_offload_date
                         AND mmt.money_movement_payment_method = 'ACHDirectDeposit'
              and  EXISTS (
                      SELECT  'T'
                        FROM psp_financial_transaction ft,
                             psp_company_service cs,
                             psp_ddcompany_service_info dd
                       WHERE
                            ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
                         AND ft.on_hold = 0
                         AND ft.current_transaction_state_fk = 'Created'
                         AND ft.company_fk = cs.company_fk
                         AND cs.company_service_seq = dd.ddcompany_service_info_seq
                         AND dd.offload_group_fk = v_offload_group_id
                         AND cs.service_fk = 'DirectDeposit');


	END create_offload_batch;


	PROCEDURE create_nacha_file(p_nacha_file_type  IN VARCHAR2)
	IS
		v_nacha_file_id         VARCHAR2(100);
		v_next_file_id_modifier VARCHAR2(1);
		v_nacha_max_amount      NUMBER(14,2) := 9999999.99;
		v_bank_owner_type       VARCHAR2(100);
		v_next_trace_number     NUMBER;
		v_has_txns              NUMBER;



		PROCEDURE create_nacha_file_rec
		IS
		   v_file_name VARCHAR2(4000);
           v_CREDIT_TXN_TOTAL_AMOUNT number;
           v_DEBIT_TXN_TOTAL_AMOUNT number;
		BEGIN
		   SELECT FN_FORMAT_SYSGUID(SYS_GUID()) INTO v_nacha_file_id FROM DUAL;
		   SELECT CHR(SEQ_ACH_FILE_CTR.NEXTVAL + ASCII('A') - 1) INTO v_next_file_id_modifier FROM DUAL;

		   IF p_nacha_file_type = 'CCD' THEN
		      v_file_name := p_ccd_file_name;
		   ELSE
		      v_file_name := p_ppd_file_name;
		   END IF;

		   INSERT INTO PSP_NACHAFILE
		   		  (NACHAFILE_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, CONFIRMATION_CODE, FILE_NAME,
			      CONFIRMATION_DATE, FINALIZATION_DATE, STATUS_EFFECTIVE_DATE, TRANSMISSION_DATE, CREDIT_TXN_TOTAL_AMOUNT, DEBIT_TXN_TOTAL_AMOUNT, STATUS,
				  FILE_TYPE, FILE_I_D_MODIFIER)
		   VALUES (v_nacha_file_id, 0, p_user_id, v_utc_date, p_user_id, v_utc_date, -1, NULL, v_file_name,
		      NULL, v_psp_date, v_psp_date, NULL, 0, 0, 'Finalized',
		      p_nacha_file_type, v_next_file_id_modifier);

		   -- Associate Entry Detail Records with NACHA file
           UPDATE psP_ENTRY_DETAIL_RECORD REC0
               SET N_A_C_H_A_FILE_FK = v_nacha_file_id,
                   VERSION = VERSION + 1,
                   MODIFIER_ID = p_user_id,
                   MODIFIED_DATE = v_utc_date
               WHERE
               N_A_C_H_A_FILE_FK is null AND
               EXISTS
               (SELECT 'T'
                  FROM  PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
                        PSP_FINANCIAL_TRANSACTION FT1
                 WHERE REC0.MONEY_MOVEMENT_TRANSACTION_FK = MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ
                   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
                   AND FT1.MONEY_MOVEMENT_TRANSACTION_FK = MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ
                   AND (FT1.DEBIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
                        OR FT1.CREDIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
                        OR (p_nacha_file_type = 'CCD'
                            AND FT1.DEBIT_BANK_ACCOUNT_TYPE = 'Intuit'
                            AND FT1.CREDIT_BANK_ACCOUNT_TYPE = 'Intuit'
                           )
                          )          );

                          /*
                 RETURNING SUM(DECODE(REC0.CREDIT_DEBIT_INDICATOR, 'Credit', REC0.AMOUNT, 0)) ,
                           SUM(DECODE(REC0.CREDIT_DEBIT_INDICATOR, 'Debit',  REC0.AMOUNT, 0))
                      INTO  v_CREDIT_TXN_TOTAL_AMOUNT,
                            v_DEBIT_TXN_TOTAL_AMOUNT; */

			-- Update NACHA File record with total credits and debits for this file
            UPDATE PSP_NACHAFILE
			   SET VERSION = VERSION + 1,
				   MODIFIER_ID = p_user_id,
				   MODIFIED_DATE = v_utc_date,
				   (CREDIT_TXN_TOTAL_AMOUNT, DEBIT_TXN_TOTAL_AMOUNT) =
				      (
					      SELECT SUM(DECODE(REC.CREDIT_DEBIT_INDICATOR, 'Credit', REC.AMOUNT, 0)) AS CREDIT_TXN_TOTAL_AMOUNT,
				                 SUM(DECODE(REC.CREDIT_DEBIT_INDICATOR, 'Debit',  REC.AMOUNT, 0)) AS DEBIT_TXN_TOTAL_AMOUNT
						    FROM PSP_ENTRY_DETAIL_RECORD REC
						   WHERE REC.N_A_C_H_A_FILE_FK = v_nacha_file_id
					  )
			   WHERE
			       NACHAFILE_SEQ = v_nacha_file_id;

		END create_nacha_file_rec;


		PROCEDURE update_txn_trace_number
		IS
		BEGIN


			MERGE INTO PSP_ENTRY_DETAIL_RECORD dst
			USING (
				   SELECT REC2.ENTRY_DETAIL_RECORD_SEQ,
				          ROW_NUMBER () OVER (ORDER BY C2.LEGAL_NAME, C2.COMPANY_SEQ, TT2.N_A_C_H_A_BATCH_TYPE, REC2.RECORD_DATA, REC2.AMOUNT, REC2.ENTRY_DETAIL_RECORD_SEQ) TRACE_NUMBER
				     FROM PSP_ENTRY_DETAIL_RECORD REC2,
					      PSP_MONEY_MOVEMENT_TRANSACTION MMT2,
					      PSP_TRANSACTION_TYPE TT2,
					      PSP_COMPANY C2
					WHERE REC2.N_A_C_H_A_FILE_FK = v_nacha_file_id
					   AND REC2.MONEY_MOVEMENT_TRANSACTION_FK = MMT2.MONEY_MOVEMENT_TRANSACTION_SEQ
					   AND REC2.RECORD_DATA IS NOT NULL
					   AND C2.COMPANY_SEQ = MMT2.COMPANY_FK
					   AND TT2.TRANSACTION_TYPE_CD =
						     (
							   SELECT FT2.TRANSACTION_TYPE_FK
							     FROM PSP_FINANCIAL_TRANSACTION FT2
								 WHERE FT2.MONEY_MOVEMENT_TRANSACTION_FK = MMT2.MONEY_MOVEMENT_TRANSACTION_SEQ
								   AND (FT2.DEBIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
								        OR FT2.CREDIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
										OR (p_nacha_file_type = 'CCD'
									         AND FT2.DEBIT_BANK_ACCOUNT_TYPE = 'Intuit'
										     AND FT2.CREDIT_BANK_ACCOUNT_TYPE = 'Intuit'
										   )
									   )
								  AND ROWNUM = 1 -- to account for multiple FTs per MMTxn
							 )
				   ) src
			ON	(src.ENTRY_DETAIL_RECORD_SEQ = dst.ENTRY_DETAIL_RECORD_SEQ)
			WHEN MATCHED
			THEN UPDATE
			     SET VERSION = VERSION + 1,
					  MODIFIER_ID = p_user_id,
					  MODIFIED_DATE = v_utc_date,
					  dst.trace_number = src.trace_number + v_next_trace_number;

		   -- Update next trace number
		   SELECT COUNT(*) + v_next_trace_number + 1
			 INTO v_next_trace_number
			 FROM PSP_ENTRY_DETAIL_RECORD REC
		    WHERE REC.N_A_C_H_A_FILE_FK = v_nacha_file_id
			  AND REC.TRACE_NUMBER IS NOT NULL;
		END update_txn_trace_number;



	BEGIN

	   IF p_nacha_file_type = 'CCD' THEN
	       v_bank_owner_type := 'Company';
	   ELSE
	       v_bank_owner_type := 'Employee';
	   END IF;

	   -- See if there are any txns for this file (0 - no btxns, 1 - found txns)
	   SELECT COUNT(*) INTO v_has_txns
		   FROM PSP_FINANCIAL_TRANSACTION FT,
		        PSP_MONEY_MOVEMENT_TRANSACTION MMT
		   WHERE ROWNUM = 1
		     AND MMT.OFFLOAD_BATCH_FK = p_offload_batch_id
             and mmt.MONEY_MOVEMENT_TRANSACTION_SEQ = ft.MONEY_MOVEMENT_TRANSACTION_FK
	         AND (FT.DEBIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
				  OR FT.CREDIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
				  OR (p_nacha_file_type = 'CCD'
		              AND FT.DEBIT_BANK_ACCOUNT_TYPE = 'Intuit'
					  AND FT.CREDIT_BANK_ACCOUNT_TYPE = 'Intuit'
				     )
	 	   	     );

	   IF v_has_txns = 0 THEN
	        RETURN;
	   END IF;


	   SELECT SEQ_TRACE_NBR.NEXTVAL INTO v_next_trace_number FROM DUAL;

	   create_nacha_file_rec();

	   update_txn_trace_number();

	END create_nacha_file;


	PROCEDURE update_financial_txn_status
	IS
	BEGIN
	   -- Create Executed Transaction States for all transactions in the Offload Batch
	   INSERT INTO PSP_FINANCIAL_TRANS_STATE
	     (SELECT FN_FORMAT_SYSGUID(SYS_GUID()), 0, p_user_id, v_utc_date, p_user_id, v_utc_date, -1,
		        v_psp_date, NULL, NULL, FT.FINANCIAL_TRANSACTION_SEQ, 'Executed', NULL
			 FROM PSP_FINANCIAL_TRANSACTION FT,
			      PSP_MONEY_MOVEMENT_TRANSACTION MMT
		     WHERE MMT.OFFLOAD_BATCH_FK = p_offload_batch_id
		       AND MMT.MONEY_MOVEMENT_TRANSACTION_SEQ = FT.MONEY_MOVEMENT_TRANSACTION_FK
			   AND FT.CURRENT_TRANSACTION_STATE_FK = 'Created'
		   );


	   -- Set the status of Financial Transactions to Executed for all transactions in the Offload Batch
           UPDATE PSP_FINANCIAL_TRANSACTION FT0
           SET CURRENT_TRANSACTION_STATE_FK = 'Executed',
               VERSION = VERSION + 1,
               MODIFIER_ID = p_user_id,
               MODIFIED_DATE = v_utc_date
           WHERE
                  FT0.CURRENT_TRANSACTION_STATE_FK = 'Created'
            and
            EXISTS
              (
                    SELECT 'T'
                     FROM  PSP_MONEY_MOVEMENT_TRANSACTION MMT1
                    WHERE  MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT0.MONEY_MOVEMENT_TRANSACTION_FK
                       AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
              );

	END update_financial_txn_status;


	PROCEDURE update_payroll_run_status
	IS
	BEGIN
		UPDATE PSP_PAYROLL_RUN PR
		   SET PAYROLL_RUN_STATUS = 'OffloadedDebit',
		       VERSION = VERSION + 1,
			   MODIFIER_ID = p_user_id,
			   MODIFIED_DATE = v_utc_date
		   WHERE
           PR.PAYROLL_RUN_STATUS != 'OffloadedAll'
           AND
           EXISTS
			  (
			     SELECT  'T'
				     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
				          PSP_FINANCIAL_TRANSACTION FT1
				     WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
					   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
					   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
					   AND FT1.TRANSACTION_TYPE_FK = 'EmployerDdDebit'
			  );

	    UPDATE PSP_PAYROLL_RUN PR
		   SET PAYROLL_RUN_STATUS = 'AutoRedebitOffloaded',
		       VERSION = VERSION + 1,
			   MODIFIER_ID = p_user_id,
			   MODIFIED_DATE = v_utc_date
		   WHERE
            PR.PAYROLL_RUN_STATUS = 'PendingAutoRedebit'
           AND
           EXISTS
			  (
			     SELECT  'T'
				     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
				          PSP_FINANCIAL_TRANSACTION FT1,
						  PSP_TRANSACTION_TYPE TT1
				     WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
					   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
					   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
					   AND FT1.TRANSACTION_TYPE_FK = TT1.TRANSACTION_TYPE_CD
					   AND TT1.ASSOCIATION_TYPE = 'Redebit'
			  );

	    UPDATE PSP_PAYROLL_RUN PR
		   SET PAYROLL_RUN_STATUS = 'RedebitOffloaded',
		       VERSION = VERSION + 1,
			   MODIFIER_ID = p_user_id,
			   MODIFIED_DATE = v_utc_date
		   WHERE
              PR.PAYROLL_RUN_STATUS = 'PendingRedebit'
           AND
           EXISTS
			  (
			     SELECT  'T'
				     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
				          PSP_FINANCIAL_TRANSACTION FT1,
						  PSP_TRANSACTION_TYPE TT1
				     WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
					   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
					   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
					   AND FT1.TRANSACTION_TYPE_FK = TT1.TRANSACTION_TYPE_CD
					   AND TT1.ASSOCIATION_TYPE = 'Redebit'
			  );

	    UPDATE PSP_PAYROLL_RUN PR
		   SET PAYROLL_RUN_STATUS = 'OffloadedAll',
		       VERSION = VERSION + 1,
			   MODIFIER_ID = p_user_id,
			   MODIFIED_DATE = v_utc_date
		   WHERE EXISTS
			  (
			     SELECT   'T'
				     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
				          PSP_FINANCIAL_TRANSACTION FT1
				     WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
					   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
					   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
					   AND FT1.TRANSACTION_TYPE_FK = 'EmployeeDdCredit'
			  );

	    UPDATE PSP_PAYROLL_RUN PR
		   SET PAYROLL_RUN_STATUS = 'ReversalsOffloaded',
		       VERSION = VERSION + 1,
			   MODIFIER_ID = p_user_id,
			   MODIFIED_DATE = v_utc_date
		   WHERE
               PR.PAYROLL_RUN_STATUS = 'PendingReversals'
            AND
           EXISTS
			  (
			     SELECT   'T'
				     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
				          PSP_FINANCIAL_TRANSACTION FT1
				     WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
					   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
					   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
					   AND FT1.TRANSACTION_TYPE_FK = 'EmployeeDdReversalDebit'
			  );
	END update_payroll_run_status;



BEGIN
	SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL;
	--SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP) INTO v_utc_date FROM DUAL;
	v_utc_date := p_app_server_date;

    -- Crates the offload batch and associated all the moneymovement txns
    -- with the offload. Also marks the moneymovement txns as 'Executed'

	create_offload_batch();

    -- Creates NACHAFILE, and associates all the Entrydetailrecord with the
    -- NACHA file id. Also updates NACHA file record with the SUM of credits
    -- and debits.
    -- Also calls update_txn_trace_number to update entry detail record with
    -- trace_nbr

/*     TODO:
     1. Based on performance numbers we may have to add an additional
     index on Financailtransaction DEBIT/CREDIT Bank account type
     2. We many split the update based on CCD and PPD.
     3. May need to revisit MERGE and COUNT  query, currently untoched.
*/

	create_nacha_file('CCD');

    -- Does same as above for PPD

    create_nacha_file('PPD');

    -- Inserts transactions associated with the offload batch into
    -- financial_transaction_state table. NO MORE updates ledger_balance table
    --  Updates all those financial_transaction to EXECUTED

    update_financial_txn_status();

    -- Updates payroll_run table will proper status after the offload
    -- 5 seperate updates to the same table.

/*    TODO:
      1. we may able to consolidate this into less update statements
*/

    update_payroll_run_status();


END PRC_OFFLOAD;
/

SHOW ERRORS;

ALTER TABLE PSP_BATCH_JOB_SETUP
 ADD CONSTRAINT C_PSP_BATCH_JOB_SETUP0
 CHECK (JOB_TYPE IN('AchOffload', 'AchOffloadConfirmationReceivedMonitor', 'AchOffloadFileCreationCompleteMonitor', 'AchOffloadFileCreationStartedMonitor', 'AchOffloadFileSendCompleteMonitor', 'AchOffloadFileSendStartedMonitor', 'AchOffloadSuccessfulMonitor', 'AchReturns', 'AchReturnsFileReceivedMonitor', 'AchReturnsSuccessfulMonitor', 'Migration', 'MissedPayrolls', 'MissedPayrollsSuccessful', 'MissedTransactions', 'Emails', 'FeeEvents', 'SalesTaxException', 'As400ToCris', 'CrisToSourceSystem', 'PspToCris', 'DicrFiles', 'DicrFilesSuccessfulMonitor', 'FraudPayrolls', 'GemsDailyUpload', 'GemsMonthlyUpload', 'MissedTransactionsSuccessful', 'LedgerBalance', 'FeeEventsMonitor'));

ALTER TABLE PSP_BATCH_JOB_SETUP
 ADD PRIMARY KEY
 (BATCH_JOB_SETUP_SEQ, REALM_ID);

PROMPT finishedDBUpgradeFrom_1.9.9.29_To_1.9.9.30.sql