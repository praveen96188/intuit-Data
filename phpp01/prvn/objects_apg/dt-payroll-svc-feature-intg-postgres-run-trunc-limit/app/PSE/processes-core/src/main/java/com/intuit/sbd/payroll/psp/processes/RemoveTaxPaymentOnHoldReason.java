package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentOnHoldReason;

/**
 * User: rnorian
 * Date: Jan 25, 2011
 * Time: 11:13:47 PM
 */
public class RemoveTaxPaymentOnHoldReason extends Process implements IProcess {

    private MoneyMovementTransaction taxPaymentMMT;
    private PaymentOnHoldReason paymentOnHoldReason;

    public RemoveTaxPaymentOnHoldReason(MoneyMovementTransaction pTaxPaymentMMT, PaymentOnHoldReason pPaymentOnHoldReason) {
        taxPaymentMMT = pTaxPaymentMMT;
        paymentOnHoldReason = pPaymentOnHoldReason;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();

        if (taxPaymentMMT == null) {
            result.getMessages().BadProcessArgument("taxPaymentMMT");
        }

        if (taxPaymentMMT == null) {
            result.getMessages().BadProcessArgument("paymentOnHoldReason");
        }

        return result;
    }

    @Override
    public ProcessResult process() {
        ProcessResult result = new ProcessResult();

        MoneyMovementTransaction.removeTaxPaymentOnHoldReason(taxPaymentMMT, paymentOnHoldReason);

        return result;
    }


}
