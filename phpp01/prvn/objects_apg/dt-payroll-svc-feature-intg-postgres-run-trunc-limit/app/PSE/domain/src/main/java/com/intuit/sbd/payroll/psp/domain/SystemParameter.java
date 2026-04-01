package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.BaseProcessCache;
import com.intuit.sbd.payroll.psp.cache.DirtyCheckProcessCache;
import com.intuit.sbd.payroll.psp.cache.ExpiringProcessCache;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.cache.ProcessCacheType;
import com.intuit.sbd.payroll.psp.cache.SessionCacheWrapper;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationProxy;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Hand-written business logic
 */
public class SystemParameter extends BaseSystemParameter {
    private static SpcfLogger logger = SpcfLogManager.getLogger(SystemParameter.class);

    private static BaseProcessCache<NaturalKey, SystemParameter> processCache;

    private static void initializeProcessCache() {
        if (processCache == null) {
            synchronized (SystemParameter.class) {
                if (processCache == null) {
                    try {
                        processCache = new SessionCacheWrapper<NaturalKey, SystemParameter>(SystemParameter.class);
                        ProcessCacheType cacheType = BaseProcessCache.getProcessCacheType();
                        switch (cacheType) {
                            case DirtyChecking:
                                processCache = new DirtyCheckProcessCache<NaturalKey, SystemParameter>(
                                        SystemParameter.class,
                                        createWhereClause(),
                                        SystemParameterCd());
                                break;
                            case Expiring:
                                processCache = new ExpiringProcessCache<NaturalKey, SystemParameter>(
                                        SystemParameter.class,
                                        Code.SYSTEM_PARAMETER_CACHE_REFRESH_INTERVAL,
                                        new Query<SystemParameter>().Where(createWhereClause()),
                                        SystemParameterCd());
                                break;
                            default:
                                processCache = new SessionCacheWrapper<NaturalKey, SystemParameter>(SystemParameter.class);
                        }
                    } catch (Throwable t) {
                        logger.error("error initializing system parameter caching", t);
                    }

                    logger.info("system parameter cache: " + processCache);
                }
            }
        }
    }

