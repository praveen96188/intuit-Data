-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\rel-1.3\PSE\Domain\src\main\model\DBUpgrade_001.009.009.052.sql
--
-- TEAMTRACK NUM: PSRV001272
-- CREATED  DATE: 3.25.2009
-- MODIFIED DATE: 3.25.2009
-- AUTHOR       : Ken Paul
--
-- PURPOSE: 
--   This script populates the new OFFLOAD_BATCH_FK field in the PSP_NACHAFILE table. 
--

BEGIN
  UPDATE psp_nachafile nf
     SET nf.offload_batch_fk =
            (SELECT ob.offload_batch_seq
               FROM psp_offload_batch ob,
                    psp_entry_detail_record edr,
                    psp_money_movement_transaction mmt
              WHERE nf.nachafile_seq = edr.n_a_c_h_a_file_fk
                AND edr.money_movement_transaction_fk = mmt.money_movement_transaction_seq
                AND mmt.offload_batch_fk = ob.offload_batch_seq
                AND nf.realm_id = ob.realm_id
                AND ROWNUM < 2)
   WHERE nf.offload_batch_fk IS NULL AND ROWNUM < 20001;

   COMMIT;
EXCEPTION
   WHEN OTHERS
   THEN
      DBMS_OUTPUT.put_line (SQLERRM);
END;
/

--
-- PSP_NACHAFILE_FK1  (Index)
--
Prompt Index PSP_NACHAFILE_FK1;

-- commented until we can confirm won't cause problems in QA or PROD
-- (we need to figure out why we are seeing orphaned PSP_NACHAFILE records in QA)
-- (that is, why do we have nacha file records that don't have any EDR associations?)
--ALTER TABLE PSP_NACHAFILE
--MODIFY(OFFLOAD_BATCH_FK NOT NULL);

CREATE INDEX PSP_NACHAFILE_FK1 ON PSP_NACHAFILE
(OFFLOAD_BATCH_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_NACHAFILE
 ADD CONSTRAINT PSP_NACHAFILE_FK1
 FOREIGN KEY (OFFLOAD_BATCH_FK, REALM_ID)
 REFERENCES PSP_OFFLOAD_BATCH (OFFLOAD_BATCH_SEQ,REALM_ID);

COMMIT;
