package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;

/**
 * User: rnorian
 * Date: Jan 25, 2011
 * Time: 9:06:31 PM
 *
 * This process is called from AddOnHoldStatusCore in response to a 'Company' level
 * OnHold reason being added.
 */
public class AddOnHoldStatusTax extends Process implements IProcess {
    private Company company;
    private ServiceSubStatusCode onHoldReasonCode;

    public AddOnHoldStatusTax(Company pCompany, ServiceSubStatusCode pOnHoldReasonCode) {
        company = pCompany;
        onHoldReasonCode = pOnHoldReasonCode;
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

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        for (MoneyMovementTransaction pendingPaymentMMT : company.findPendingTaxPayments()) {
            if (pendingPaymentMMT.hasPendingERTaxDebits()) {
                processResult.merge(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(pendingPaymentMMT, PaymentOnHoldReason.Company));
            }
        }

        if (!ServiceSubStatus.isAS400HoldReason(onHoldReasonCode)) {
            CompanyEvent.createCompanyEvent(company, EventTypeCode.PSPToAS400HoldSync);
        }

        return processResult; 
    }
}
