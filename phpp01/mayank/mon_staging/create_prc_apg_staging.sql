CREATE OR REPLACE PROCEDURE pspadm.prc_set_psp_event_log(OUT p_return_cd text, OUT p_error_desc character varying, IN p_companyid character varying, IN p_typecd character varying, IN p_domainname character varying, IN p_archname character varying, IN p_compname character varying, IN p_hostname character varying, IN p_appname character varying, IN p_objectname character varying, IN p_username character varying, IN p_messagedttm character varying, IN p_message character varying)
    language plpgsql
as
$$
DECLARE
    v_temp_date          TIMESTAMP(6) WITHOUT TIME ZONE;
    v_error_sqlState          text;
    v_sql text;
    v_con_count int;
BEGIN
    -- initialize
    p_RETURN_CD  := 0;
    p_ERROR_DESC := '';


    --db_link connect optimize
    SELECT count(1) INTO v_con_count FROM dblink_get_connections()
    WHERE dblink_get_connections@>'{myconn}';
	
	--check if myconn dblink connect exsist
    IF v_con_count = 0 THEN
         PERFORM dblink_connect_u('myconn', 'loopback_dblink_port');
    END IF;

    v_temp_date  := TO_DATE (p_MessageDTTM, 'YYYY-MM-DD"T"HH24:MI:SS');

    v_sql := format('INSERT INTO PSP_EVENT_LOG (
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
                %L,
               %L ,
               %L ,
               %L,
               %L ,
               %L,
               %L,
               %L,
               %L,
               %L,
               %L,
               %L,
               %L,
               %L,
               %L,
               %L,
               %L)', gen_random_uuid(),
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

    PERFORM dblink_exec('myconn', v_sql);

EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS
            v_error_sqlState = RETURNED_SQLSTATE;
        p_RETURN_CD  := v_error_sqlState;
        p_ERROR_DESC := SUBSTR ( ('Error creating PSP Event Log. ' || SQLERRM), 1, 250);
END;
$$;

