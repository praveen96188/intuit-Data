package com.intuit.sbd.payroll.psp.hibernate;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class StoredProceduresTests {

    private static String prc_achtransactionprocessor = "prc_achtransactionprocessor";
    private static String prc_calculate_w2_totals = "prc_calculate_w2_totals";
    private static String prc_eftps_payments_response = "prc_eftps_payments_response";
    private static String prc_eftps_payments_return = "prc_eftps_payments_return";
    private static String prc_eftps_payments_sent = "prc_eftps_payments_sent";
    private static String prc_eftps_payments_sent_events = "prc_eftps_payments_sent_events";
    private static String prc_offload = "prc_offload";
    private static String prc_offload_insert_fts = "prc_offload_insert_fts";
    private static String prc_offload_upd_agency_status = "prc_offload_upd_agency_status";
    private static String prc_offload_update_ft = "prc_offload_update_ft";
    private static String prc_offload_update_mmt = "prc_offload_update_mmt";
    private static String prc_offload_update_payroll = "prc_offload_update_payroll";
    private static String prc_remove_company_fast = "prc_remove_company_fast";
    private static String prc_upd_company_ledger_balance = "prc_upd_company_ledger_balance";
    private static String prc_update_ledger_balance = "prc_update_ledger_balance";
    private static String pk_gems_accounts_receivable_prc_main = "fn_gems_accounts_receivable_main";
    private static String pk_payroll_item_totals_prc_comp_qtr_payroll_item_tot = "pk_payroll_item_totals.prc_comp_qtr_payroll_item_tot";
    private static String pk_payroll_item_totals_prc_qtr_payroll_item_tot = "pk_payroll_item_totals.prc_qtr_payroll_item_tot";
    private static String pk_payroll_item_totals_prc_year_payroll_item_tot = "pk_payroll_item_totals.prc_year_payroll_item_tot";

    @Ignore
    @Test
    public void testGetStoredProcedureName() {
        Assert.assertEquals(prc_achtransactionprocessor, StoredProcedures.PRC_ACHTRANSACTIONPROCESSOR.getStoredProcedureName());
        Assert.assertEquals(prc_calculate_w2_totals, StoredProcedures.PRC_CALCULATE_W2_TOTALS.getStoredProcedureName());
        Assert.assertEquals(prc_eftps_payments_response, StoredProcedures.PRC_EFTPS_PAYMENTS_RESPONSE.getStoredProcedureName());
        Assert.assertEquals(prc_eftps_payments_return, StoredProcedures.PRC_EFTPS_PAYMENTS_RETURN.getStoredProcedureName());
        Assert.assertEquals(prc_eftps_payments_sent, StoredProcedures.PRC_EFTPS_PAYMENTS_SENT.getStoredProcedureName());
        Assert.assertEquals(prc_eftps_payments_sent_events, StoredProcedures.PRC_EFTPS_PAYMENTS_SENT_EVENTS.getStoredProcedureName());
        Assert.assertEquals(prc_offload, StoredProcedures.PRC_OFFLOAD.getStoredProcedureName());
        Assert.assertEquals(prc_offload_insert_fts, StoredProcedures.PRC_OFFLOAD_INSERT_FTS.getStoredProcedureName());
        Assert.assertEquals(prc_offload_upd_agency_status, StoredProcedures.PRC_OFFLOAD_UPD_AGENCY_STATUS.getStoredProcedureName());
        Assert.assertEquals(prc_offload_update_ft, StoredProcedures.PRC_OFFLOAD_UPDATE_FT.getStoredProcedureName());
        Assert.assertEquals(prc_offload_update_mmt, StoredProcedures.PRC_OFFLOAD_UPDATE_MMT.getStoredProcedureName());
        Assert.assertEquals(prc_offload_update_payroll, StoredProcedures.PRC_OFFLOAD_UPDATE_PAYROLL.getStoredProcedureName());
        Assert.assertEquals(prc_remove_company_fast, StoredProcedures.PRC_REMOVE_COMPANY_FAST.getStoredProcedureName());
        Assert.assertEquals(prc_upd_company_ledger_balance, StoredProcedures.PRC_UPD_COMPANY_LEDGER_BALANCE.getStoredProcedureName());
        Assert.assertEquals(prc_update_ledger_balance, StoredProcedures.PRC_UPDATE_LEDGER_BALANCE.getStoredProcedureName());
        Assert.assertEquals(pk_gems_accounts_receivable_prc_main, StoredProcedures.GEMS_ACCOUNTS_RECEIVABLE_MAIN.getStoredProcedureName());
        Assert.assertEquals(pk_payroll_item_totals_prc_comp_qtr_payroll_item_tot, StoredProcedures.PAYROLL_ITEM_TOTALS_COMP_QTR_PAYROLL_ITEM_TOT.getStoredProcedureName());
        Assert.assertEquals(pk_payroll_item_totals_prc_qtr_payroll_item_tot, StoredProcedures.PAYROLL_ITEM_TOTALS_QTR_PAYROLL_ITEM_TOT.getStoredProcedureName());
        Assert.assertEquals(pk_payroll_item_totals_prc_year_payroll_item_tot, StoredProcedures.PAYROLL_ITEM_TOTALS_YEAR_PAYROLL_ITEM_TOT.getStoredProcedureName());
    }

    @Ignore
    @Test
    public void testToStringMethod() {
        Assert.assertEquals(prc_achtransactionprocessor, StoredProcedures.PRC_ACHTRANSACTIONPROCESSOR.toString());
        Assert.assertEquals(prc_calculate_w2_totals, StoredProcedures.PRC_CALCULATE_W2_TOTALS.toString());
        Assert.assertEquals(prc_eftps_payments_response, StoredProcedures.PRC_EFTPS_PAYMENTS_RESPONSE.toString());
        Assert.assertEquals(prc_eftps_payments_return, StoredProcedures.PRC_EFTPS_PAYMENTS_RETURN.toString());
        Assert.assertEquals(prc_eftps_payments_sent, StoredProcedures.PRC_EFTPS_PAYMENTS_SENT.toString());
        Assert.assertEquals(prc_eftps_payments_sent_events, StoredProcedures.PRC_EFTPS_PAYMENTS_SENT_EVENTS.toString());
        Assert.assertEquals(prc_offload, StoredProcedures.PRC_OFFLOAD.toString());
        Assert.assertEquals(prc_offload_insert_fts, StoredProcedures.PRC_OFFLOAD_INSERT_FTS.toString());
        Assert.assertEquals(prc_offload_upd_agency_status, StoredProcedures.PRC_OFFLOAD_UPD_AGENCY_STATUS.toString());
        Assert.assertEquals(prc_offload_update_ft, StoredProcedures.PRC_OFFLOAD_UPDATE_FT.toString());
        Assert.assertEquals(prc_offload_update_mmt, StoredProcedures.PRC_OFFLOAD_UPDATE_MMT.toString());
        Assert.assertEquals(prc_offload_update_payroll, StoredProcedures.PRC_OFFLOAD_UPDATE_PAYROLL.toString());
        Assert.assertEquals(prc_remove_company_fast, StoredProcedures.PRC_REMOVE_COMPANY_FAST.toString());
        Assert.assertEquals(prc_upd_company_ledger_balance, StoredProcedures.PRC_UPD_COMPANY_LEDGER_BALANCE.toString());
        Assert.assertEquals(prc_update_ledger_balance, StoredProcedures.PRC_UPDATE_LEDGER_BALANCE.toString());
        Assert.assertEquals(pk_gems_accounts_receivable_prc_main, StoredProcedures.GEMS_ACCOUNTS_RECEIVABLE_MAIN.toString());
        Assert.assertEquals(pk_payroll_item_totals_prc_comp_qtr_payroll_item_tot, StoredProcedures.PAYROLL_ITEM_TOTALS_COMP_QTR_PAYROLL_ITEM_TOT.toString());
        Assert.assertEquals(pk_payroll_item_totals_prc_qtr_payroll_item_tot, StoredProcedures.PAYROLL_ITEM_TOTALS_QTR_PAYROLL_ITEM_TOT.toString());
        Assert.assertEquals(pk_payroll_item_totals_prc_year_payroll_item_tot, StoredProcedures.PAYROLL_ITEM_TOTALS_YEAR_PAYROLL_ITEM_TOT.toString());
    }

    @Test
    public void testNameMethod() {
        Assert.assertEquals("PRC_ACHTRANSACTIONPROCESSOR", StoredProcedures.PRC_ACHTRANSACTIONPROCESSOR.name());
        Assert.assertEquals("GEMS_ACCOUNTS_RECEIVABLE_MAIN", StoredProcedures.GEMS_ACCOUNTS_RECEIVABLE_MAIN.name());

        Assert.assertEquals(prc_achtransactionprocessor.toUpperCase(), StoredProcedures.PRC_ACHTRANSACTIONPROCESSOR.name());
        Assert.assertEquals(prc_calculate_w2_totals.toUpperCase(), StoredProcedures.PRC_CALCULATE_W2_TOTALS.name());
        Assert.assertEquals(prc_eftps_payments_response.toUpperCase(), StoredProcedures.PRC_EFTPS_PAYMENTS_RESPONSE.name());
        Assert.assertEquals(prc_eftps_payments_return.toUpperCase(), StoredProcedures.PRC_EFTPS_PAYMENTS_RETURN.name());
        Assert.assertEquals(prc_eftps_payments_sent.toUpperCase(), StoredProcedures.PRC_EFTPS_PAYMENTS_SENT.name());
        Assert.assertEquals(prc_eftps_payments_sent_events.toUpperCase(), StoredProcedures.PRC_EFTPS_PAYMENTS_SENT_EVENTS.name());
        Assert.assertEquals(prc_offload.toUpperCase(), StoredProcedures.PRC_OFFLOAD.name());
        Assert.assertEquals(prc_offload_insert_fts.toUpperCase(), StoredProcedures.PRC_OFFLOAD_INSERT_FTS.name());
        Assert.assertEquals(prc_offload_upd_agency_status.toUpperCase(), StoredProcedures.PRC_OFFLOAD_UPD_AGENCY_STATUS.name());
        Assert.assertEquals(prc_offload_update_ft.toUpperCase(), StoredProcedures.PRC_OFFLOAD_UPDATE_FT.name());
        Assert.assertEquals(prc_offload_update_mmt.toUpperCase(), StoredProcedures.PRC_OFFLOAD_UPDATE_MMT.name());
        Assert.assertEquals(prc_offload_update_payroll.toUpperCase(), StoredProcedures.PRC_OFFLOAD_UPDATE_PAYROLL.name());
        Assert.assertEquals(prc_remove_company_fast.toUpperCase(), StoredProcedures.PRC_REMOVE_COMPANY_FAST.name());
        Assert.assertEquals(prc_upd_company_ledger_balance.toUpperCase(), StoredProcedures.PRC_UPD_COMPANY_LEDGER_BALANCE.name());
        Assert.assertEquals(prc_update_ledger_balance.toUpperCase(), StoredProcedures.PRC_UPDATE_LEDGER_BALANCE.name());
        Assert.assertEquals("GEMS_ACCOUNTS_RECEIVABLE_MAIN", StoredProcedures.GEMS_ACCOUNTS_RECEIVABLE_MAIN.name());
        Assert.assertEquals("PAYROLL_ITEM_TOTALS_COMP_QTR_PAYROLL_ITEM_TOT", StoredProcedures.PAYROLL_ITEM_TOTALS_COMP_QTR_PAYROLL_ITEM_TOT.name());
        Assert.assertEquals("PAYROLL_ITEM_TOTALS_QTR_PAYROLL_ITEM_TOT", StoredProcedures.PAYROLL_ITEM_TOTALS_QTR_PAYROLL_ITEM_TOT.name());
        Assert.assertEquals("PAYROLL_ITEM_TOTALS_YEAR_PAYROLL_ITEM_TOT", StoredProcedures.PAYROLL_ITEM_TOTALS_YEAR_PAYROLL_ITEM_TOT.name());
    }

}
