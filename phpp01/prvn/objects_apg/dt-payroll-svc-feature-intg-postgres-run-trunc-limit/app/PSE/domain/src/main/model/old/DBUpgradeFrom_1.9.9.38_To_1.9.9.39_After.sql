--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.38_To_1.9.9.39.sql
--
-- Developers can hand code logic here for data migration purposes
--

-- This guarantees that the trigger exists so we can delete it ...
CREATE OR REPLACE TRIGGER PSP_FINANCIAL_TRANSACTION_AT
AFTER INSERT OR UPDATE ON PSP_FINANCIAL_TRANSACTION
FOR EACH ROW
DECLARE
  v_company_fk VARCHAR2(255);
  v_creator_id VARCHAR2(30);
  v_modifier_id VARCHAR2(30);
BEGIN

SELECT :NEW.CREATOR_ID INTO v_creator_id FROM DUAL;
SELECT :NEW.MODIFIER_ID INTO v_modifier_id FROM DUAL;

END;

/

drop trigger PSP_FINANCIAL_TRANSACTION_AT

/

commit;