CREATE OR REPLACE FUNCTION FN_$it.triggerName$()
RETURNS TRIGGER
AS \$\$
DECLARE
  v_company_fk VARCHAR(255);
  v_creator_id VARCHAR(30);
  v_modifier_id VARCHAR(30);
  v_psp_utc_systimestamp TIMESTAMP;
BEGIN
$it.CreatorId$
$it.ModifierId$

  IF (TG_OP='UPDATE') THEN
     $it.PropertyTemplateParameters:CheckUpdatedProperty(); separator="\n\n"$

  END IF;
  RETURN NEW;
END;
\$\$
LANGUAGE PLPGSQL;

CREATE TRIGGER $it.triggerName$
AFTER INSERT OR UPDATE ON PSP_$it.tableName$
FOR EACH ROW
EXECUTE FUNCTION FN_$it.triggerName$();


ALTER TABLE PSP_$it.tableName$ DISABLE TRIGGER $it.triggerName$;
ALTER TABLE PSP_$it.tableName$ ENABLE TRIGGER $it.triggerName$;

;

