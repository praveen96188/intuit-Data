package com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps;

import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.SyntacticAddressValidator;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.ValidationStep;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;

public class SyntacticAddressValidationStep implements ValidationStep< SMSValidationResult> {


    private final AddressDTO addressDTO;
    private final AddressTypeEnum addressType;

    public SyntacticAddressValidationStep(AddressDTO addressDTO, AddressTypeEnum addressType){
            this.addressDTO = addressDTO;
            this.addressType = addressType;
    }


    @Override
    public SMSValidationResult process() {

        SyntacticAddressValidator syntacticAddressValidator = new SyntacticAddressValidator(addressType);
        SMSValidationResult smsValidationResult = syntacticAddressValidator.validateAddressSyntax(addressDTO);
        return smsValidationResult;
    }
}
