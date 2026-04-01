set search_path to pspadm;
DO $$
DECLARE
    v_last_holiday date;
	v_initiation_date date;
    v_initiation_date_ts timestamp;
    v_begin_dst date;
    v_end_dst date;
    v_is_holiday int;
    v_weekday int;
    v_offload_batch_exists int;

    v_offload_batch_id           VARCHAR (100);
    v_nacha_file_id           VARCHAR (100);
    v_next_file_id_modifier   VARCHAR (1);
    v_dd_offload_batch_begin_dst date;

BEGIN
    SELECT MAX(BANK_HOLIDAY_DATE) INTO v_last_holiday FROM PSP_BANK_HOLIDAY;
    v_initiation_date := '2007-01-01';
    v_dd_offload_batch_begin_dst := '2007-01-01';
    --------------------------------------------------------------------------------------------------------------------------------------
    -- Insert offload batch and nacha file records until the last holiday
    --------------------------------------------------------------------------------------------------------------------------------------
    WHILE v_initiation_date < v_last_holiday
    LOOP
            SELECT
                (date_trunc('year', v_initiation_date) + interval '2 month' - interval '1 day')::date + ((7 - extract('dow' from (date_trunc('year', v_initiation_date) + interval '2 month' - interval '1 day')::date))::int + 1) % 7,
                (date_trunc('year', v_initiation_date) + interval '10 month' - interval '1 day')::date + ((7 - extract('dow' from (date_trunc('year', v_initiation_date) + interval '10 month' - interval '1 day')::date))::int + 1) % 7
            INTO v_begin_dst, v_end_dst;

            IF v_initiation_date >= v_begin_dst AND v_initiation_date <= v_end_dst THEN
                v_initiation_date_ts := to_timestamp(to_char(v_initiation_date, 'YYYY-MM-DD') || ' 07:00:00',  'yyyy-mm-dd hh24:mi:ss');
            ELSE
                v_initiation_date_ts := to_timestamp(to_char(v_initiation_date, 'YYYY-MM-DD') || ' 08:00:00',  'yyyy-mm-dd hh24:mi:ss');
            END IF;

            SELECT  EXTRACT(DOW FROM v_initiation_date) INTO v_weekday;
            SELECT COUNT(*) INTO v_is_holiday FROM PSP_BANK_HOLIDAY  WHERE date(BANK_HOLIDAY_DATE) = date(v_initiation_date);

            IF (v_weekday != 0 AND  v_weekday != 6 AND  v_is_holiday = 0) THEN
                         --------------------------------------------------------------------------------------------------------------------------------------
                             -- Not weekend or holiday: insert psp_offload_batch for standard offload group and PPD and CCD nacha file records
                             --------------------------------------------------------------------------------------------------------------------------------------

                        -- STD offload group
                        SELECT COUNT(*) INTO v_offload_batch_exists FROM PSP_OFFLOAD_BATCH  WHERE OFFLOAD_DATE = v_initiation_date_ts AND OFFLOAD_GROUP_FK =  '3b67b658-dc4e-012a-fc4f-005056c02727';

                        IF (v_offload_batch_exists = 0) THEN
                            SELECT gen_random_uuid()  INTO v_offload_batch_id ;

                            INSERT INTO PSP_OFFLOAD_BATCH
                                    (OFFLOAD_BATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIER_ID,MODIFIED_DATE,REALM_ID,STATUS_CD,STATUS_EFFECIVE_DATE,OFFLOAD_DATE,IS_OFFLDTXN_EVT_COMPLETE ,OFFLOAD_GROUP_FK)
                            VALUES
                                    (v_offload_batch_id, 0, 'System', current_timestamp, 'System', current_timestamp, -1, 'InProcess', current_timestamp, v_initiation_date_ts, 0, '3b67b658-dc4e-012a-fc4f-005056c02727');

                            -- CCD nacha file
                            SELECT gen_random_uuid()  INTO v_nacha_file_id ;
                            SELECT CHR ((nextval('seq_ach_file_ctr') + ASCII ('A') - 1)::integer) INTO v_next_file_id_modifier ;

                            INSERT INTO psp_nachafile
                                    (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                     realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                     status_effective_date, transmission_date, credit_txn_total_amount,
                                     debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                    )
                            VALUES (v_nacha_file_id, 0,  'System', current_timestamp,  'System', current_timestamp,
                                    -1, NULL, NULL, NULL, current_timestamp,
                                    current_timestamp, NULL, 0,
                                    0, 'InProcess', 'CCD', v_next_file_id_modifier, v_offload_batch_id
                                   );

                            -- PPD nacha file
                            SELECT gen_random_uuid()  INTO v_nacha_file_id ;
                            SELECT CHR ((nextval('seq_ach_file_ctr') + ASCII ('A') - 1)::integer) INTO v_next_file_id_modifier ;

                            INSERT INTO psp_nachafile
                                    (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                     realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                     status_effective_date, transmission_date, credit_txn_total_amount,
                                     debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                    )
                            VALUES (v_nacha_file_id, 0,  'System', current_timestamp,  'System', current_timestamp,
                                    -1, NULL, NULL, NULL, current_timestamp,
                                    current_timestamp, NULL, 0,
                                    0, 'InProcess', 'PPD', v_next_file_id_modifier, v_offload_batch_id
                                   );
                    END IF;

                 --------------------------------------------------------------------------------------------------------------------------------------
                 -- Not weekend or holiday: insert psp_offload_batch for tax payment offload group and CCD+ nacha file record
                 --------------------------------------------------------------------------------------------------------------------------------------

                 -- TXP offload group
                    SELECT COUNT(*) INTO v_offload_batch_exists FROM PSP_OFFLOAD_BATCH  WHERE OFFLOAD_DATE = v_initiation_date_ts AND OFFLOAD_GROUP_FK =  '3b672727-dc4e-012a-fc4f-005056c02727';

                    IF (v_offload_batch_exists = 0) THEN
                        SELECT gen_random_uuid()  INTO v_offload_batch_id ;

                        INSERT INTO PSP_OFFLOAD_BATCH
                            (OFFLOAD_BATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIER_ID,MODIFIED_DATE,REALM_ID,STATUS_CD,STATUS_EFFECIVE_DATE,OFFLOAD_DATE,IS_OFFLDTXN_EVT_COMPLETE ,OFFLOAD_GROUP_FK)
                        VALUES
                            (v_offload_batch_id, 0, 'System', current_timestamp, 'System', current_timestamp, -1, 'InProcess', current_timestamp, v_initiation_date_ts, 0, '3b672727-dc4e-012a-fc4f-005056c02727');

                        -- CCD+ nacha file
                        SELECT gen_random_uuid()  INTO v_nacha_file_id ;
                        SELECT CHR ((nextval('seq_ach_file_ctr') + ASCII ('A') - 1)::integer) INTO v_next_file_id_modifier ;


                        INSERT INTO psp_nachafile
                            (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                             realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                             status_effective_date, transmission_date, credit_txn_total_amount,
                             debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                            )
                        VALUES (v_nacha_file_id, 0,  'System', current_timestamp,  'System', current_timestamp,
                                -1, NULL, NULL, NULL, current_timestamp,
                                current_timestamp, NULL, 0,
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
                            SELECT gen_random_uuid()  INTO v_offload_batch_id ;

                            INSERT INTO PSP_OFFLOAD_BATCH
                                (OFFLOAD_BATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIER_ID,MODIFIED_DATE,REALM_ID,STATUS_CD,STATUS_EFFECIVE_DATE,OFFLOAD_DATE,IS_OFFLDTXN_EVT_COMPLETE ,OFFLOAD_GROUP_FK)
                            VALUES
                                (v_offload_batch_id, 0, 'System', current_timestamp, 'System', current_timestamp, -1, 'InProcess', current_timestamp, v_initiation_date_ts, 0, '3b672729-dc4e-012a-fc4f-005056c02727');

                            -- CCD nacha file
                            SELECT gen_random_uuid()  INTO v_nacha_file_id ;
                            SELECT CHR ((nextval('seq_ach_file_ctr') + ASCII ('A') - 1)::integer) INTO v_next_file_id_modifier ;

                            INSERT INTO psp_nachafile
                                    (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                     realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                     status_effective_date, transmission_date, credit_txn_total_amount,
                                     debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                    )
                            VALUES (v_nacha_file_id, 0,  'System', current_timestamp,  'System', current_timestamp,
                                    -1, NULL, NULL, NULL, current_timestamp,
                                    current_timestamp, NULL, 0,
                                    0, 'InProcess', 'CCD', v_next_file_id_modifier, v_offload_batch_id
                                   );
                            -- PPD nacha file
                            SELECT gen_random_uuid()  INTO v_nacha_file_id ;
                            SELECT CHR ((nextval('seq_ach_file_ctr') + ASCII ('A') - 1)::integer) INTO v_next_file_id_modifier ;

                            INSERT INTO psp_nachafile
                                    (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                     realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                     status_effective_date, transmission_date, credit_txn_total_amount,
                                     debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                    )
                            VALUES (v_nacha_file_id, 0,  'System', current_timestamp,  'System', current_timestamp,
                                    -1, NULL, NULL, NULL, current_timestamp,
                                    current_timestamp, NULL, 0,
                                    0, 'InProcess', 'PPD', v_next_file_id_modifier, v_offload_batch_id
                                   );
                        END IF;
                      --------------------------------------------------------------------------------------------------------------------------------------
                       -- Not weekend or holiday: insert psp_offload_batch for DD offload group and DD nacha file record in case where DD does offload
                       --------------------------------------------------------------------------------------------------------------------------------------

                      --PSP-13970  STD_DDS offload group
                        SELECT COUNT(*) INTO v_offload_batch_exists FROM PSP_OFFLOAD_BATCH  WHERE OFFLOAD_DATE = v_initiation_date_ts AND OFFLOAD_GROUP_FK =  '3b672730-dc4e-012a-fc4f-005056c02727';

                        IF (v_offload_batch_exists = 0) THEN
                            SELECT gen_random_uuid()  INTO v_offload_batch_id ;

                            INSERT INTO PSP_OFFLOAD_BATCH
                            (OFFLOAD_BATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIER_ID,MODIFIED_DATE,REALM_ID,STATUS_CD,STATUS_EFFECIVE_DATE,OFFLOAD_DATE,IS_OFFLDTXN_EVT_COMPLETE ,OFFLOAD_GROUP_FK)
                            VALUES
                            (v_offload_batch_id, 0, 'System', current_timestamp, 'System', current_timestamp, -1, 'InProcess', current_timestamp, v_initiation_date_ts, 0, '3b672730-dc4e-012a-fc4f-005056c02727');

                            -- CCD nacha file
                            SELECT gen_random_uuid()  INTO v_nacha_file_id ;
                            SELECT CHR ((nextval('seq_ach_file_ctr') + ASCII ('A') - 1)::integer) INTO v_next_file_id_modifier ;

                            INSERT INTO psp_nachafile
                            (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                             realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                             status_effective_date, transmission_date, credit_txn_total_amount,
                             debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                            )
                            VALUES (v_nacha_file_id, 0,  'System', current_timestamp,  'System', current_timestamp,
                                    -1, NULL, NULL, NULL, current_timestamp,
                                    current_timestamp, NULL, 0,
                                    0, 'InProcess', 'CCD', v_next_file_id_modifier, v_offload_batch_id
                                   );
                            -- PPD nacha file
                            SELECT gen_random_uuid()  INTO v_nacha_file_id;
                            SELECT CHR ((nextval('seq_ach_file_ctr') + ASCII ('A') - 1)::integer) INTO v_next_file_id_modifier ;

                            INSERT INTO psp_nachafile
                                    (nachafile_seq, VERSION, creator_id, created_date, modifier_id, modified_date,
                                     realm_id, confirmation_code, file_name, confirmation_date, finalization_date,
                                     status_effective_date, transmission_date, credit_txn_total_amount,
                                     debit_txn_total_amount, status, file_type, file_i_d_modifier, offload_batch_fk
                                    )
                            VALUES (v_nacha_file_id, 0,  'System', current_timestamp,  'System', current_timestamp,
                                    -1, NULL, NULL, NULL, current_timestamp,
                                    current_timestamp, NULL, 0,
                                    0, 'InProcess', 'PPD', v_next_file_id_modifier, v_offload_batch_id
                                   );
                        END IF;
                    END IF;
            END IF;

           v_initiation_date := v_initiation_date + 1;
    END LOOP;

END $$;
