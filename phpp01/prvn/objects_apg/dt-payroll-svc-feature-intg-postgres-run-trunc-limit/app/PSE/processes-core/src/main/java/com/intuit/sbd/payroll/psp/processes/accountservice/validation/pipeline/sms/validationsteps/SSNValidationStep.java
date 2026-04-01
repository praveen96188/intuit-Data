package com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps;

import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.sbd.payroll.psp.gateways.validationservice.gateway.ValidationServiceGateway;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.ValidationStep;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;

public class SSNValidationStep implements ValidationStep< SMSValidationResult> {

    private final PaymentsAccount paymentsAccount;

    public SSNValidationStep(PaymentsAccount paymentsAccount){
        this.paymentsAccount = paymentsAccount;
    }
    private ValidationServiceGateway validationServiceGateway = PayrollApplicationBeanFactory.getBean(ValidationServiceGateway.class);

    @Override
    public SMSValidationResult process() {
       return validationServiceGateway.validateSSN(paymentsAccount.getBusinessOwners().get(0));
    }
}
