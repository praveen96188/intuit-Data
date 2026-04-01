package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: vidhyak689
 * Date: 8/20/12
 * Time: 1:18 PM
 */
public class SAPUsageBillingInvoice {
    private String billPOID = null;
    private Date statementDate = null;
    private SAPUsageBillingInvoiceDetail invoiceDetail;

    public String getBillPOID() {
        return billPOID;
    }

    public void setBillPOID(String pBillPOID) {
        billPOID = pBillPOID;
    }

    public Date getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(Date pStatementDate) {
        statementDate = pStatementDate;
    }

    public SAPUsageBillingInvoiceDetail getInvoiceDetail() {
        return invoiceDetail;
    }

    public void setInvoiceDetail(SAPUsageBillingInvoiceDetail pInvoiceDetail) {
        invoiceDetail = pInvoiceDetail;
    }
}
