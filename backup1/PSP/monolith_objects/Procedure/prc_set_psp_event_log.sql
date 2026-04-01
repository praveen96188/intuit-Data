CREATE OR REPLACE PROCEDURE PRC_SET_PSP_EVENT_LOG
  (
        OUT p_return_cd TEXT,
        OUT p_error_desc VARCHAR,
        IN p_companyid VARCHAR,
        IN p_typecd VARCHAR,
        IN p_domainname VARCHAR,
        IN p_archname VARCHAR,
        IN p_compname VARCHAR,
        IN p_hostname VARCHAR,
        IN p_appname VARCHAR,
        IN p_objectname VARCHAR,
        IN p_username VARCHAR,
        IN p_messagedttm VARCHAR,
        IN p_message VARCHAR
    )
    LANGUAGE plpgsql AS

/******************************************************************************
   NAME:       PR_SET_PSP_EVENT_LOG
   PURPOSE:    Creates log entry into PSP_EVENT_LOG Table, used for offload log
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        05/02/2023  Manoj Venkatesh/ 1. Updated for DBLink
                          Anuradha Kumari
   INPUT:
   OUTPUT:
   RETURNED VALUE:
   CALLED BY:
   ASSUMPTIONS:
   LIMITATIONS:
   ALGORITHM : Autonomous transaction transaction for logging
   NOTES:
******************************************************************************/
$$
DECLARE
v_temp_date          TIMESTAMP(6) WITHOUT TIME ZONE;
    v_error_sqlState     TEXT;
BEGIN
    -- initialize
    p_RETURN_CD  := 0;
    p_ERROR_DESC := '';

    v_temp_date  := TO_DATE (p_MessageDTTM, 'YYYY-MM-DD"T"HH24:MI:SS');

INSERT INTO PSP_EVENT_LOG (
    EVENT_LOG_SEQ,
    VERSION ,
    CREATOR_ID ,
    CREATED_DATE ,
    MODIFIER_ID ,
    MODIFIED_DATE   ,
    REALM_ID ,
    COMPANY_ID,
    EVENT_LOG_TYPE_CD,
    DOMAIN_NAME,
    ARCHITECTURE_NAME,
    COMPONENT_NAME,
    HOST_NAME,
    APPLICATION_NAME,
    OBJECT_NAME,
    MESSAGE_DTTM,
    MESSAGE)
VALUES (
           gen_random_uuid(),
           0 ,
           'PRC_OFFLOAD' ,
           timezone('UTC', CURRENT_TIMESTAMP),
           null ,
           timezone('UTC', CURRENT_TIMESTAMP),
           -1 ,
           p_CompanyId,
           p_TypeCd,
           p_DomainName,
           p_ArchName,
           p_CompName,
           p_HostName,
           p_AppName,
           p_ObjectName,
           v_temp_date,
           p_Message);

EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS
            v_error_sqlState = RETURNED_SQLSTATE;
        p_RETURN_CD  := v_error_sqlState;
        p_ERROR_DESC := SUBSTR ( ('Error creating PSP Event Log. ' || SQLERRM), 1, 250);
        RAISE NOTICE 'RETURN=% , error=%', p_RETURN_CD, p_ERROR_DESC;
END;
$$;
