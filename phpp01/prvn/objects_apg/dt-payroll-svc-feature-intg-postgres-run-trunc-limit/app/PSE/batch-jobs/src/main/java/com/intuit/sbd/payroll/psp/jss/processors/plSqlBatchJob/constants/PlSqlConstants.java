package com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.constants;

public class PlSqlConstants {

    public static final String OFFERING_UPDATE_USAGE_BILLING = "Number of Rows Updated For Offering update usage billing:: PSP_COMPANY_OFFERING : %s";
    public static final String EDR_ASSOCIATION_FIX = "Number of Rows Updated For psp_entry_detail_record : %s";
    public static final String EFTPS_ONHOLD_PAYMENT = "Number of Rows Updated for money movement : %s";
    public static final String NCD_FIX = "Number of Rows Updated For NCD fix psp_entitlement : %s";
    public static final String VALIDATE_EMPLOYEE_WAGE_PLANS = "Number of Rows Updated psp_employee_wage_plan : %s";
    public static final String FAILED_PAYROLL = "Number of Rows Updated from Error to Pending : %s";
    public static final String RETRY_ENTITLEMENT_ACTIVATION = "JIRA-14162 :Number of Rows Updated For PSP_ENTITLEMENT_UNIT : %s";
    public static final String NCD_FIX_ALL = "Retail Activation customers :Number of Rows Updated For PSP_ENTITLEMENT : %s";
    public static final String PAYROLL_FRAUD_BATCH_PURGE = "Number of rows deleted for payroll fraud batch : %s";

    public static final String PAYROLL_FRAUD_BATCH_PURGE_PLSQL_JOBS_PROCESSOR = "PayrollFraudBatchPurgePlSqlJobsProcessor";
    public static final String COST_CO_PLSQL_JOBS_PROCESSOR  = "CostCoPlSqlJobsProcessor";
    public static final String FAILED_PAYROLL_PLSQL_JOBS_PROCESSOR = "FailedPayrollPlSqlJobsProcessor";
    public static final String EFTPS_ONHOLD_PAYMENT_PLSQL_JOBS_PROCESSOR = "EFTPSOnHoldPaymentPlSqlJobsProcessor";
    public static final String VALIDATE_EMPLOYEE_WAGE_PLANS_PLSQL_JOBS_PROCESSOR = "ValidateEmployeeWagePlansPlSqlJobsProcessor";
    public static final String NCD_FIX_ALL_PLSQL_JOBS_PROCESSOR = "NCDFixALLPlSqlJobsProcessor";
    public static final String OFFERING_UPDATE_USAGE_BILLING_PLSQL_JOBS_PROCESSOR = "OfferingUpdateUsageBillingPlSqlJobsProcessor";
    public static final String EDR_ASSOCIATION_FIX_PLSQL_JOBS_PROCESSOR = "EDRAssociationFixPlSqlJobsProcessor";
    public static final String NCD_FIX_PLSQL_JOBS_PROCESSOR = "NCDFixPlSqlJobsProcessor";
    public static final String RETRY_ENTITLEMENT_ACTIVATION_PLSQL_JOBS_PROCESSOR = "RetryEntitlementActivationPlSqlJobsProcessor";

    public static final String PRC_COST_CO_PLSQL_JOBS_PROCESSOR="prc_cost_co_plsql_jobs_processor";
    public static final String PRC_PAYROLL_FRAUDBATCH_PURGE_DBUPGRADE_PLSQL_JOBS_PROCESSOR="prc_payroll_fraudbatch_purge_dbupgrade_plsql_jobs_processor";

}
