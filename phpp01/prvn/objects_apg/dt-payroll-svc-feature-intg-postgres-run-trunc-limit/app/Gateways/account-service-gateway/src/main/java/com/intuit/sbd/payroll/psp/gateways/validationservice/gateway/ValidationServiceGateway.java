package com.intuit.sbd.payroll.psp.gateways.validationservice.gateway;

import com.intuit.payments.cdm.v2.client.BusinessOwner;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.payments.cdm.v2.client.PhysicalAddress;
import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;

import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;

public interface ValidationServiceGateway {

    SMSValidationResult validatePaymentsAccount(PaymentsAccount paymentsAccountToValidate,String psid);

    SMSValidationResult validatePhysicalAddress(PhysicalAddress physicalAddressToValidate,AddressTypeEnum addressType);

    SMSValidationResult validateSSN(BusinessOwner businessOwner);

    SMSValidationResult validateTIN(String legalName, String ein);


}
