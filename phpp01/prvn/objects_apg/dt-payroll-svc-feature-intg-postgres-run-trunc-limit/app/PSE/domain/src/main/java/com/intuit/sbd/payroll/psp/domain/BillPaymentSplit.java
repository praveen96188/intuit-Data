package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class BillPaymentSplit extends BaseBillPaymentSplit {

    /**
     * Default constructor.
     */
    public BillPaymentSplit() {
        super();
    }

    public static BillPaymentSplit findBillPaymentSplit(PayrollRun pPayrollRun, String pSourceBillPaymentSplitId) {
        return pPayrollRun.getBillPaymentSplit(pSourceBillPaymentSplitId);
    }
}