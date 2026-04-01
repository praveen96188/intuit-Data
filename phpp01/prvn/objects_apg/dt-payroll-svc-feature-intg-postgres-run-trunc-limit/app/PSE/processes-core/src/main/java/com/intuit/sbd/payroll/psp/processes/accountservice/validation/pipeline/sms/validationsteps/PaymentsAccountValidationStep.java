package com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps;

import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.sbd.payroll.psp.gateways.validationservice.gateway.ValidationServiceGateway;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.ValidationStep;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;

public class PaymentsAccountValidationStep implements ValidationStep<SMSValidationResult> {

    private final PaymentsAccount paymentsAccountToValidate;
    private final String psid;
    private ValidationServiceGateway validationServiceGateway= PayrollApplicationBeanFactory.getBean(ValidationServiceGateway.class);

    public PaymentsAccountValidationStep(PaymentsAccount paymentsAccountToValidate,String psid){
        this.paymentsAccountToValidate = paymentsAccountToValidate;
        this.psid = psid;

    }

    @Override
    public SMSValidationResult process() {
        SMSValidationResult paymentsAccountValidationResult = validationServiceGateway.validatePaymentsAccount(paymentsAccountToValidate,psid);
        return paymentsAccountValidationResult;
    }
}
