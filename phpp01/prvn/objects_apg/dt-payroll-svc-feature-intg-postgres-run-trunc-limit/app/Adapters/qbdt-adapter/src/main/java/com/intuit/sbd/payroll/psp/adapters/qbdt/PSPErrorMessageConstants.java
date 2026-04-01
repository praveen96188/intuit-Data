package com.intuit.sbd.payroll.psp.adapters.qbdt;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Sep 16, 2008
 * Time: 11:29:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class PSPErrorMessageConstants {

    public static final String COMPANY_HAS_PENDING_FINANCIAL_TRANSACTIONS = "175";
    public static final String COMPANY_HAS_RECENT_BANK_ACTIVITY = "182";
    public static final String DUPLICATE_PAYROLL_RUN_ID = "183";
    public static final String DUPLICATE_PAYCHECK_ID = "184";
    public static final String TRANSACTION_ALREADY_CANCELLED = "196";
    public static final String PAYROLL_ALREADY_CANCELLED = "1017";
    public static final String COMPANY_HAS_UNRESOLVED_BANK_RETURNS = "227";
    public static final String TRANSACTIONS_OFFLOADED_CANNOT_RECALL = "1015";
    public static final String TRANSACTION_NO_LONGER_PENDING_CANNOT_CANCEL = "258";

    public static final String PAYCHECK_DATE_TOO_FAR_IN_FUTURE = "109";
    public static final String PAYCHECK_DATE_AFTER_DISCO_DATE = "111";
    public static final String INVALID_ROUTING_NUMBER = "255";
    public static final String PAYROLL_SUBMITTED_WITH_PENDING_NOC = "2301";
    public static final String PAYROLL_SUBMITTED_WITH_PENDING_EE_RETURN = "2501";
    public static final String COMPANY_OPERATION_NOT_ALLOWED = "1101";
    public static final String COMPANY_BANK_ACCOUNT_NOT_ACTIVE = "1062";
    public static final String PAYROLL_RUN_EXCEEDS_DD_LIMITS = "1043";
    public static final String DUPLICATE_PAYCHECK_FOUND = "10090";
    public static final String OUTDATED_FFCRA_ITEMS = "12010";

    public static final String WARNING_PENDING_NOC = "301";
    public static final String WARNING_BACKDATE_FEE = "228";
}
