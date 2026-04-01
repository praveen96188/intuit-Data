IF (((:NEW.$it.columnName$ <> :OLD.$it.columnName$) AND :NEW.$it.columnName$ IS NOT NULL AND :OLD.$it.columnName$ IS NOT NULL) OR
    (:NEW.$it.columnName$ IS NULL AND :OLD.$it.columnName$ IS NOT NULL) OR
    (:NEW.$it.columnName$ IS NOT NULL AND :OLD.$it.columnName$ IS NULL) ) THEN
    BEGIN
       SELECT $it.companyIdColumnName$ INTO v_company_fk from $it.companyIdFromClause$;
    EXCEPTION
       WHEN NO_DATA_FOUND THEN v_company_fk := NULL;
       WHEN OTHERS THEN RAISE;
    END;

    IF v_company_fk IS NOT NULL THEN
       SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP + NUMTODSINTERVAL((SELECT SYSTEM_PARAMETER_VALUE/1000 FROM PSP_SYSTEM_PARAMETER WHERE SYSTEM_PARAMETER_CD = 'PSP_DATE_OFFSET'), 'SECOND')) INTO v_psp_utc_systimestamp FROM DUAL;
       INSERT INTO PSP_PROPERTY_AUDIT
         (PROPERTY_AUDIT_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, CLASS_NAME, PROPERTY_NAME, OLD_PROPERTY_VALUE, NEW_PROPERTY_VALUE, OBJECT_IDENTIFIER, USER_ID, AUDIT_DATE, COMPANY_FK)
       VALUES
         (SYS_GUID(), 0, v_psp_utc_systimestamp, v_psp_utc_systimestamp, '$it.className$', '$it.propertyName$', TO_CHAR(:OLD.$it.columnName$ $it.columnToCharFormat$), TO_CHAR(:NEW.$it.columnName$ $it.columnToCharFormat$), :NEW.$it.pkColumnName$, v_modifier_id, v_psp_utc_systimestamp, v_company_fk);
    END IF;
END IF;
