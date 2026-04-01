package com.intuit.sbd.payroll.psp.jss.util;

public class BatchJobConstants {

    public static final String SOX_DB_QUERY_ORACLE = "SELECT username ,null role, account_status, profile, created FROM dba_users";
    public static final String SOX_DB_QUERY_POSTGRES = "Select usename, null AS role, CASE when valuntil Is Not Null AND valuntil < now() THEN 'EXPIRED' ELSE 'OPEN' END AS ACCOUNT_STATUS, 'DEFAULT' AS PROFILE, NOW() AS CREATED From pg_user Order by 1";
    public static final String ACCESS_TYPE_DATABASE = "DATABASE";
    public static final String SOX_APP_QUERY =   "SELECT corp_id, rol.name, null account_status, null profile, usr.created_date" +
            " FROM PSP_AUTH_USER usr" +
            " JOIN"+
            " PSP_AUTH_USER_AUTH_ROLE__ASSOC ara ON USR.AUTH_USER_SEQ=ara.AUTH_USER_FK"+
            " JOIN"+
            " PSP_AUTH_ROLE rol ON ara.AUTH_ROLE_FK=rol.AUTH_ROLE_SEQ order by corp_id";
    public static final String ACCESS_TYPE_APPLICATION = "APPLICATION";
    public static final String ORACLE_DB_NAME_QUERY = "SELECT db_unique_name FROM v$database";
    public static final String POSTGRES_DB_NAME_QUERY = "SELECT current_database();";
    public static final String PSP_SOX_HEADER_ENDPOINT = "psp_sox_header_endpoint";
    public static final String PSP_SOX_BATCH_ENDPOINT = "psp_sox_batch_endpoint";
    public static final int SOX_RETRY_COUNT = 3;

    public static final int BATCH_SIZE = 50;
    public static final int MAX_BATCH_SIZE_LIMIT = 2000;

}
