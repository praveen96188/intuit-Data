    BEGIN
       SELECT $it.companyIdColumnName$ INTO v_company_fk from $it.companyIdFromClause$;
    EXCEPTION
       WHEN NO_DATA_FOUND THEN v_company_fk := NULL;
       WHEN OTHERS THEN RAISE;
    END;

    IF v_company_fk IS NOT NULL THEN
       INSERT INTO PSP_PROPERTY_AUDIT
         (PROPERTY_AUDIT_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, CLASS_NAME, PROPERTY_NAME, OLD_PROPERTY_VALUE, NEW_PROPERTY_VALUE, OBJECT_IDENTIFIER, USER_ID, AUDIT_DATE, COMPANY_FK)
       VALUES
         (gen_random_uuid(), 0, SELECT CURRENT_TIMESTAMP AT TIME ZONE 'UTC', SELECT CURRENT_TIMESTAMP AT TIME ZONE 'UTC', '$it.className$', '$it.propertyName$', null, TO_CHAR(NEW.$it.columnName$ $it.columnToCharFormat$), NEW.$it.pkColumnName$, v_creator_id, SELECT CURRENT_TIMESTAMP AT TIME ZONE 'UTC', v_company_fk);
    END IF;
