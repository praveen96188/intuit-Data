--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
Prompt Sequence SEQ_TXN_TOKEN_NBR;
Prompt drop Sequence SEQ_TXN_TOKEN_NBR;
DROP SEQUENCE SEQ_TXN_TOKEN_NBR;
Prompt Sequence SEQ_TXN_TOKEN_NBR;
--
-- SEQ_TXN_TOKEN_NBR  (Sequence) 
--
CREATE SEQUENCE SEQ_TXN_TOKEN_NBR
  START WITH 400000
  MAXVALUE 999999999999999999999999999
  MINVALUE 400000
  NOCYCLE
  CACHE 20
  NOORDER;

Prompt Sequence SEQ_TRACE_NBR;
Prompt drop Sequence SEQ_TRACE_NBR;
DROP SEQUENCE SEQ_TRACE_NBR;
Prompt Sequence SEQ_TRACE_NBR;
--
-- SEQ_TRACE_NBR  (Sequence) 
--
CREATE SEQUENCE SEQ_TRACE_NBR
  START WITH 1000000000
  INCREMENT BY 1000000000
  MAXVALUE 9999999999999999
  MINVALUE 1000000000
  CYCLE
  CACHE 20
  NOORDER;

PROMPT finishedDBUpgradeFrom_1.9.9.34_To_1.9.9.35.sql