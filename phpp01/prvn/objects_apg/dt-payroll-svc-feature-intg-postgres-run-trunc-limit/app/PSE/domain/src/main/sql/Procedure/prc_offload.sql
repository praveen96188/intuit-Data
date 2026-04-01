CREATE OR REPLACE PROCEDURE prc_offload (
   p_offload_batch_id   IN      VARCHAR2,                       -- psp_offload_batch.offload_batch_seq
   p_offload_date       IN       TIMESTAMP,                      -- UTC Date
   p_file_type          IN       VARCHAR2,                          -- DD or Tax
   p_user_id            IN       VARCHAR2,                          -- For audit purposes
   p_app_server_date    IN       TIMESTAMP                    -- UTC Date
)
IS
   -- 11/20/09 added new hints
   -- these two variables are used in all SQL statements to populate date fields,
   -- the UTC date is used to populate SPCF audit fields created_date and modified_date
   v_psp_date   TIMESTAMP;                                         -- current system date and time adjusted by PSPDate offset
   v_utc_date   TIMESTAMP;                                         -- current system UTC date and time

   v_return_cd number; -- return code variable for logging
   v_error_desc varchar2(100);-- error desc variable for logging

   PROCEDURE update_nacha_file_trace_number (p_nacha_file_type IN VARCHAR2)
   IS
          v_nacha_file_id           VARCHAR2 (100);
   BEGIN
          PRC_SET_PSP_EVENT_LOG (
                    v_RETURN_CD,
                    v_ERROR_DESC,
                    'N/A',                          -- p_CompanyId          IN   VARCHAR2,
                    null,                         -- p_TypeCd             IN   VARCHAR2,
                    'PROD',                         -- p_DomainName         IN   VARCHAR2,
                    'PSP',                          --p_ArchName           IN   VARCHAR2,
                    'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
                    'N/A',                          -- p_HostName           IN   VARCHAR2,
                    'Offload Stored Proc',          -- Application_name           IN   VARCHAR2,
                    'PSP_ENTRY_DETAIL_RECORD',   --  p_ObjectName         IN   VARCHAR2,
                    'N/A',                          --p_UserName           IN   VARCHAR2,
                    to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS'),
                    'Updating ENTRY_DETAIL_RECORD ' || p_nacha_file_type );

         SELECT nachafile_seq
         INTO v_nacha_file_id
         FROM PSP_NACHAFILE
         WHERE offload_batch_fk = p_offload_batch_id AND
                     file_type = p_nacha_file_type;           

         -- Update trace number for entry detail records associated with nacha file
         -- nvl on record_data with some value so that the sort order is preserved
         -- no need to do above, checked with Dawn..
         UPDATE
             (SELECT  rec0.entry_detail_record_seq, rec0.trace_number, rec0.n_a_c_h_a_file_fk,
                          rec0.record_data_enc, rec0.VERSION, rec0.modifier_id, rec0.modified_date
              FROM psp_entry_detail_record rec0
              WHERE rec0.n_a_c_h_a_file_fk = v_nacha_file_id
                    AND rec0.initiation_date = p_offload_date
              ORDER BY rec0.legal_name,
                          rec0.company_fk,
                          rec0.n_a_c_h_a_batch_type,
                          rec0.settlement_date,
                          rec0.record_data_enc,
                          rec0.amount,
                          rec0.entry_detail_record_seq) src
              SET trace_number = DECODE (NVL (record_data_enc, '0'), '0', NULL, seq_trace_number.NEXTVAL),
                  VERSION = VERSION + 1,
                  modifier_id = p_user_id,
                  modified_date = v_utc_date;
                  
         IF SQL%ROWCOUNT != 0 THEN
               UPDATE PSP_NACHAFILE
               SET STATUS = 'Finalized',
                      VERSION = VERSION + 1,
                      MODIFIER_ID = p_user_id,
                      MODIFIED_DATE = v_utc_date,
                      FINALIZATION_DATE = v_psp_date,
                      STATUS_EFFECTIVE_DATE = v_psp_date
               WHERE nachafile_seq = v_nacha_file_id;
         END IF;                  
   END update_nacha_file_trace_number;

BEGIN
   SELECT SYS_EXTRACT_UTC (to_timestamp_tz(concat(fn_get_psp_timestamp(0), ' US/Pacific')))
     INTO v_psp_date
     FROM DUAL;
   v_utc_date := p_app_server_date;

   UPDATE PSP_OFFLOAD_BATCH
   SET STATUS_CD = 'Completed',
          STATUS_EFFECIVE_DATE = v_psp_date,
          VERSION = VERSION + 1,
          MODIFIER_ID = p_user_id,
          MODIFIED_DATE = v_utc_date
   WHERE OFFLOAD_BATCH_SEQ = p_offload_batch_id;
   
   IF p_file_type = 'DD'
     THEN
        update_nacha_file_trace_number ('CCD');
        update_nacha_file_trace_number ('PPD');
     END IF;-- 'DD' File Type

     IF p_file_type = 'Tax'
     THEN
       update_nacha_file_trace_number ('CCDPlus');
     END IF; -- 'Tax' File Type
END prc_offload;
/