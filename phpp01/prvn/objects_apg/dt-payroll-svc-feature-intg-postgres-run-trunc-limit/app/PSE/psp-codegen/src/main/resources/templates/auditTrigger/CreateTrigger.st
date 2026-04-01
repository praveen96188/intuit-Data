CREATE OR REPLACE TRIGGER $it.triggerName$
AFTER INSERT OR UPDATE ON PSP_$it.tableName$
FOR EACH ROW
DECLARE
  v_company_fk VARCHAR2(255);
  v_creator_id VARCHAR2(30);
  v_modifier_id VARCHAR2(30);
  v_psp_utc_systimestamp TIMESTAMP;
BEGIN

$it.CreatorId$
$it.ModifierId$

  IF (UPDATING) THEN
     $it.PropertyTemplateParameters:CheckUpdatedProperty(); separator="\n\n"$
        
  END IF;
END;
/
ALTER TRIGGER $it.triggerName$ COMPILE
/