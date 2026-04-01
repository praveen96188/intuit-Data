DECLARE
	v_last_holiday date;           
	v_initiation_date date;
    v_initiation_date_ts timestamp;
    v_begin_dst date;
    v_end_dst date;
    v_is_holiday int;
    v_weekday int;
    v_offload_batch_exists int;
    
    v_offload_batch_id           VARCHAR2 (100);
    v_nacha_file_id           VARCHAR2 (100);
    v_next_file_id_modifier   VARCHAR2 (1);
    v_dd_offload_batch_begin_dst date;

BEGIN
    SELECT MAX(BANK_HOLIDAY_DATE) INTO v_last_holiday FROM PSP_BANK_HOLIDAY;   
    SELECT date  '2007-01-01' INTO v_initiation_date  FROM DUAL;
    SELECT date '2007-01-01' INTO v_dd_offload_batch_begin_dst FROM DUAL;
    
    --------------------------------------------------------------------------------------------------------------------------------------
    -- Insert offload batch and nacha file records until the last holiday
    --------------------------------------------------------------------------------------------------------------------------------------  
    WHILE v_initiation_date < v_last_holiday
    LOOP
            SELECT
                next_day(to_date('01-MAR-'||to_char(v_initiation_date,'YYYY'),'DD-MON-YYYY' ) -1, 'SUNDAY') + 7,
                next_day(to_date('01-NOV-'||to_char(v_initiation_date,'YYYY'),'DD-MON-YYYY' ) -1, 'SUNDAY') 
             INTO v_begin_dst, v_end_dst
             FROM dual;

             IF v_initiation_date >= v_begin_dst AND v_initiation_date <= v_end_dst THEN
                 v_initiation_date_ts := to_timestamp(to_char(v_initiation_date, 'YYYY-MM-DD') || '07:00:00',  'yyyy-mm-dd hh24:mi:ss');
             ELSE
                v_initiation_date_ts := to_timestamp(to_char(v_initiation_date, 'YYYY-MM-DD') || '08:00:00',  'yyyy-mm-dd hh24:mi:ss'); 
             END IF;
             
             SELECT  TO_CHAR(v_initiation_date,'D') INTO v_weekday FROM dual;               
             SELECT COUNT(*) INTO v_is_holiday FROM PSP_BANK_HOLIDAY  WHERE trunc(BANK_HOLIDAY_DATE) = trunc(v_initiation_date);
     
             IF (v_weekday != 1 AND  v_weekday != 7 AND  v_is_holiday = 0) THEN
                  --------------------------------------------------------------------------------------------------------------------------------------
                 -- Not weekend or holiday: insert psp_offload_batch for standard offload group and PPD and CCD nacha file records
                 --------------------------------------------------------------------------------------------------------------------------------------
                 
                 -- STD offload group
                 SELECT COUNT(*) INTO v_offload_batch_exists FROM PSP_OFFLOAD_BATCH  WHERE OFFLOAD_DATE = v_initiation_date_ts AND OFFLOAD_GROUP_FK =  '3b67b658-dc4e-012a-fc4f-005056c02727';
                 
                 IF (v_offload_batch_exists = 0) THEN
                     SELECT fn_format_sysguid (SYS_GUID ())  INTO v_offload_batch_id FROM DUAL;
                 
                     INSERT INTO PSP_OFFLOAD_BATCH  
                              (OFFLOAD_BATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIER_ID,MODIFIED_DATE,REALM_ID,STATUS_CD,STATUS_EFFECIVE_DATE,OFFLOAD_DATE,IS_OFFLDTXN_EVT_COMPLETE ,OFFLOAD_GROUP_FK)
                     VALUES
                               (v_offload_batch_id, 0, 'System', systimestamp, 'System', systimestamp, -1, 'InProcess', systimestamp, v_initiation_date_ts, 0, '3b67b658-dc4e-012a-fc4f-005056c02727');
                 
                     -- CCD nacha file
                     SELECT fn_format_sysguid (SYS_GUID ())  INTO v_nacha_file_id FROM DUAL;
                     SELECT CHR (seq_ach_file_ctr.NEXTVAL + ASCII ('A') - 1) INTO v_next_file_id_modifier FROM DUAL;

                     INSERT INTO psp_nachafile
                                 (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                  realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                  status_effective_date, transmission_date, credit_txn_total_amount,
                                  debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                 )
                     VALUES (v_nacha_file_id, 0,  'System', systimestamp,  'System', systimestamp,
                                  -1, NULL, NULL, NULL, systimestamp,
                                  systimestamp, NULL, 0,
                                  0, 'InProcess', 'CCD', v_next_file_id_modifier, v_offload_batch_id
                                 );
   
                     -- PPD nacha file
                     SELECT fn_format_sysguid (SYS_GUID ())  INTO v_nacha_file_id FROM DUAL;
                     SELECT CHR (seq_ach_file_ctr.NEXTVAL + ASCII ('A') - 1) INTO v_next_file_id_modifier FROM DUAL;

                     INSERT INTO psp_nachafile
                                 (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                  realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                  status_effective_date, transmission_date, credit_txn_total_amount,
                                  debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                )
                     VALUES (v_nacha_file_id, 0,  'System', systimestamp,  'System', systimestamp,
                                  -1, NULL, NULL, NULL, systimestamp,
                                  systimestamp, NULL, 0,
                                  0, 'InProcess', 'PPD', v_next_file_id_modifier, v_offload_batch_id
                                 );
                 END IF;
                 
                 --------------------------------------------------------------------------------------------------------------------------------------
                 -- Not weekend or holiday: insert psp_offload_batch for tax payment offload group and CCD+ nacha file record                          
                 --------------------------------------------------------------------------------------------------------------------------------------
                 
                 -- TXP offload group
                 SELECT COUNT(*) INTO v_offload_batch_exists FROM PSP_OFFLOAD_BATCH  WHERE OFFLOAD_DATE = v_initiation_date_ts AND OFFLOAD_GROUP_FK =  '3b672727-dc4e-012a-fc4f-005056c02727';
                 
                 IF (v_offload_batch_exists = 0) THEN
                     SELECT fn_format_sysguid (SYS_GUID ())  INTO v_offload_batch_id FROM DUAL;
                  
                     INSERT INTO PSP_OFFLOAD_BATCH  
                              (OFFLOAD_BATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIER_ID,MODIFIED_DATE,REALM_ID,STATUS_CD,STATUS_EFFECIVE_DATE,OFFLOAD_DATE,IS_OFFLDTXN_EVT_COMPLETE ,OFFLOAD_GROUP_FK)
                     VALUES
                               (v_offload_batch_id, 0, 'System', systimestamp, 'System', systimestamp, -1, 'InProcess', systimestamp, v_initiation_date_ts, 0, '3b672727-dc4e-012a-fc4f-005056c02727');
                           
                    -- CCD+ nacha file
                    SELECT fn_format_sysguid (SYS_GUID ())  INTO v_nacha_file_id FROM DUAL;
                    SELECT CHR (seq_ach_file_ctr.NEXTVAL + ASCII ('A') - 1) INTO v_next_file_id_modifier FROM DUAL;

                    INSERT INTO psp_nachafile
                                 (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                  realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                  status_effective_date, transmission_date, credit_txn_total_amount,
                                debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                )
                    VALUES (v_nacha_file_id, 0,  'System', systimestamp,  'System', systimestamp,
                                  -1, NULL, NULL, NULL, systimestamp,
                                  systimestamp, NULL, 0,
                                  0, 'InProcess', 'CCDPlus', v_next_file_id_modifier, v_offload_batch_id
                                );
                END IF;
                
                IF (v_initiation_date >= v_dd_offload_batch_begin_dst) THEN
                   --------------------------------------------------------------------------------------------------------------------------------------
                   -- Not weekend or holiday: insert psp_offload_batch for DD offload group and DD nacha file record in case where PSP does offload
                   --------------------------------------------------------------------------------------------------------------------------------------

                   -- PSP-13970 PSPO offload group
                   SELECT COUNT(*) INTO v_offload_batch_exists FROM PSP_OFFLOAD_BATCH  WHERE OFFLOAD_DATE = v_initiation_date_ts AND OFFLOAD_GROUP_FK =  '3b672729-dc4e-012a-fc4f-005056c02727';

                   IF (v_offload_batch_exists = 0) THEN
                       SELECT fn_format_sysguid (SYS_GUID ())  INTO v_offload_batch_id FROM DUAL;

                       INSERT INTO PSP_OFFLOAD_BATCH
                                (OFFLOAD_BATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIER_ID,MODIFIED_DATE,REALM_ID,STATUS_CD,STATUS_EFFECIVE_DATE,OFFLOAD_DATE,IS_OFFLDTXN_EVT_COMPLETE ,OFFLOAD_GROUP_FK)
                       VALUES
                                 (v_offload_batch_id, 0, 'System', systimestamp, 'System', systimestamp, -1, 'InProcess', systimestamp, v_initiation_date_ts, 0, '3b672729-dc4e-012a-fc4f-005056c02727');

                      -- CCD nacha file
                      SELECT fn_format_sysguid (SYS_GUID ())  INTO v_nacha_file_id FROM DUAL;
                      SELECT CHR (seq_ach_file_ctr.NEXTVAL + ASCII ('A') - 1) INTO v_next_file_id_modifier FROM DUAL;

                      INSERT INTO psp_nachafile
                                   (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                    realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                    status_effective_date, transmission_date, credit_txn_total_amount,
                                  debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                  )
                      VALUES (v_nacha_file_id, 0,  'System', systimestamp,  'System', systimestamp,
                                    -1, NULL, NULL, NULL, systimestamp,
                                    systimestamp, NULL, 0,
                                    0, 'InProcess', 'CCD', v_next_file_id_modifier, v_offload_batch_id
                                  );
                      -- PPD nacha file
                      SELECT fn_format_sysguid (SYS_GUID ())  INTO v_nacha_file_id FROM DUAL;
                      SELECT CHR (seq_ach_file_ctr.NEXTVAL + ASCII ('A') - 1) INTO v_next_file_id_modifier FROM DUAL;

                       INSERT INTO psp_nachafile
                                   (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                    realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                    status_effective_date, transmission_date, credit_txn_total_amount,
                                    debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                  )
                       VALUES (v_nacha_file_id, 0,  'System', systimestamp,  'System', systimestamp,
                                    -1, NULL, NULL, NULL, systimestamp,
                                    systimestamp, NULL, 0,
                                    0, 'InProcess', 'PPD', v_next_file_id_modifier, v_offload_batch_id
                                   );
                  END IF;
                  --------------------------------------------------------------------------------------------------------------------------------------
                   -- Not weekend or holiday: insert psp_offload_batch for DD offload group and DD nacha file record in case where DD does offload
                   --------------------------------------------------------------------------------------------------------------------------------------

                  --PSP-13970  STD_DDS offload group
                   SELECT COUNT(*) INTO v_offload_batch_exists FROM PSP_OFFLOAD_BATCH  WHERE OFFLOAD_DATE = v_initiation_date_ts AND OFFLOAD_GROUP_FK =  '3b672730-dc4e-012a-fc4f-005056c02727';

                   IF (v_offload_batch_exists = 0) THEN
                       SELECT fn_format_sysguid (SYS_GUID ())  INTO v_offload_batch_id FROM DUAL;

                       INSERT INTO PSP_OFFLOAD_BATCH
                                (OFFLOAD_BATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIER_ID,MODIFIED_DATE,REALM_ID,STATUS_CD,STATUS_EFFECIVE_DATE,OFFLOAD_DATE,IS_OFFLDTXN_EVT_COMPLETE ,OFFLOAD_GROUP_FK)
                       VALUES
                                 (v_offload_batch_id, 0, 'System', systimestamp, 'System', systimestamp, -1, 'InProcess', systimestamp, v_initiation_date_ts, 0, '3b672730-dc4e-012a-fc4f-005056c02727');

                      -- CCD nacha file
                      SELECT fn_format_sysguid (SYS_GUID ())  INTO v_nacha_file_id FROM DUAL;
                      SELECT CHR (seq_ach_file_ctr.NEXTVAL + ASCII ('A') - 1) INTO v_next_file_id_modifier FROM DUAL;

                      INSERT INTO psp_nachafile
                                   (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                    realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                    status_effective_date, transmission_date, credit_txn_total_amount,
                                  debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                  )
                      VALUES (v_nacha_file_id, 0,  'System', systimestamp,  'System', systimestamp,
                                    -1, NULL, NULL, NULL, systimestamp,
                                    systimestamp, NULL, 0,
                                    0, 'InProcess', 'CCD', v_next_file_id_modifier, v_offload_batch_id
                                  );
                      -- PPD nacha file
                      SELECT fn_format_sysguid (SYS_GUID ())  INTO v_nacha_file_id FROM DUAL;
                      SELECT CHR (seq_ach_file_ctr.NEXTVAL + ASCII ('A') - 1) INTO v_next_file_id_modifier FROM DUAL;

                       INSERT INTO psp_nachafile
                                   (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                    realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                    status_effective_date, transmission_date, credit_txn_total_amount,
                                    debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                  )
                       VALUES (v_nacha_file_id, 0,  'System', systimestamp,  'System', systimestamp,
                                    -1, NULL, NULL, NULL, systimestamp,
                                    systimestamp, NULL, 0,
                                    0, 'InProcess', 'PPD', v_next_file_id_modifier, v_offload_batch_id
                                   );
                  END IF;
                END IF;
                
            END IF;


            
           v_initiation_date := v_initiation_date + 1;     
    END LOOP;
    
END;

/
COMMIT;

