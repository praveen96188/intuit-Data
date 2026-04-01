CREATE OR REPLACE PROCEDURE PRC_SET_PSP_EVENT_LOG (
        p_RETURN_CD          OUT  NUMBER,
        p_ERROR_DESC         OUT  VARCHAR2,
        p_CompanyId          IN   VARCHAR2,
        p_TypeCd             IN   VARCHAR2,
        p_DomainName         IN   VARCHAR2,
        p_ArchName           IN   VARCHAR2,
        p_CompName           IN   VARCHAR2,
        p_HostName           IN   VARCHAR2,
        p_AppName            IN   VARCHAR2,
        p_ObjectName         IN   VARCHAR2,
        p_UserName           IN   VARCHAR2,
        p_MessageDTTM        IN   VARCHAR2,  -- formatted AS 'DD/MM/YYYY HH24:MI:SS'
        p_Message            IN   VARCHAR2)
    IS
        PRAGMA               AUTONOMOUS_TRANSACTION;
        v_temp_date          PSP_EVENT_LOG.MESSAGE_DTTM%TYPE;

/******************************************************************************
   NAME:       PR_SET_PSP_EVENT_LOG
   PURPOSE:    Creates log entry into PSP_EVENT_LOG Table, used for offload log
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        09/18/2009  Tushar Thakker   1. Created


   INPUT:
   OUTPUT:
   RETURNED VALUE:
   CALLED BY:
   ASSUMPTIONS:
   LIMITATIONS:
   ALGORITHM : Autonomous transaction transaction for logging
   NOTES:
******************************************************************************/


    BEGIN
        -- initialize
        p_RETURN_CD  := 0;
        p_ERROR_DESC := '';

        -- this was the first version Rob came up with
        -- v_temp_date  := TO_DATE (p_MessageDTTM, 'DD/MM/YYYY HH24:MI:SS');

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
            FN_FORMAT_SYSGUID(SYS_GUID()),
            0 ,
            'PRC_OFFLOAD' ,
            SYS_EXTRACT_UTC(SYSTIMESTAMP),
            null ,
            SYS_EXTRACT_UTC(SYSTIMESTAMP),
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

        COMMIT;

    EXCEPTION
        WHEN OTHERS THEN
             p_RETURN_CD  := SQLCODE;
             p_ERROR_DESC := SUBSTR ( ('Error creating PSP Event Log. ' || SQLERRM), 1, 250);
    END;
/
