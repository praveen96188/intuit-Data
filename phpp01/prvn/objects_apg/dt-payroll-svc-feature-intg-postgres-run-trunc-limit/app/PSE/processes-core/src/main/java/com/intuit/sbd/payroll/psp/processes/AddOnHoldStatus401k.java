package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

public class AddOnHoldStatus401k extends Process implements IProcess {
    private ServiceSubStatusCode onHoldReasonCode;
    private Company company;

    public AddOnHoldStatus401k(Company pCompany, ServiceSubStatusCode pOnHoldReasonCd) {
        company = pCompany;
        onHoldReasonCode = pOnHoldReasonCd;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if (onHoldReasonCode == ServiceSubStatusCode.Fraud || onHoldReasonCode == ServiceSubStatusCode.FraudReview) {
            CompanyEvent.tokNotifiedOfFraudHoldEvent(company, onHoldReasonCode);
        }

        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate company exists
        if (company == null) {
            validationResult.getMessages().InvalidValue(EntityName.Company, null, "Company is null in AddOnHoldStatus401k.");
            return validationResult;
        }

        if (onHoldReasonCode == null) {
            validationResult.getMessages().InvalidValue(EntityName.ServiceSubStatus, null, "ServiceSubStatus is null in AddOnHoldStatus401k.");
            return validationResult;
        }

        return validationResult;
    }

}