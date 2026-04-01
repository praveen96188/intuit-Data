CREATE OR REPLACE FUNCTION get_company_fk(p_address_seq VARCHAR)
RETURNS VARCHAR AS $$
DECLARE
    v_company_fk VARCHAR(255);
BEGIN
    SELECT ee.company_fk
    INTO v_company_fk
    FROM PSP_EMPLOYEE ee
        JOIN PSP_INDIVIDUAL individual on ee.EMPLOYEE_SEQ = individual.INDIVIDUAL_SEQ
    WHERE individual.MAILING_ADDRESS_FK = p_address_seq LIMIT 1;

    IF NOT FOUND THEN
        SELECT t1.COMPANY_SEQ
        INTO STRICT v_company_fk
        FROM PSP_COMPANY t1
        WHERE (t1.MAILING_ADDRESS_FK = p_address_seq or t1.LEGAL_ADDRESS_FK = p_address_seq) LIMIT 1;
    END IF;

    RETURN v_company_fk;
EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN NULL;
    WHEN OTHERS THEN RAISE;
END;
$$ LANGUAGE PLPGSQL;
