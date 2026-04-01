package com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps;

import com.intuit.sbd.payroll.psp.gateways.validationservice.gateway.ValidationServiceGateway;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.ValidationStep;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;

public class TINValidationStep implements ValidationStep< SMSValidationResult> {

    private final String  legalName;
    private final String ein;

    public TINValidationStep(String legalName,String ein){
        this.legalName = legalName;
        this.ein = ein;
    }
    private ValidationServiceGateway validationServiceGateway = PayrollApplicationBeanFactory.getBean(ValidationServiceGateway.class);

    @Override
    public SMSValidationResult process() {
       return validationServiceGateway.validateTIN(this.legalName,this.ein);
    }
}