    private static Criterion<SystemParameter> createWhereClause() {
        Criterion<SystemParameter> whereClause =
                SystemParameter.SystemParameterCd().notEqualTo(Code.AS400_TOKEN.name())
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.PSP_TO_AS400_DATA_SYNC_TOKEN.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.AS400_DATA_SYNC_TOKEN.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.ATF_EXTRACT_CUTOFF.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.FILING_SPECIFIC_TRANSACTIONS_TOKEN.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.PERFORM_ATF_DATA_EXTRACT.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.ATF_MONTH_OF_QTR_CUTOFF.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.ATF_DAY_OF_MONTH_CUTOFF.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.ATF_DAY_OF_WEEK_CUTOFF.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.FILING_SPECIFIC_NUM_THREADS.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.ATF_FILING_SPECIFIC_COMMIT_SIZE.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.ATF_PAYMENTS_IN_MAX_SIZE.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.IOP_SYNC_TOKEN.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.IOP_SYNC_END_TIME_CALCULATION_TOKEN.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.IOP_SYNC_MAX_TIME_WINDOW_TOKEN.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.PRINTED_CHECKS_NEXT_CHECK_NUMBER.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.AMO_BATCH_TOKEN.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.EMAIL_GATEWAY_MAX_BATCH_SIZE.name()))
                               .And(SystemParameter.SystemParameterCd().notEqualTo(Code.EMPLOYEE_CALCULATION_TOKEN.name()))
                               .And(SystemParameter.SystemParameterOrg().isNotNull());

        Expression<SystemParameter> query = new Query<SystemParameter>().Where(SystemParameter.SystemParameterCd().equalTo(Code.SYSTEM_PARAMETER_CACHE_VOLATILE_KEYS.name()));
        DomainEntitySet<SystemParameter> volatileParameters = Application.find(SystemParameter.class, query);
        for (SystemParameter volatileParameter : volatileParameters) {
            if (volatileParameter.getSystemParameterValue() != null) {
                String[] parameterCodes = volatileParameter.getSystemParameterValue().split(",", 0);
                for (String parameterCd : parameterCodes) {
                    parameterCd = parameterCd.trim();
                    if (parameterCd.length() > 0) {
                        whereClause = whereClause.And(SystemParameter.SystemParameterCd().notEqualTo(parameterCd));
                        logger.info("excluding key from cache: " + parameterCd);
                    }
                }
            }
        }

        return whereClause;
    }

    public static BaseProcessCache<NaturalKey, SystemParameter> getProcessCache() {
        if (processCache == null)
            initializeProcessCache();
        return processCache;
    }

    public NaturalKey getNaturalKey() {
        return new NaturalKey(SystemParameter.class, getSystemParameterCd());
    }

    public static SystemParameter findSystemParameter(String pSystemParamCode) {
        NaturalKey naturalKey = new NaturalKey(SystemParameter.class, pSystemParamCode);
        return getProcessCache().get(naturalKey);
    }

    /**
     * Since SystemParameters are generally cached, an update requires re-attaching the SystemParameter
     * object to the current session.  This is just a convenience method -- should move to PayrollServices
     * SystemParameterManager interface.
     *
     * @param pParameterCode    the parameter to update
     * @param pValue            the value to store
     */
    public static void update(Code pParameterCode, String pValue) {
        SystemParameter sp = findSystemParameter(pParameterCode);
        if (sp == null)
            throw new IllegalArgumentException("no system parameter found: " + pParameterCode.name());
        sp.setSystemParameterValue(pValue);
        if (!Application.getHibernateSession().contains(sp)) {
            Application.getHibernateSession().update(sp);
        }
        Application.save(sp);
        DirtyCheckProcessCache.updateDBCacheTokenValue();
    }

    public String getDecryptedSystemParameterValue() {
        //
        // KP - PSRV003218 - Call the decode method of ConfigurationProxy to return plain-text form of parameter.
        // (this method will decrypt value if it is encrypted, else it will return unmodified value)
        //
        return ConfigurationProxy.decodeProperty(getSystemParameterValue());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Finds a system parameter based on the parameter code
     *
     * @param pSystemParamCode  parameter requested
     * @return the parameter value
     */
    public static SystemParameter findSystemParameter(SystemParameter.Code pSystemParamCode) {
        return findSystemParameter(pSystemParamCode.name());
    }

    /**
     * Finds a system parameter based on the parameter code
     * (delegates to the method findStringValue(String))
     *
     * @param pSystemParameterCd    parameter code requested
     * @return the parameter value as a String
     */
    public static String findValue(SystemParameter.Code pSystemParameterCd) {
        return findStringValue(pSystemParameterCd);
    }

    /**
     * Finds a system parameter based on the parameter code
     *
     * @param pSystemParameterCd    parameter requested
     * @return the parameter value as a String
     */
    public static String findStringValue(SystemParameter.Code pSystemParameterCd) {
        SystemParameter systemParameter = findSystemParameter(pSystemParameterCd);

        if (systemParameter == null) {
            throw new RuntimeException("Could not locate system parameter for value: " + pSystemParameterCd);
        }

        return systemParameter.getIsSecured() ? systemParameter.getDecryptedSystemParameterValue()
                : systemParameter.getSystemParameterValue();
    }

    /**
     * Reads the system parameter value from the database and attempts to convert it to an int.  If any failure
     * occurs (the system parameter does not exists or does not contain a value that can be converted to an int)
     * the passed in default value is returned and the error condition is logged.  No exception is thrown.
     *
     * @param pSystemParameterCd    parameter requested
     * @param pDefaultValue         value to return on failure
     * @return the parameter value from the database if available, else the default value
     */
    public static String findStringValue(SystemParameter.Code pSystemParameterCd, String pDefaultValue) {
        String value = pDefaultValue;

        try {
            value = findStringValue(pSystemParameterCd);
        } catch (Throwable t) {
            logger.info("Could not read parameter value " + pSystemParameterCd + " -- using default: " + pDefaultValue);
        }

        return value;
    }

    /**
     * Finds a system parameter based on the parameter code
     * Converts the stored value to a boolean using Boolean.parseBool(String)
     *
     * @param pSystemParameterCd    parameter requested
     * @return the parameter value as a boolean
     */
    public static boolean findBooleanValue(SystemParameter.Code pSystemParameterCd) {
        return Boolean.parseBoolean(findValue(pSystemParameterCd));
    }

    /**
     * Finds a system parameter based on the parameter code
     * Converts the stored value to a boolean using Boolean.parseBoolean(String)
     * Returns default if no system parameter with the given code exists.
     *
     * @param pSystemParameterCd    parameter requested
     * @param pDefaultValue         value to return on failure
     * @return the parameter value as a boolean
     */
    public static boolean findBooleanValue(SystemParameter.Code pSystemParameterCd, boolean pDefaultValue) {
        boolean value = pDefaultValue;

        try {
            value = findBooleanValue(pSystemParameterCd);
        } catch (Throwable t) {
            logger.info("Could not read parameter value " + pSystemParameterCd + " -- using default: " + pDefaultValue);
        }

        return value;
    }

    /**
     * Find a system parameter based on the parameter code.
     *
     * @param pSystemParameterCd    parameter requested
     * @return an int value
     * @throws RuntimeException if the parameterCd does not exist, a parameter value does not exist or cannot be
     *                          converted to an int
     */
    public static int findIntValue(SystemParameter.Code pSystemParameterCd) {
        String value = findValue(pSystemParameterCd);

        if ((value == null) || (value.trim().length() == 0)) {
            throw new RuntimeException("No value found for system parameter: " + pSystemParameterCd);
        }

        return Integer.parseInt(value);
    }

    /**
     * Reads the system parameter value from the database and attempts to convert it to an int.  If any failure
     * occurs (the system parameter does not exists or does not contain a value that can be converted to an int)
     * the passed in default value is returned and the error condition is logged.  No exception is thrown.
     *
     * @param pSystemParameterCd    parameter requested
     * @param pDefaultValue         value to return on failure
     * @return the parameter value from the database if available, else the default value
     */
    public static int findIntValue(SystemParameter.Code pSystemParameterCd, int pDefaultValue) {
        int value = pDefaultValue;

        try {
            value = findIntValue(pSystemParameterCd);
        } catch (Throwable t) {
            logger.info("Could not read parameter value " + pSystemParameterCd + " -- using default: " + pDefaultValue);
        }

        return value;
    }

    /**
     * Find a system parameter based on the parameter code.
     *
     * @param pSystemParameterCd    parameter requested
     * @return a long value
     * @throws RuntimeException if the parameterCd does not exist, a parameter value does not exist or cannot be
     *                          converted to an int
     */
    public static Long findLongValue(SystemParameter.Code pSystemParameterCd) {
        String value = findValue(pSystemParameterCd);

        if ((value == null) || (value.trim().length() == 0)) {
            throw new RuntimeException("No value found for system parameter: " + pSystemParameterCd);
        }

        return Long.parseLong(value);
    }

    /**
     * This is a global feature flag for PSP to determine if PSP is in Test State
     * @return the parameter value as a boolean
     */
    public static boolean isSystemInTestState() {
        SystemState state = SystemState.valueOf(findStringValue(SystemParameter.Code.PRIMARY_BATCH_JOB_MULTI_OFFLOAD_ENABLED));
        return (state == SystemState.DUAL_OFFLOAD_STATE || state == SystemState.INTEGRATION_STATE);
    }

    /**
     * Reads the system parameter value from the database and attempts to convert it to an long.  If any failure
     * occurs (the system parameter does not exists or does not contain a value that can be converted to a long)
     * the passed in default value is returned and the error condition is logged.  No exception is thrown.
     *
     * @param pSystemParameterCd    parameter requested
     * @param pDefaultValue         value to return on failure
     * @return the parameter value from the database if available, else the default value
     */
    public static Long findLongValue(SystemParameter.Code pSystemParameterCd, long pDefaultValue) {
        long value = pDefaultValue;

        try {
            value = findLongValue(pSystemParameterCd);
        } catch (Throwable t) {
            logger.info("could not read parameter value " + pSystemParameterCd + " -- using default: " + pDefaultValue);
        }

        return value;
    }

    public static SpcfCalendar findCalendarValue(Code pCode) {
        String value = findValue(pCode);
        return SpcfCalendar.parse("yyyy-MM-dd HH:mm", value);
    }

    public static SpcfCalendar findCalendarValueByFormat(String format,Code pCode) {
        String value = findValue(pCode);
        if(value==null) return null;
        return SpcfCalendar.parse(format, value);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public SystemParameter() {
        super();
    }

    public enum SystemState{
        ZERO_STATE,
        DUAL_OFFLOAD_STATE,
        INTEGRATION_STATE,
        TARGET_STATE;
    }

    public enum Code {
        /**
         * Offset, in seconds, to the current time
         */
        PSP_DATE_OFFSET,
        PSP_DATE_TIMEZONE_OFFSET,
        JPMC_ENABLE_ENCRYPTION,
        JPMC_SKIP_SIGNATURE_VERIFICATION,
        JPMC_IMMEDIATE_DESTINATION,
        JPMC_IMMEDIATE_DESTINATION_NAME,
        JPMC_IMMEDIATE_ORIGIN,
        JPMC_IMMEDIATE_ORIGIN_NAME,
        JPMC_ACCUM_BATCH_COMPANY_NAME,
        JPMC_CCD_BATCH_COMPANY_NAME,
        JPMC_ACCUM_BATCH_PAYROLL_ID,
        JPMC_COMPANY_ID_CCD,
        JPMC_COMPANY_ID_PPD,
        JPMC_COMPANY_ID_CCDPLUS,
        JPMC_ORIGINATING_DFI_ID,
        ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET,
        AS400_TOKEN,
        AS400_DATA_SYNC_TOKEN,
        AS400_DATA_SYNC_THREADS,
        AS400_DATA_SYNC_DELAY,
        AS400_DATA_SYNC_CREATE_NEW_COMPANY,
        AS400_DATA_SYNC_MIGRATE_COMPANY_DATA,
        AS400_DATA_IS_ENCRYPTED,
        PSP_TO_AS400_DATA_SYNC_TOKEN,
        PSP_TO_AS400_DATA_SYNC_LOOKBACK_PERIOD,
        CRIS_SYNC_TIMESTAMP,

        BACKDATED_PAYROLL_INITIATION_DATE_OFFSET,
        BACKDATED_PAYROLL_INIT_DATE_PAYMENT_TEMPLATES_TO_CHECK_DAY_RULES,

        // constants for AS400->CRIS event processing
        CRIS_TXN_PROCESSOR_THREAD_PROCESSING_CHECK_SLEEP_MILLIS,
        CRIS_TXN_PROCESSOR_CUEVENT_THREADS,
        CRIS_TXN_PROCESSOR_HEARTBEAT_INTERVAL_SECONDS,
        CRIS_LOG_EVENT_PROCESSING_ERRORS,

        // ERS Gateway
        ERS_REQUEST_TIMEOUT,
        ERS_MAX_RETRIES,
        ERS_ASSISTED_WAIT_DAYS,
        ERS_BATCH_SIZE,
        ERS_MAX_REQUEST_RETRIES,

        //BRM Gateway
        BRM_REQUEST_TIMEOUT,
        BRM_MAX_RETRIES,

        // constants for AS400 gateway
        AS400_GATEWAY_RETRY_SLEEP_MILLIS,
        AS400_GATEWAY_ALLOWED_ELAPSED_MILLIS,
        AS400_MIGRATION_BATCH_SIZE,

        // ACH Transaction Processor Parameters
        ACH_TRANSACTION_PROCESSOR_BATCH_SIZE,

        TRANSACTION_OFFLOAD_EVENT_BATCH_SIZE,

        // PSP UI LDAP connection parameters
        PSPUI_LDAP_DN,
        PSPUI_LDAP_PASSWORD,
        PSPUI_LDAP_URL,
        PSPUI_LDAP_ENABLE_SSL,
        PSP_TEST_DELETE_ALL,

        // PSP UI settings
        PSPUI_MAX_COMPANY_SEARCH_RESULTS,

        // PSP UI lockout setting
        MAX_NUMBER_OF_FAILED_LOGIN_ATTEMPTS,
        LOCK_ACCOUNT_DURATION,

        //ATF DataExtract Rel
        ATF_EXTRACT_CUTOFF,
        FILING_SPECIFIC_TRANSACTIONS_TOKEN,
        FILING_SPECIFIC_NUM_THREADS,
        PERFORM_ATF_DATA_EXTRACT,
        ATF_MONTH_OF_QTR_CUTOFF,
        ATF_DAY_OF_MONTH_CUTOFF,
        ATF_DAY_OF_WEEK_CUTOFF,
        ATF_FILING_SPECIFIC_COMMIT_SIZE,
        ATF_PAYMENTS_IN_MAX_SIZE,
        ATF_SEND_TO_SECONDARY_FTP_SERVER,
        PERFORM_ATF_DEPOSIT_FREQUENCY_EXTRACT,
        PERFORM_ATF_COMPANY_INFO_EXTRACT,
        PERFORM_ATF_EMPLOYEE_INFO_EXTRACT,
        PERFORM_ATF_WAGE_LIMITS_EXTRACT,
        PERFORM_ATF_COMPANY_TAX_EXTRACT,
        PERFORM_ATF_COMPANY_TAX_RATE_EXTRACT,
        PERFORM_ATF_EMPLOYEE_QUARTERLY_TOTALS_EXTRACT,
        PERFORM_ATF_W2_COUNT_INFO_EXTRACT,
        PERFORM_ATF_COMPANY_PAYROLL_ITEMS_EXTRACT,
        ATF_DAYS_FOR_END_DATE,

        //ATF extract files - FTP flag
        FTP_ATF_DEPOSIT_FREQUENCY_EXTRACT_FILE,
        FTP_ATF_COMPANY_INFO_EXTRACT_FILE,
        FTP_ATF_EMPLOYEE_INFO_EXTRACT_FILE,
        FTP_ATF_WAGE_LIMITS_EXTRACT_FILE,
        FTP_ATF_COMPANY_TAX_EXTRACT_FILE,
        FTP_ATF_COMPANY_TAX_RATE_EXTRACT_FILE,
        FTP_ATF_EMPLOYEE_QUARTERLY_TOTALS_EXTRACT_FILE,
        FTP_ATF_W2_COUNT_INFO_EXTRACT_FILE,

        //Events Gateway
        EVENTS_GATEWAY_INTERVAL,
        EVENTS_GATEWAY_MAX_WAIT,
        EVENTS_GATEWAY_MIN_POOL_SIZE,
        EVENTS_GATEWAY_MAX_POOL_SIZE,

        // PSP Forecast parameters
        FORECAST_ESTIMATE_PADDING,
        FORECAST_SEARCH_WINDOW,
        FORECAST_LOOK_BACK_WINDOW,

        // Tax Credits
        TAX_CREDITS_DISABLE_HUD,
        TAX_CREDITS_HUD_GEO_URL,
        TAX_CREDITS_HUD_ZONE_URL,
        TAX_CREDITS_CONTACT_EMAIL,
        TAX_CREDITS_ECHOSIGN_SYNC_WINDOW,
        TAX_CREDITS_ECHOSIGN_ENDPOINT,

        // Check Distribution parameters
        CD_COVERPAGE_PRINTER_NAME,
        CD_PAYCHECK_PRINTER_NAME,
        CD_FONT_DIRECTORY,
        CD_IS_NEXTSHIP_INTEGRATED,
        CD_NEXTSHIP_SERVERNAME,
        CD_NEXTSHIP_USERNAME,
        CD_NEXTSHIP_PASSWORD,
        CD_PRINT_PAYCHECK_THREAD_POOL_INTERVAL,
        CD_PRINT_PAYCHECK_THREAD_POOL_MAX_WAIT,
        CD_PRINT_PAYCHECK_MIN_THREAD_POOL_SIZE,
        CD_PRINT_PAYCHECK_MAX_THREAD_POOL_SIZE,

        // QBDT specific
        QBDT_STATE_FILING_STATUS_TRANSLATION,
        QBDT_MONTHLY_BILLING_START_DATE,

        // QBDTWS 401K Response Expiration
        K401_RESPONSE_EXPIRATION_MINUTES,
        K401_ERROR_MSG_AGE_OUT_DAYS,

        // QBDTWS -- general

        /**
         * supports a ":" delimited list of service names that determine whether a request is processed.
         * This switch is used in case of an 'emergency' due to a critical defect in the code for a particular
         * service or due to a desire to decrease load on the server by not servicing customers with no paid
         * cloud data service (i.e. Intuit is just saving their data for 'Data As An Asset' initiatives.
         */
        QBDTWS_PROCESS_DATA_FOR_SERVICES,
        QBDTWS_ASSISTED_2010R10_401K_AVAILABILITY_DATE,
        QBDTWS_ASSISTED_2011R4_401K_AVAILABILITY_DATE,

        /**
         * Alerts are sent when the result set returned from any query is above the RESULTSET_SIZE_THRESHOLD_FOR_ALERT threshold and
         * the statement is not on the exclusion list
         */
        RESULTSET_SIZE_ALERT_THRESHOLD,
        RESULTSET_SIZE_ALERT_EXCLUSION_LIST,

        /**
         * Alerts are sent when the total number of sql calls in a given unit of work is above the TOTAL_SQL_CALLS_ALERT_THRESHOLD threshold and
         * the statement is not on the exclusion list
         */
        TOTAL_SQL_CALLS_ALERT_THRESHOLD,
        TOTAL_SQL_CALLS_ALERT_EXCLUSION_LIST,

        /**
         * Fraud batch job threading parameters
         */
        FRAUD_CONTROLS_THREAD_POOL_INTERVAL,
        FRAUD_CONTROLS_THREAD_POOL_MAX_WAIT,
        FRAUD_CONTROLS_MIN_THREAD_POOL_SIZE,
        FRAUD_CONTROLS_MAX_THREAD_POOL_SIZE,

        /**
         * IOP batch job threading parameters
         */
        IOP_SYNC_THREAD_POOL_INTERVAL,
        IOP_SYNC_THREAD_POOL_MAX_WAIT,
        IOP_SYNC_MIN_THREAD_POOL_SIZE,
        IOP_SYNC_MAX_THREAD_POOL_SIZE,
        IOP_REQUEST_TIMEOUT,
        IOP_SYNC_TOKEN,
        IOP_SYNC_END_TIME_CALCULATION_TOKEN,
        IOP_SYNC_MAX_TIME_WINDOW_TOKEN,

        /**
         * ERS batch job threading parameters
         */
        ERS_SYNC_THREAD_POOL_INTERVAL,
        ERS_SYNC_THREAD_POOL_MAX_WAIT,
        ERS_SYNC_MIN_THREAD_POOL_SIZE,
        ERS_SYNC_MAX_THREAD_POOL_SIZE,
        ERS_SYNC_OUTPUT_DIRECTORY,
        ERS_SYNC_BATCH_SIZE,

        /**
         * App server threading parameters
         */
        APP_SERVER_THREAD_POOL_INTERVAL,
        APP_SERVER_THREAD_POOL_MAX_WAIT,
        APP_SERVER_MIN_THREAD_POOL_SIZE,
        APP_SERVER_MAX_THREAD_POOL_SIZE,
        APP_SERVER_BATCH_SIZE,

        /**
         * ACH Transaction processor batch job threading parameters
         */
        ACH_TRANSACTION_THREAD_POOL_INTERVAL,
        ACH_TRANSACTION_THREAD_POOL_MAX_WAIT,
        ACH_TRANSACTION_MIN_THREAD_POOL_SIZE,
        ACH_TRANSACTION_MAX_THREAD_POOL_SIZE,
        ACH_WAIT_PERIOD,

        /**
         * ACH Returns Processing (NightlyBatchJob processor job step) threading
         */
        ACH_RETURNS_THREAD_POOL_SIZE,

        /**
         * process data caching expiration
         */
        SYSTEM_PARAMETER_CACHE_VOLATILE_KEYS,
        SYSTEM_PARAMETER_CACHE_REFRESH_INTERVAL,
        SOURCE_PAYROLL_PARAMETER_CACHE_VOLATILE_KEYS,
        SOURCE_PAYROLL_PARAMETER_REFRESH_INTERVAL,
        PROCESS_CACHE_TYPE,
        PROCESS_CACHE_REFRESH_TOKEN,

        /**
         * SFTP proxy parameters
         */
        SFTP_PROXY_ENABLED,

        /*
        Paycards
         */
        PAYCARD_ROUTING_NUMBER,
        PAYCARD_ACCOUNT_PREFIX,
        /**
         * This parameter is a kill switch for the processing of assisted requests in psp
         */
        PROCESS_ASSISTED_REQUESTS,
        /**
         * Retry OFX batch job threading parameters
         */
        RETRY_OFX_THREAD_POOL_INTERVAL,
        RETRY_OFX_THREAD_POOL_MAX_WAIT,
        RETRY_OFX_MIN_THREAD_POOL_SIZE,
        RETRY_OFX_MAX_THREAD_POOL_SIZE,

        /**
         * EFTPS Enrollment SYSTEM PARAMETERS
         */
        EFTPS_838_MAX_SEGMENT_COUNT,
        EFTPS_838_MAX_TRANSACTION_COUNT,

        /**
         * EFTPS Payments
         */
        EFTPS_PAYMENT_CUTOFF,
        EFTPS_813_SETTLEMENT_DATE_OFFSET,
        EFTPS_813_MAX_SEGMENTS_PER_FILE,
        EFTPS_813_MAX_PAYMENTS_PER_SEGMENT,
        EFTPS_813_MAX_ACH_AMOUNT_PER_SEGMENT,
        EFTPS_813_MAX_PAYMENTS_TO_PROCESS_PER_BATCH_RUN,

        /**
         * ACH Tax Payments
         */
        ACH_TAX_PAYMENT_CUTOFF,
        ACH_DEBIT_TAX_PAYMENT_CUTOFF,

        /**
         * Check Payments
         */
        CHECK_PAYMENT_CUTOFF,

        /**
         * EDI Tax Payments
         */
        EDI_TAX_PAYMENT_CUTOFF,
        EDI_813_MAX_PAYMENTS_PER_SEGMENT_4010,

        /**
         * RAF Enrollment SYSTEM PARAETERS
         */
        RAF_ENROLLMENT_SELECT_SIZE,

        /**
         * QBDT Adapter
         */
        QBDT_PENNY_CUTOFF,
        LAST_TRANSMISSION_LOOKBACK_MONTHS,
        PRELOAD_EMPLOYEE_COUNT,
        RECORD_REQUEST_INFO,
        QBDT_VALIDATE_FILE_ID,
        QBDT_NEW_EMPLOYEE_COUNT,
        QBDT_FORWARD_REQUESTS_TO_AS400,
        MAX_NUM_PAYCHKS_PER_OFX,
        QBDT_MAX_PAYCHECKS_PER_DR,
        QBDT_MAX_EMPLOYEES_PER_DR,

        /**
         * Printed check selection batch job
         */
        PRINTED_CHECKS_NEXT_CHECK_NUMBER,
        PRINTED_CHECKS_PRINTER_NAME,
        PRINTED_CHECKS_BATCH_SIZE,
        PRINTED_CHECKS_UPLOAD_POSITIVE_PAY_FILES,
        PRINTED_CHECKS_POSITIVE_PAY_FILE_DIRECTORY,
        PRINTED_CHECKS_SFTP_PATH,
        PRINTED_CHECKS_SFTP_USER,
        PRINTED_CHECKS_SFTP_PASSWORD,
        PRINTED_CHECKS_SFTP_HOST,
        PRINTED_CHECKS_SFTP_RETRY_COUNT,

        /**
         * coupon batch job
         */
        COUPON_PAYMENT_TEMPLATE_INCLUDE_ACH_CREDIT,

        /**
         * AMO batch job threading parameters
         */
        AMO_BATCH_SIZE,
        AMO_INCREMENTAL_BATCH_SIZE,
        AMO_CONNECTION_RETRY_ATTEMPTS,
        AMO_CONNECTION_RETRY_WAIT_PERIOD,
        AMO_MESSAGE_RECEIVE_TIMEOUT,
        AMO_THREAD_POOL_INTERVAL,
        AMO_THREAD_POOL_MAX_WAIT,
        AMO_MIN_THREAD_POOL_SIZE,
        AMO_MAX_THREAD_POOL_SIZE,
        AMO_BATCH_TOKEN,
        AMO_MAX_MESSAGE_FAILURE_COUNT,
        AMO_MESSAGE_EXPIRATION_WAIT_PERIOD,
        AMO_EDITION_ELEMENT_NAME,
        AMO_EDITION_VALUE_BASIC,
        AMO_EDITION_VALUE_ENHANCED,
        AMO_EDITION_VALUE_ENHANCED_ACCOUNTANT,
        AMO_EDITION_VALUE_ENHANCED_ACCOUNTANT_PRO_ADVISOR,
        AMO_EDITION_VALUE_STANDARD,
        AMO_NUMBER_OF_EMPLOYEES_ELEMENT_NAME,
        AMO_NUMBER_OF_EMPLOYEES_VALUE_ONE,
        AMO_NUMBER_OF_EMPLOYEES_VALUE_UPTO3,
        AMO_NUMBER_OF_EMPLOYEES_VALUE_UNLIMITED,
        AMO_MESSAGE_TIMESTAMP_GRACE_PERIOD,

        /**
         * AMO web service parameters
         */
        AMO_WS_REQUEST_TIMEOUT,
        AMO_WS_EWS_SYNC_ENABLED,

        /**
         * TES batch job threading parameters
         */
        TES_BATCH_SIZE,
        TES_INCREMENTAL_BATCH_SIZE,
        TES_LOOKUP_BATCH_SIZE,
        TES_LOOKUP_PROCESSING_METHOD,
        TES_CONNECTION_RETRY_ATTEMPTS,
        TES_CONNECTION_RETRY_WAIT_PERIOD,
        TES_MESSAGE_RECEIVE_TIMEOUT,
        TES_THREAD_POOL_INTERVAL,
        TES_THREAD_POOL_MAX_WAIT,
        TES_MIN_THREAD_POOL_SIZE,
        TES_MAX_THREAD_POOL_SIZE,

        PSP_TO_AS400_MAX_RETRY,
        PSP_TO_AS400_MAX_BATCH_SIZE,

        /**
         * Zero Payment Requirements
         */
        ZERO_PAYMENT_COUPON_REPORT_REQUIRED,
        ZERO_PAYMENT_RECON_FILE_REQUIRED,

        /**
         * Bank server configuration
         */
        BANK_SFTP_ACH_HOST,
        BANK_SFTP_ARP_HOST,
        HIRE_ACT_ENABLED,

        SAP_SESSION_TIMEOUT,
        SAP_CALL_TRACKING,
        SIEBEL_ITEMS_NOT_IN_PSP,

        CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY,

        //Book transfer Intuit company key
        BOOK_TRANSFER_INTUIT_COMPANY_ID,

        /**
         * Mobile Adapter Configuration
         */
        MOBILE_LIVE_PERSON_HOST,
        MOBILE_LIVE_PERSON_HOST_VERSION,
        MOBILE_LIVE_PERSON_APPLICATION_KEY,

        TRACK_MMT_DELETES,

        /**
         * EWS Adapter Configuration
         */
        EWS_ASSISTED_AUTO_OFFER_CODE,

        //To enable/Diable Gems files uploading using Scp/sftp
        ENABLE_SCP_GEMS_FILE_UPLOAD,

        // billing
        PSP_TO_EMSBS_SYNC_TOKEN,
        EMSBS_MAX_NUMBER_LOOKBACK_DAYS,
        MAX_PAYRUNS_PER_BILLING_BATCH,
        NUM_EE_PER_OFX_ALERT_LEVEL,
        LISTID_SUPPORTED_QBDT_VER,

        // Employee Quarterly Calculations
        MAX_PAYRUNS_FOR_EE_CALC_BATCH,
        HOURLY_RATE_REQUIRED_LAW_IDS,
        EMPLOYEE_CALCULATION_TOKEN,
        // Number of thread to create for employee law quarterly totals
        EE_TOTALS_CALC_NUM_THREADS,
        // Control the payroll item calculation portion in EE Totals batch job
        EE_TOTALS_CALC_PAYROLL_ITEMS,
        // batch job calculation mode
        EE_TOTALS_CALC_BATCH_MODE,

        // Email Gateway
        EMAIL_GATEWAY_NTF_BATCH_SIZE,
        EMAIL_GATEWAY_MAX_BATCH_SIZE,
        EMAIL_GATEWAY_SEND_MAX_RETRY,

        // W2s and Filings
        W2_COMPANY_LIST,
        TFS_MONTHLY_TRANSFER_COMPANY_LIST,

        PSP_BACKDATE_PROCESSING_BEGIN,

        //AIA_GATEWAY PARAMETERS
        AIA_REQUEST_TIMEOUT,

        ERS_DATA_SYNC_OPTIONS,

        LEDGER_OPERATIONS_THREAD_POOL_INTERVAL,
        LEDGER_OPERATIONS_THREAD_POOL_MAX_WAIT,
        LEDGER_OPERATIONS_MIN_THREAD_POOL_SIZE,
        LEDGER_OPERATIONS_MAX_THREAD_POOL_SIZE,

        OFFLOAD_EVENTS_THREAD_POOL_INTERVAL,
        OFFLOAD_EVENTS_THREAD_POOL_MAX_WAIT,
        OFFLOAD_EVENTS_MIN_THREAD_POOL_SIZE,
        OFFLOAD_EVENTS_MAX_THREAD_POOL_SIZE,

        SALES_TAX_GATEWAY_IMPLEMENTATION_CLASS,

        ANNUAL_BILLING_THREAD_POOL_INTERVAL,
        ANNUAL_BILLING_THREAD_POOL_MAX_WAIT,
        ANNUAL_BILLING_MIN_THREAD_POOL_SIZE,
        ANNUAL_BILLING_MAX_THREAD_POOL_SIZE,

        ASSISTED_PSIDS_START_WITH_999,

        // Exempted FL Agency Id list
        EXEMPTED_AGENCY_IDS,

        QB_EMPLOYEE_ID_SWAPPING_VERSIONS,

        ALLOW_NEGATIVE_MMT,

        QB_FILE_ID_SEARCH_MONTHS_PRIOR,

        // Worker comp related parameters
        WC_SYNC_TOKEN,

        HISTORIC_PAYCHECK_PROCESS_THRESHOLD,

        MAX_PAST_DAYS_MMT,

        BATCH_JOB_CONTROLLER_ENABLED,

        BRM_SYMPHONY_FILE_NAME,

        //Alert Thresholds
        FINANCIAL_TRANSACTION_ALERT_THRESHOLD,
        //OIM Whitelisted AuthUsers
        DATA_ADAPTER_WHITELISTED_AUTH_ROLES,
        //PSP-5866 - Payment templates to override backdate hold in  case of bulk debit
        OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT ,
        PMT_TMPLT_SPLIT_ETD_NULL_BACKDATED_AND_TIMELY_TAXES,
        VMP_PAYCHECK_PERIOD_LOOK_BACK_MONTHS,
        VMP_SIZE_THROTTLING_VALUE,

        //TPSU pulls in data for all the Direct deposit active customers. To process in batches this is used.
        TPSU_REPORT_BATCH_SIZE,

        // No of past X Quarters to consider for processing ATF payments
        PAST_ATF_PAYMENTS_INTERVAL,

        ENABLE_BA_CHANGE_EMAIL_NOTIFICATION,
        ENABLE_EBA_ADD_EMAIL_NOTIFICATION,
        ENABLE_PBA_ADD_EMAIL_NOTIFICATION,

        //PSP-12037 temporary (remove after 02/28/2017)
        SPECIAL_OFFER_ACTIVE,
        SPECIAL_OFFER_CODE,

        //PSP-4929 defining threshold for difference in credit and debit totals
        CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD,
        PERFORM_CREDIT_DEBIT_TOTALS_CHECK,

        //PSP-12282 defining threshold for difference in credit and debit totals
        PMT_TEMPLATES_FOR_REFUND_SPLIT,

        //PSP-12591 fraud payrolls batch size
        PROCESS_FRAUD_PAYROLLS_BATCH_SIZE,

        //PSP-11809 List of Fee types to be removed from OnHold
        REMOVE_ON_HOLD_FEE_TYPES,

        //Rollback flag for JSS-FLUX
        //PSP-13531 : Implement the rollback flag for batch jobs scheduled from SAP
        SCHEDULE_SAP_BATCHJOBS_USING_FLUX,

        //PSP-13891 Suppressing StaleObjectStateException happening during Batch Job processing
        SUPRESS_STALE_OBJECTS_BATCH_JOBS,

        //PSP-11389:Enhance the IamEmailProcessor job to pick records in batch and process in threadpool
        IAMEMAILADDRESSPROCESSOR_JOB_BATCH_SIZE,
        IAMEMAILADDRESS_CONTROLS_THREAD_POOL_INTERVAL,
        IAMEMAILADDRESS_CONTROLS_THREAD_POOL_MAX_WAIT,

        //PSP-13615: ResetQbdtFlags params to pick the company events created between Begin and end time
        RESET_QBDT_FLAGS_BEGIN_TIME,
        RESET_QBDT_FLAGS_END_TIME,
        ENTITLED_OFFER_CODE,

        PRIMARY_BATCH_JOB_MULTI_OFFLOAD_ENABLED,

        //Flag to enable or disable use of IDPS for PTC decryption tool
        ENABLE_IDPS_ENCRYPTION_FOR_PTC,
        //Flag to enable or disable use of IDPS Encryption/Decryption
        PSP_ENCRYPTION_FLAG,
        //PSP-16104 Adding TimeOut functionality for SAP sqlconsole session
        SQL_QUERY_TIMEOUT,
        EFE_REQUEST_TIMEOUT,

        //For build error fix,added below enums.Below ENUMS we are using in batch jobs.
        LAST_COMPANY_ONHOLD_MESSAGE_PROCESSED_TIME,
        ASSISTED_USAGE_BILLING_TOKEN,
        LAST_PAYROLL_RUN_PROCESSED_TIME,
        ENTITY_CONNECTION_RETRY_ATTEMPTS,
        ENTITY_CONNECTION_RETRY_WAIT_PERIOD,
        ENTITY_MESSAGE_RECEIVE_TIMEOUT,
        ENTITY_BATCH_SIZE,
        ENTITY_RETRY_BATCH_SIZE,
        ENTITY_THREAD_POOL_INTERVAL,
        ENTITY_THREAD_POOL_MAX_WAIT,
        ENTITY_MIN_THREAD_POOL_SIZE,
        ENTITY_MAX_THREAD_POOL_SIZE,
        LAST_EVENT_RETRY_PROCESSED_TIME,
        LAST_PROCESSED_TIME,
        ACHTRACEID_CONTROLS_THREAD_POOL_INTERVAL,
        ACHTRACEID_CONTROLS_THREAD_POOL_MAX_WAIT,
        ACHTRACEID_CONTROLS_THREAD_POOL_RECORDS_PER_THREAD,
        ACHTRACEID_CONTROLS_THREAD_POOL_RECORDS_PER_BATCH,
        SFTP_RETRY_SLEEP_MILLIS,
        FFCRA_END_DATE,
        CARES_END_DATE,
        SFTP_A4_MARGIN_LEFT,
        SFTP_A4_MARGIN_RIGHT,
        SFTP_A4_MARGIN_TOP,
        SFTP_A4_MARGIN_BOTTOM,

        //FOR KY_WH state report processor
        KY_SUBMISSION_SEQ,
        KY_PROCESS_TYPE,

        //For Equifax integration
        ENTITY_PUBLISH_BATCH_SIZE,
        ENTITY_CHUNK_SIZE,
        EVS_LAST_PROCESSED_TIME,
        BATCH_TARGETED_FOR_SERVICE,
        ENTITY_PUBLISH_STATUS,

        //For Workforce
        MAX_COMPANIES_PER_RUN,
        MAX_WORKFORCE_INVITE_PER_DAY,
        EMAIL_TEMPLATE_NAME,
        LAST_PAYROLL_RUN_DURATION_COMPANY,
        LAST_PAID_DURATION_EMPLOYEE,
        WORKFORCE_INVITE_COVERED,
        PUBLISH_STATUS_WORKFORCE,
        WORKFORCE_LATEST_INVITE_DATE,
        JPMC_NACHA_FILE_UPLOAD_DELAY,
        WORKERS_COMP,
        MANUAL_LEDGER_TAX_BLOCK_LIMIT,
        MANUAL_LEDGER_TAX_WARNING_LIMIT,
        DEFAULT_EMPLOYER_FEE_LIMIT,
        WORKFORCE_INVITE_MAX_RETRY,
        WORKFORCE_COMPANY_EVENTS_FETCH_HOURS,

        PAYROLL_REFUND_REBILL_LIMIT,
        DEFAULT_PENALTY_REFUND_AMOUNT,
        DEFAULT_INTEREST_REFUND_AMOUNT,
        DEFAULT_COURTESY_REFUND_AMOUNT,
        DEFAULT_UPDATE_PAYMENTS_AMOUNT,
        DEFAULT_BULK_EOQV_AMOUNT;
    }
}
