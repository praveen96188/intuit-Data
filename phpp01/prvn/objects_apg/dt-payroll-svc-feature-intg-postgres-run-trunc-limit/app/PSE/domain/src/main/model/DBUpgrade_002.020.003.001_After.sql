--
-- This script will be executed AFTER the automatically generated
-- C:\Users\rtbteam\Payroll-Services\PSE\domain\src\main\model\DBUpgrade_002.020.003.001.sql
--
-- Developers can hand code logic here for data migration purposes
--
Prompt Index PSP_ENTITY_UPDATE_U1;
CREATE INDEX PSP_ENTITY_UPDATE_U1 ON PSP_ENTITY_UPDATE
(CREATED_DATE)
NOPARALLEL;

Prompt Index PSP_ENTITLEMENT_IDX3;
CREATE INDEX PSP_ENTITLEMENT_IDX3 ON PSP_ENTITLEMENT
(BILLING_REALM_ID)
NOPARALLEL;

/
DECLARE
   sequence_exists PLS_INTEGER;
BEGIN
   SELECT COUNT(*) INTO sequence_exists
   FROM "USER_SEQUENCES"
   WHERE SEQUENCE_NAME = 'SEQ_TRANSACTION_NUMBER';
   IF sequence_exists = 0 THEN
    EXECUTE IMMEDIATE 'create sequence  SEQ_TRANSACTION_NUMBER INCREMENT BY 1 MINVALUE 1 MAXVALUE 9999999999999999 CYCLE';
   END IF;
end;
/
