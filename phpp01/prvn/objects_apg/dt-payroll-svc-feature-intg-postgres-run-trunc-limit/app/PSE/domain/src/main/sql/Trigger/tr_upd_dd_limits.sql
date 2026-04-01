CREATE OR REPLACE TRIGGER TR_UPD_DD_LIMITS
BEFORE INSERT OR UPDATE ON PSP_PROPERTY_AUDIT
FOR EACH ROW
DECLARE
  v_default_company_limit VARCHAR2(255);
  v_default_employee_limit VARCHAR2(255);
BEGIN

IF :NEW.CLASS_NAME = 'DDCompanyServiceInfo' AND :NEW.PROPERTY_NAME='OverrideCompanyLimitAmount' THEN

  select value into v_default_company_limit
  from psp_limit_value lv
      join psp_limit_rule on lv.limit_rule_fk = limit_rule_seq
      join psp_offering o on limit_rule_seq = o.limit_rule_fk
      join psp_company_offering co on o.offering_seq = co.offering_fk
  where co.company_fk = :NEW.COMPANY_FK
      and o.service_code = 'DirectDeposit'
      and lv.name = 'DefaultCompanyLimit'
      and co.created_date = (select max(ico.created_date)  from psp_company_offering ico
                                join psp_offering io on io.offering_seq = ico.offering_fk
                                        where ico.company_fk = :NEW.COMPANY_FK
                                              and io.service_code='DirectDeposit');

  IF :NEW.OLD_PROPERTY_VALUE IS NULL THEN
    :NEW.OLD_PROPERTY_VALUE := v_default_company_limit;
  END IF;

  IF :NEW.NEW_PROPERTY_VALUE IS NULL THEN
    :NEW.NEW_PROPERTY_VALUE := v_default_company_limit;
  END IF;
END IF;

IF :NEW.CLASS_NAME = 'DDCompanyServiceInfo' AND :NEW.PROPERTY_NAME='OverrideEmployeeLimitAmount' THEN

  select value into v_default_employee_limit
  from psp_limit_value lv
      join psp_limit_rule on lv.limit_rule_fk = limit_rule_seq
      join psp_offering o on limit_rule_seq = o.limit_rule_fk
      join psp_company_offering co on o.offering_seq = co.offering_fk
  where co.company_fk = :NEW.COMPANY_FK
      and o.service_code = 'DirectDeposit'
      and lv.name = 'DefaultEmployeeLimit'
      and co.created_date = (select max(ico.created_date)  from psp_company_offering ico
                                  join psp_offering io on io.offering_seq = ico.offering_fk
                                        where ico.company_fk = :NEW.COMPANY_FK
                                              and io.service_code='DirectDeposit');


  IF :NEW.OLD_PROPERTY_VALUE IS NULL THEN
    :NEW.OLD_PROPERTY_VALUE := v_default_employee_limit;
  END IF;

  IF :NEW.NEW_PROPERTY_VALUE IS NULL THEN
    :NEW.NEW_PROPERTY_VALUE := v_default_employee_limit;
  END IF;
END IF;
END;
/