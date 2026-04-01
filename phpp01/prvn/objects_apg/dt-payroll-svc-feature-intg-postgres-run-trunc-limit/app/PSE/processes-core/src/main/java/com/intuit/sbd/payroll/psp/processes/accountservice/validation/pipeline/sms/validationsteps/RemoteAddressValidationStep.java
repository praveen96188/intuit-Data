package com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps;


import com.intuit.payments.cdm.v2.client.PhysicalAddress;
import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.sbd.payroll.psp.gateways.validationservice.gateway.ValidationServiceGateway;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.ValidationStep;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;

public class RemoteAddressValidationStep implements ValidationStep< SMSValidationResult> {

    private ValidationServiceGateway validationServiceGateway = PayrollApplicationBeanFactory.getBean(ValidationServiceGateway.class);


    private final PhysicalAddress physicalAddressToValidate;
    private final AddressTypeEnum addressType;

    public RemoteAddressValidationStep(PhysicalAddress physicalAddressToValidate, AddressTypeEnum addressType) {
        this.physicalAddressToValidate = physicalAddressToValidate;
        this.addressType = addressType;
    }

    @Override
    public SMSValidationResult process() {
        SMSValidationResult addressValidationResult = validationServiceGateway.validatePhysicalAddress(physicalAddressToValidate, addressType);
        return addressValidationResult;
    }
}
