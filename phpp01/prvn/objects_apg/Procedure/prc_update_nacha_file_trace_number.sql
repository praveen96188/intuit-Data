CREATE OR REPLACE PROCEDURE prc_update_nacha_file_trace_number (p_RETURN_CD_new  INOUT   text ,
                                                            p_ERROR_DESC_new INOUT   varchar,
                                                            p_offload_batch_id_new IN varchar,
                                                            p_offload_date_new     IN TIMESTAMP,
                                                            p_user_id_new    IN       VARCHAR,
                                                            v_utc_date_new   TIMESTAMP,
                                                            p_nacha_file_type IN VARCHAR,
                                                            v_psp_date_new IN TIMESTAMP)
LANGUAGE plpgsql AS
$$
DECLARE
  v_nacha_file_id           VARCHAR (100);
  total_rows                NUMERIC;
  BEGIN
      CALL PRC_SET_PSP_EVENT_LOG (
          p_RETURN_CD_new,
          p_ERROR_DESC_new,
          'N/A',                          -- p_CompanyId          IN   VARCHAR,
          null,                         -- p_TypeCd             IN   VARCHAR,
          'PROD',                         -- p_DomainName         IN   VARCHAR,
          'PSP',                          --p_ArchName           IN   VARCHAR,
          'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR,
          'N/A',                          -- p_HostName           IN   VARCHAR,
          'Offload Stored Proc',          -- Application_name           IN   VARCHAR,
          'PSP_ENTRY_DETAIL_RECORD',   --  p_ObjectName         IN   VARCHAR,
          'N/A',                          --p_UserName           IN   VARCHAR,
          to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
          'Updating ENTRY_DETAIL_RECORD ' || p_nacha_file_type );

      SELECT nachafile_seq
             INTO v_nacha_file_id
      FROM PSP_NACHAFILE
      WHERE offload_batch_fk = p_offload_batch_id_new AND
          file_type = p_nacha_file_type;

      -- Update trace number for entry detail records associated with nacha file
      -- nvl on record_data with some value so that the sort order is preserved
      -- no need to do above, checked with Dawn..
      UPDATE psp_entry_detail_record
      SET trace_number  = edr.generated_trace_number,
          VERSION = VERSION + 1,
          modifier_id = p_user_id_new,
          modified_date = v_utc_date_new
        from (SELECT rec0.entry_detail_record_seq,
             (CASE
                  WHEN COALESCE(record_data_enc, '0') = '0' THEN NULL
                  ELSE (nextval('SEQ_TRACE_NUMBER'))
                 END) generated_trace_number
      FROM psp_entry_detail_record rec0
      WHERE rec0.n_a_c_h_a_file_fk = v_nacha_file_id
        AND rec0.initiation_date = p_offload_date_new
      order by rec0.legal_name,
               rec0.company_fk,
               rec0.n_a_c_h_a_batch_type,
               rec0.settlement_date,
               rec0.record_data_enc,
               rec0.amount,
               rec0.entry_detail_record_seq) edr
      where edr.entry_detail_record_seq = psp_entry_detail_record.entry_detail_record_seq;

      GET DIAGNOSTICS total_rows := ROW_COUNT;

      IF total_rows != 0 THEN
        UPDATE PSP_NACHAFILE
        SET STATUS = 'Finalized',
            VERSION = VERSION + 1,
            MODIFIER_ID = p_user_id_new,
            MODIFIED_DATE = v_utc_date_new,
            FINALIZATION_DATE = v_psp_date_new,
            STATUS_EFFECTIVE_DATE = v_psp_date_new
        WHERE nachafile_seq = v_nacha_file_id;
      END IF;
  END;
$$