package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;

public enum StoredProcedures {

    PRC_ACHTRANSACTIONPROCESSOR,
    PRC_CALCULATE_W2_TOTALS,
    PRC_EFTPS_PAYMENTS_RESPONSE,
    PRC_EFTPS_PAYMENTS_RETURN,
    PRC_EFTPS_PAYMENTS_SENT,
    PRC_EFTPS_PAYMENTS_SENT_EVENTS,
    PRC_OFFLOAD,
    PRC_OFFLOAD_INSERT_FTS,
    PRC_OFFLOAD_UPD_AGENCY_STATUS,
    PRC_OFFLOAD_UPDATE_FT,
    PRC_OFFLOAD_UPDATE_MMT,
    PRC_OFFLOAD_UPDATE_PAYROLL,
    PRC_REMOVE_COMPANY_FAST,
    PRC_UPD_COMPANY_LEDGER_BALANCE,
    PRC_UPDATE_LEDGER_BALANCE,
    PRC_PAYROLL_FRAUDBATCH_PURGE_DBUPGRADE_PLSQL_JOBS_PROCESSOR,
    PRC_COST_CO_PLSQL_JOBS_PROCESSOR,

    //Packages and their converted Names in Postgres
    GEMS_ACCOUNTS_RECEIVABLE_MAIN("PK_GEMS_ACCOUNTS_RECEIVABLE.PRC_MAIN",
            "FN_GEMS_ACCOUNTS_RECEIVABLE_MAIN"),
    PAYROLL_ITEM_TOTALS_COMP_QTR_PAYROLL_ITEM_TOT("PK_PAYROLL_ITEM_TOTALS.PRC_COMP_QTR_PAYROLL_ITEM_TOT",
            "PRC_PAYROLL_ITEM_TOTALS_COMP_QTR_PAYROLL_ITEM_TOT"),
    PAYROLL_ITEM_TOTALS_QTR_PAYROLL_ITEM_TOT("PK_PAYROLL_ITEM_TOTALS.PRC_QTR_PAYROLL_ITEM_TOT",
            "PRC_PAYROLL_ITEM_TOTALS_QTR_PAYROLL_ITEM_TOT"),
    PAYROLL_ITEM_TOTALS_YEAR_PAYROLL_ITEM_TOT("PK_PAYROLL_ITEM_TOTALS.PRC_YEAR_PAYROLL_ITEM_TOT",
            "PRC_PAYROLL_ITEM_TOTALS_YEAR_PAYROLL_ITEM_TOT"),

    // For Test case
    TEMP_TEST_PROC("TEMP_TEST_PROC_ORACLE",
            "TEMP_TEST_PROC_POSTGRES");

    private final String storedProcedureName;
    private final String postgresStoredProcedureName;

    StoredProcedures() {
        this.storedProcedureName = this.name().toLowerCase();
        this.postgresStoredProcedureName = this.name().toLowerCase();
    }

    StoredProcedures(String storedProcedureName, String postgresStoredProcedureName) {
        this.storedProcedureName = storedProcedureName.toLowerCase();
        this.postgresStoredProcedureName = postgresStoredProcedureName.toLowerCase();
    }

    public String getStoredProcedureName() {
        if (Application.isPostgresDB()) {
            return postgresStoredProcedureName;
        }
        return storedProcedureName;
    }

    @Override
    public String toString() {
        return getStoredProcedureName();
    }

}