package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;

/**
 * User: rnorian
 * Date: Jan 25, 2011
 * Time: 10:08:43 PM
 */
public class RemoveOnHoldStatusTax extends Process implements IProcess {

    private Company company;
    private ServiceSubStatusCode onHoldReasonCode;

    public RemoveOnHoldStatusTax(Company pCompany, ServiceSubStatusCode pOnHoldReasonCode) {
        this.company = pCompany;
        this.onHoldReasonCode = pOnHoldReasonCode;
    }

    @Override
    public ProcessResult process() {
        ProcessResult result = new ProcessResult();

        //If the only hold left on the company is from the AS400, we need to remove the PSP hold that is on the AS400
        if (company.isCompanyOnAS400HoldOnly() && !ServiceSubStatus.isAS400HoldReason(onHoldReasonCode)) {
            CompanyEvent.createCompanyEvent(company, EventTypeCode.PSPToAS400HoldRemoveSync);
        }

        // only expire PaymentOnHoldReason.Company and create hold remove event when the company is completely off of all PSP holds
        if (company.isCompanyOnHold()) {
            return result;
        }

        for (MoneyMovementTransaction holdMMT : MoneyMovementTransaction.findTaxPayments().setCompany(company).setOnHold().setNonDirect().find()) {
            result.merge(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(holdMMT, PaymentOnHoldReason.Company));
        }

        // company is no longer on any PSP holds
        if (!ServiceSubStatus.isAS400HoldReason(onHoldReasonCode)) {
            CompanyEvent.createCompanyEvent(company, EventTypeCode.PSPToAS400HoldRemoveSync);
        }

        return result;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();
        if (company == null) {
            result.getMessages().BadProcessArgument("company");
        }

        if (onHoldReasonCode == null) {
            result.getMessages().BadProcessArgument("onHoldReasonCode");
        }

        return result;
    }
}
