package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentOnHoldReason;
import com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: rnorian
 * Date: Jan 25, 2011
 * Time: 5:49:26 PM
 */
public class AddTaxPaymentOnHoldReason extends Process implements IProcess {
    private MoneyMovementTransaction taxPaymentMMT;
    private PaymentOnHoldReason paymentOnHoldReason;

    public AddTaxPaymentOnHoldReason(MoneyMovementTransaction pTaxPaymentMMT, PaymentOnHoldReason pPaymentOnHoldReason) {
        this.taxPaymentMMT = pTaxPaymentMMT;
        this.paymentOnHoldReason = pPaymentOnHoldReason;
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

        if (!result.isSuccess()) {
            return result;
        }
               
        if (taxPaymentMMT.getTaxPaymentStatus() == TaxPaymentStatus.None)
            result.getMessages().GenericError(EntityName.MoneyMovementTransaction, taxPaymentMMT.getId().toString(),
                                              "Cannot add a PaymentOnHoldReason (" + paymentOnHoldReason + ") to a non-TaxPayment MoneyMovementTransaction: " + taxPaymentMMT.getCompany() + "  " + taxPaymentMMT);

        if (!taxPaymentMMT.isPendingMMT() || !taxPaymentMMT.isPendingTaxPayment()) {
            result.getMessages().GenericError(EntityName.MoneyMovementTransaction, taxPaymentMMT.getId().toString(),
                                              "Cannot add a PaymentOnHoldReason (" + paymentOnHoldReason + ") to a non-pending TaxPayment MoneyMovementTransaction: " + taxPaymentMMT.getCompany() + "  " + taxPaymentMMT);
        }

        return result;
    }

    @Override
    public ProcessResult process() {
        // only add an on hold reason to the MMT if there are pending ERTaxDebits
        // do not put MMT on hold if all ERTaxDebits are closed
        // do not create new MMT if all ERTaxDebits are closed; no new Payrolls will be accepted when company is on hold
        ProcessResult result = new ProcessResult();

        MoneyMovementTransaction.addTaxPaymentOnHoldReason(taxPaymentMMT, paymentOnHoldReason);

        return result;
    }
}
