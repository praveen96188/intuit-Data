--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
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
		   OFFLOAD_BATCH_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, STATUS_CD, STATUS_EFFECIVE_DATE, OFFLOAD_DATE, OFFLOAD_GROUP_FK)
	   VALUES (p_offload_batch_id, 0, p_user_id, v_utc_date, p_user_id, v_utc_date, -1, 'Completed', v_psp_date, p_offload_date, v_offload_group_id);

	   -- Associate MMTxns with Offload Batch and set their status to Executed
	   UPDATE PSP_MONEY_MOVEMENT_TRANSACTION MMT1
		   SET OFFLOAD_BATCH_FK = p_offload_batch_id,
			   STATUS = 'Executed',
			   VERSION = VERSION + 1,
			   MODIFIER_ID = p_user_id,
			   MODIFIED_DATE = v_utc_date
		   WHERE EXISTS
		    (SELECT MMT.MONEY_MOVEMENT_TRANSACTION_SEQ
		     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT,
		          PSP_FINANCIAL_TRANSACTION FT,
		          PSP_COMPANY_SERVICE CS,
		 	      PSP_DDCOMPANY_SERVICE_INFO DD
		     WHERE MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = MMT.MONEY_MOVEMENT_TRANSACTION_SEQ
			   AND MMT.OFFLOAD_BATCH_FK IS NULL
			   AND MMT.INITIATION_DATE = p_offload_date
		       AND MMT.MONEY_MOVEMENT_TRANSACTION_SEQ = FT.MONEY_MOVEMENT_TRANSACTION_FK
		       AND FT.ON_HOLD = 0
		       AND FT.CURRENT_TRANSACTION_STATE_FK = 'Created'
		       AND MMT.MONEY_MOVEMENT_PAYMENT_METHOD = 'ACHDirectDeposit'
		       AND CS.COMPANY_FK = MMT.COMPANY_FK
		       AND DD.DDCOMPANY_SERVICE_INFO_SEQ = CS.COMPANY_SERVICE_SEQ
		       AND DD.OFFLOAD_GROUP_FK = v_offload_group_id
		       AND CS.SERVICE_FK = 'DirectDeposit');
	END create_offload_batch;


	PROCEDURE create_nacha_file(p_nacha_file_type  IN VARCHAR2)
	IS
		v_nacha_file_id         VARCHAR2(100);
		v_next_file_id_modifier VARCHAR2(1);
		v_nacha_max_amount      NUMBER(14,2) := 9999999.99;
		v_bank_owner_type       VARCHAR2(100);
		v_next_trace_number     NUMBER;
		v_has_txns              NUMBER;

		TYPE accum_rec IS RECORD
		(
		   nacha_batch_type       VARCHAR2(100),
		   src_system_cd          VARCHAR2(100),
		   intuit_bank_account_id VARCHAR2(100),
		   credit_debit_ind       VARCHAR2(10),
		   amount                 NUMBER(14,2),
		   txn_begin_sort_key     INTEGER,
		   txn_end_sort_key       INTEGER
		);
		-- Associative array (accum_rec_key -> accum_rec)
		TYPE accum_rec_tab IS TABLE OF accum_rec INDEX BY VARCHAR2(4000);
		v_accum_rec_table         accum_rec_tab;
		v_accum_rec               accum_rec;
		v_accum_rec_key           VARCHAR2(4000);

		-- Associative aray (accum_batch_key -> accum_rec_key)
		TYPE accum_batch_tab IS TABLE OF INTEGER INDEX BY VARCHAR2(4000);
		v_accum_batch_table accum_batch_tab;
		v_accum_batch_key         VARCHAR2(4000);
		v_accum_rec_num           INTEGER;

		PROCEDURE init_accumulated_batches
		IS
		BEGIN
		   FOR rec IN (
		                  select ibb.N_A_C_H_A_BATCH_TYPE, ss.SOURCE_SYSTEM_CD, ibb.INTUIT_BANK_ACCOUNT_FK
						    from psp_intuit_ba_bt_ft ibb, psp_source_system ss
						   where ibb.FILE_TYPE = p_nacha_file_type
							 and ss.SOURCE_SYSTEM_CD IN ('QBDT', 'QBOE')
						   order by ibb.N_A_C_H_A_BATCH_TYPE, ss.SOURCE_SYSTEM_CD, ibb.INTUIT_BANK_ACCOUNT_FK
		              )
		   LOOP
		       SELECT REC.N_A_C_H_A_BATCH_TYPE || ':' || REC.SOURCE_SYSTEM_CD || ':' || REC.INTUIT_BANK_ACCOUNT_FK
			     INTO v_accum_batch_key
				 FROM DUAL;

		   	   v_accum_batch_table(v_accum_batch_key || ':' || 'Credit') := 0;
			   v_accum_batch_table(v_accum_batch_key || ':' || 'Debit') := 0;
		   END LOOP;
		END init_accumulated_batches;


		PROCEDURE create_nacha_file_rec
		IS
		   v_file_name VARCHAR2(4000);
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
		   UPDATE PSP_ENTRY_DETAIL_RECORD REC0
			   SET N_A_C_H_A_FILE_FK = v_nacha_file_id,
				   VERSION = VERSION + 1,
				   MODIFIER_ID = p_user_id,
				   MODIFIED_DATE = v_utc_date
			   WHERE EXISTS
			   (SELECT REC1.ENTRY_DETAIL_RECORD_SEQ
			     FROM PSP_ENTRY_DETAIL_RECORD REC1,
					  PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
			          PSP_FINANCIAL_TRANSACTION FT1
			     WHERE REC1.ENTRY_DETAIL_RECORD_SEQ = REC0.ENTRY_DETAIL_RECORD_SEQ
				   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				   AND REC1.MONEY_MOVEMENT_TRANSACTION_FK = MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ
			       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
			       AND (FT1.DEBIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
						OR FT1.CREDIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
						OR (p_nacha_file_type = 'CCD'
				            AND FT1.DEBIT_BANK_ACCOUNT_TYPE = 'Intuit'
							AND FT1.CREDIT_BANK_ACCOUNT_TYPE = 'Intuit'
						   )
				   	   )
			   );

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


		PROCEDURE accumulate_intuit_batch
		IS
		BEGIN
		   FOR REC0 IN (
		                SELECT REC1.ENTRY_DETAIL_RECORD_SEQ,
						       C1.SOURCE_SYSTEM_CD,
							   TT1.N_A_C_H_A_BATCH_TYPE,
							   REC1.INTUIT_BANK_ACCOUNT_FK,
							   REC1.CREDIT_DEBIT_INDICATOR,
					   		   REC1.AMOUNT,
						       ROW_NUMBER() OVER (ORDER BY TT1.N_A_C_H_A_BATCH_TYPE, C1.SOURCE_SYSTEM_CD, REC1.INTUIT_BANK_ACCOUNT_FK, REC1.AMOUNT, REC1.ENTRY_DETAIL_RECORD_SEQ) SORT_KEY
					    FROM PSP_ENTRY_DETAIL_RECORD REC1,
						  PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
						  PSP_TRANSACTION_TYPE TT1,
						  PSP_COMPANY C1
						WHERE REC1.N_A_C_H_A_FILE_FK = v_nacha_file_id
						   AND REC1.MONEY_MOVEMENT_TRANSACTION_FK = MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ
						   AND REC1.INTUIT_BANK_ACCOUNT_FK IS NOT NULL
						   AND C1.COMPANY_SEQ = MMT1.COMPANY_FK
						   AND TT1.TRANSACTION_TYPE_CD =
						     (
							   SELECT FT2.TRANSACTION_TYPE_FK
							     FROM PSP_FINANCIAL_TRANSACTION FT2
								 WHERE FT2.MONEY_MOVEMENT_TRANSACTION_FK = MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ
								   AND (FT2.DEBIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
								        OR FT2.CREDIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
										OR (p_nacha_file_type = 'CCD'
									         AND FT2.DEBIT_BANK_ACCOUNT_TYPE = 'Intuit'
										     AND FT2.CREDIT_BANK_ACCOUNT_TYPE = 'Intuit'
										   )
									   )
								  AND ROWNUM = 1 -- to account for multiple FTs per MMTxn
							 )
						 ORDER BY TT1.N_A_C_H_A_BATCH_TYPE, C1.SOURCE_SYSTEM_CD, REC1.INTUIT_BANK_ACCOUNT_FK, REC1.AMOUNT, REC1.ENTRY_DETAIL_RECORD_SEQ
			           )
		    LOOP
			   SELECT REC0.N_A_C_H_A_BATCH_TYPE || ':' || REC0.SOURCE_SYSTEM_CD || ':' || REC0.INTUIT_BANK_ACCOUNT_FK || ':' || REC0.CREDIT_DEBIT_INDICATOR
			     INTO v_accum_batch_key
				 FROM DUAL;

			  v_accum_rec_num := v_accum_batch_table(v_accum_batch_key);

			  if v_accum_rec_num = 0 then
			     v_accum_rec_key := null;
				 v_accum_rec := null;
		      else
			     v_accum_rec_key := v_accum_batch_key  || ':' || v_accum_rec_num;
			     v_accum_rec := v_accum_rec_table(v_accum_rec_key);
			  end if;


			  if v_accum_rec_num > 0 and v_accum_rec.amount + REC0.AMOUNT <= v_nacha_max_amount then
		         -- update existing accumulated record
			  	 v_accum_rec.amount := v_accum_rec.amount + REC0.AMOUNT;
				 v_accum_rec.txn_end_sort_key := REC0.SORT_KEY;
				 v_accum_rec_table(v_accum_rec_key) := v_accum_rec;
			  else
			     -- over NACHA limit -> create new record
				 v_accum_rec_num := v_accum_rec_num + 1;
				 v_accum_batch_table(v_accum_batch_key) := v_accum_rec_num;
				 v_accum_rec_key := v_accum_batch_key || ':' || TO_CHAR(v_accum_rec_num);
				 select REC0.N_A_C_H_A_BATCH_TYPE, REC0.SOURCE_SYSTEM_CD, REC0.INTUIT_BANK_ACCOUNT_FK, REC0.CREDIT_DEBIT_INDICATOR, REC0.AMOUNT, REC0.SORT_KEY, REC0.SORT_KEY
				    into v_accum_rec
					from dual;
				 v_accum_rec_table(v_accum_rec_key) := v_accum_rec;
			  end if;

			END LOOP;
		END accumulate_intuit_batch;


		PROCEDURE update_accum_txn_trace_number
		IS
		BEGIN
			v_accum_batch_key := v_accum_batch_table.first;
			while v_accum_batch_key is not null
			loop
			   v_accum_rec_num := v_accum_batch_table(v_accum_batch_key);
			   for i in 1..v_accum_rec_num loop
			       v_accum_rec_key := v_accum_batch_key || ':' || to_char(i);
			       v_accum_rec := v_accum_rec_table(v_accum_rec_key);

				   MERGE INTO PSP_ENTRY_DETAIL_RECORD dst
				   USING (
				       SELECT ENTRY_DETAIL_RECORD_SEQ,
						      SOURCE_SYSTEM_CD,
							  N_A_C_H_A_BATCH_TYPE,
							  INTUIT_BANK_ACCOUNT_FK,
							  CREDIT_DEBIT_INDICATOR,
					          SORT_KEY
					     FROM
                             (
						       SELECT REC1.ENTRY_DETAIL_RECORD_SEQ,
								      C1.SOURCE_SYSTEM_CD,
									  TT1.N_A_C_H_A_BATCH_TYPE,
									  REC1.INTUIT_BANK_ACCOUNT_FK,
									  REC1.CREDIT_DEBIT_INDICATOR,
							          ROW_NUMBER () OVER (ORDER BY TT1.N_A_C_H_A_BATCH_TYPE, C1.SOURCE_SYSTEM_CD, REC1.INTUIT_BANK_ACCOUNT_FK, REC1.AMOUNT, REC1.ENTRY_DETAIL_RECORD_SEQ) SORT_KEY
							    FROM PSP_ENTRY_DETAIL_RECORD REC1,
								     PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
								     PSP_TRANSACTION_TYPE TT1,
								     PSP_COMPANY C1
								WHERE REC1.N_A_C_H_A_FILE_FK = v_nacha_file_id
								   AND REC1.MONEY_MOVEMENT_TRANSACTION_FK = MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ
								   AND REC1.INTUIT_BANK_ACCOUNT_FK IS NOT NULL
								   AND C1.COMPANY_SEQ = MMT1.COMPANY_FK
								   AND TT1.TRANSACTION_TYPE_CD =
								     (
									   SELECT FT2.TRANSACTION_TYPE_FK
									     FROM PSP_FINANCIAL_TRANSACTION FT2
										 WHERE FT2.MONEY_MOVEMENT_TRANSACTION_FK = MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ
										   AND (FT2.DEBIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
										        OR FT2.CREDIT_BANK_ACCOUNT_TYPE = v_bank_owner_type
												OR (p_nacha_file_type = 'CCD'
											         AND FT2.DEBIT_BANK_ACCOUNT_TYPE = 'Intuit'
												     AND FT2.CREDIT_BANK_ACCOUNT_TYPE = 'Intuit'
												   )
											   )
										  AND ROWNUM = 1 -- to account for multiple FTs per MMTxn
									 )
							  )
					     WHERE SOURCE_SYSTEM_CD = v_accum_rec.src_system_cd
						   AND N_A_C_H_A_BATCH_TYPE = v_accum_rec.nacha_batch_type
						   AND INTUIT_BANK_ACCOUNT_FK = v_accum_rec.intuit_bank_account_id
						   AND CREDIT_DEBIT_INDICATOR = v_accum_rec.credit_debit_ind
						   AND SORT_KEY BETWEEN v_accum_rec.txn_begin_sort_key AND v_accum_rec.txn_end_sort_key
					) src
					ON	(src.ENTRY_DETAIL_RECORD_SEQ = dst.ENTRY_DETAIL_RECORD_SEQ)
					WHEN MATCHED
					THEN UPDATE
					     SET VERSION = VERSION + 1,
							  MODIFIER_ID = p_user_id,
							  MODIFIED_DATE = v_utc_date,
							  dst.trace_number = v_next_trace_number;
				   v_next_trace_number := v_next_trace_number + 1;
			   end loop;
			   v_accum_batch_key := v_accum_batch_table.next(v_accum_batch_key);
			end loop;
		END update_accum_txn_trace_number;

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
	         AND MMT.MONEY_MOVEMENT_TRANSACTION_SEQ = FT.MONEY_MOVEMENT_TRANSACTION_FK
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

	   init_accumulated_batches();

	   SELECT SEQ_TRACE_NBR.NEXTVAL INTO v_next_trace_number FROM DUAL;

	   create_nacha_file_rec();
	   update_txn_trace_number();
	   accumulate_intuit_batch();
	   update_accum_txn_trace_number();

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
		   WHERE EXISTS
		      (
			   	 SELECT FT1.FINANCIAL_TRANSACTION_SEQ
				     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
				          PSP_FINANCIAL_TRANSACTION FT1
				     WHERE FT1.FINANCIAL_TRANSACTION_SEQ = FT0.FINANCIAL_TRANSACTION_SEQ
					   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
					   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Created'
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
		   WHERE EXISTS
			  (
			     SELECT PR.PAYROLL_RUN_SEQ
				     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
				          PSP_FINANCIAL_TRANSACTION FT1
				     WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
					   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
					   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
					   AND PR.PAYROLL_RUN_STATUS != 'OffloadedAll'
					   AND FT1.TRANSACTION_TYPE_FK = 'EmployerDdDebit'
			  );

	    UPDATE PSP_PAYROLL_RUN PR
		   SET PAYROLL_RUN_STATUS = 'AutoRedebitOffloaded',
		       VERSION = VERSION + 1,
			   MODIFIER_ID = p_user_id,
			   MODIFIED_DATE = v_utc_date
		   WHERE EXISTS
			  (
			     SELECT PR.PAYROLL_RUN_SEQ
				     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
				          PSP_FINANCIAL_TRANSACTION FT1,
						  PSP_TRANSACTION_TYPE TT1
				     WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
					   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
					   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
					   AND PR.PAYROLL_RUN_STATUS = 'PendingAutoRedebit'
					   AND FT1.TRANSACTION_TYPE_FK = TT1.TRANSACTION_TYPE_CD
					   AND TT1.ASSOCIATION_TYPE = 'Redebit'
			  );

	    UPDATE PSP_PAYROLL_RUN PR
		   SET PAYROLL_RUN_STATUS = 'RedebitOffloaded',
		       VERSION = VERSION + 1,
			   MODIFIER_ID = p_user_id,
			   MODIFIED_DATE = v_utc_date
		   WHERE EXISTS
			  (
			     SELECT PR.PAYROLL_RUN_SEQ
				     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
				          PSP_FINANCIAL_TRANSACTION FT1,
						  PSP_TRANSACTION_TYPE TT1
				     WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
					   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
					   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
					   AND PR.PAYROLL_RUN_STATUS = 'PendingRedebit'
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
			     SELECT PR.PAYROLL_RUN_SEQ
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
		   WHERE EXISTS
			  (
			     SELECT PR.PAYROLL_RUN_SEQ
				     FROM PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
				          PSP_FINANCIAL_TRANSACTION FT1
				     WHERE FT1.PAYROLL_RUN_FK = PR.PAYROLL_RUN_SEQ
					   AND MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
				       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
					   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
					   AND FT1.TRANSACTION_TYPE_FK = 'EmployeeDdReversalDebit'
					   AND PR.PAYROLL_RUN_STATUS = 'PendingReversals'
			  );
	END update_payroll_run_status;


	PROCEDURE create_fee_offloaded_event
	IS

		CURSOR c1 IS
		    SELECT
	            FN_FORMAT_SYSGUID(SYS_GUID()),
				FN_FORMAT_SYSGUID(SYS_GUID()),
				c1.current_token + row_number() over (partition by COMPANY_SEQ order by FT1.CREATED_DATE, FT1.financial_transaction_seq) as current_token,
				c1.company_seq,
				ft1.financial_transaction_seq
			FROM
			    PSP_MONEY_MOVEMENT_TRANSACTION MMT1,
		        PSP_FINANCIAL_TRANSACTION FT1,
				PSP_TRANSACTION_TYPE TT1,
				PSP_COMPANY C1,
				PSP_EVTTP_SRCSYS_ASSOC ES1
		     WHERE MMT1.OFFLOAD_BATCH_FK = p_offload_batch_id
		       AND MMT1.MONEY_MOVEMENT_TRANSACTION_SEQ = FT1.MONEY_MOVEMENT_TRANSACTION_FK
			   AND FT1.CURRENT_TRANSACTION_STATE_FK = 'Executed'
			   AND C1.COMPANY_SEQ = FT1.COMPANY_FK
			   AND C1.SOURCE_SYSTEM_CD = ES1.SOURCE_SYSTEM_FK
			   AND ES1.INTERESTING_EVENT_TYPES_FK = 'FeeOffloaded'
			   AND FT1.TRANSACTION_TYPE_FK = TT1.TRANSACTION_TYPE_CD
			   AND TT1.FEE_IND = 1
			   -- can't join because of duplicate SKUs in PSP_OFFERING_SVCCHG
			   AND EXISTS (SELECT OSC2.S_K_U FROM PSP_OFFERING_SVCCHG OSC2 WHERE OSC2.S_K_U = FT1.SKU AND OSC2.SKU_TYPE = 'NonPayroll');

		TYPE event_id_tbl_type IS TABLE OF psp_company_event.company_event_seq%TYPE INDEX BY PLS_INTEGER;
		TYPE event_detail_id_tbl_type IS TABLE OF psp_company_event_detail.company_event_detail_seq%TYPE INDEX BY PLS_INTEGER;
		TYPE event_token_tbl_type IS TABLE OF psp_company_event.event_token%TYPE INDEX BY PLS_INTEGER;
		TYPE company_id_tbl_type IS TABLE OF psp_company.company_seq%TYPE INDEX BY PLS_INTEGER;
		TYPE fin_txn_id_tbl_type IS TABLE OF psp_financial_transaction.financial_transaction_seq%TYPE INDEX BY PLS_INTEGER;
		event_id_table event_id_tbl_type;
		event_detail_id_tbl event_detail_id_tbl_type;
		event_token_tbl event_token_tbl_type;
		company_id_tbl company_id_tbl_type;
		fin_txn_id_tbl fin_txn_id_tbl_type;
	BEGIN

	    OPEN c1;
		LOOP
		    FETCH c1 BULK COLLECT INTO event_id_table, event_detail_id_tbl, event_token_tbl, company_id_tbl, fin_txn_id_tbl LIMIT 100;

			FORALL i IN event_id_table.first..event_id_table.last
				INSERT INTO PSP_COMPANY_EVENT(
		   			COMPANY_EVENT_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID,
		   			EVENT_TIME_STAMP, STATUS_EFFECTIVE_DATE, STATUS_CD, EVENT_TYPE_CD, EVENT_TOKEN, EMAIL_STATUS_EFFECTIVE_DATE, EMAIL_RETRY_COUNT, EMAIL_STATUS, SOURCE_ID, NOTE_LAST_UPDATED_DATE, COMPANY_FK)
				VALUES (event_id_table(i), 0, p_user_id, v_utc_date, p_user_id, v_utc_date, -1,
				        v_psp_date, v_psp_date, 'Active', 'FeeOffloaded', event_token_tbl(i), v_psp_date, 0, 'Ignore', NULL, NULL, company_id_tbl(i));

		    FORALL i IN event_id_table.first..event_id_table.last
				INSERT INTO PSP_COMPANY_EVENT_DETAIL
					(COMPANY_EVENT_DETAIL_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID,
					 VALUE, EVENT_DETAIL_TYPE_CD, COMPANY_EVENT_FK)
				VALUES (event_detail_id_tbl(i), 0, p_user_id, v_utc_date, p_user_id, v_utc_date, -1,
				        fin_txn_id_tbl(i), 'FinancialTransactionId', event_id_table(i));

		    FORALL i IN event_id_table.first..event_id_table.last
			  UPDATE PSP_COMPANY
			  SET CURRENT_TOKEN = CURRENT_TOKEN + 1,
			      VERSION = VERSION + 1,
				  MODIFIER_ID = p_user_id,
				  MODIFIED_DATE = v_utc_date
			  WHERE
			      COMPANY_SEQ = company_id_tbl(i);

			EXIT WHEN c1%NOTFOUND;
		END LOOP;
		CLOSE c1;

	END create_fee_offloaded_event;

BEGIN
	SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL;
	--SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP) INTO v_utc_date FROM DUAL;
	v_utc_date := p_app_server_date;


	create_offload_batch();
	create_nacha_file('CCD');
	create_nacha_file('PPD');
	update_financial_txn_status();
	update_payroll_run_status();
	create_fee_offloaded_event();
END PRC_OFFLOAD;

/

SHOW ERRORS;

 PROMPT finishedDBUpgradeFrom_1.9.9.28_To_1.9.9.29.sql