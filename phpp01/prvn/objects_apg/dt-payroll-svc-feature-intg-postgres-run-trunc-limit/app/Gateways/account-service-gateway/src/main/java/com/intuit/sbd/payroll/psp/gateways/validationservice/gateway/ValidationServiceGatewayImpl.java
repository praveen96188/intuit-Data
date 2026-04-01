package com.intuit.sbd.payroll.psp.gateways.validationservice.gateway;

import com.intuit.payments.cdm.v2.client.BusinessOwner;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.payments.cdm.v2.client.PhysicalAddress;
import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.sbd.payroll.psp.payments.PaymentServiceAuthorizationManager;
import com.intuit.sbg.psp.validationservices.ValidationServiceClient;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ValidationServiceGateway")
public class ValidationServiceGatewayImpl implements ValidationServiceGateway {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(ValidationServiceGatewayImpl.class);

    private PaymentServiceAuthorizationManager authorizationManager;
    private ValidationServiceClient validationServiceClient;

    @Autowired
    public ValidationServiceGatewayImpl(PaymentServiceAuthorizationManager paymentServiceAuthorizationManager,ValidationServiceClient validationServiceClient) {
        this.validationServiceClient = validationServiceClient;
        this.authorizationManager = paymentServiceAuthorizationManager;

    }

    @Override
    public SMSValidationResult validatePaymentsAccount(PaymentsAccount paymentsAccount,String psid) {
        SMSValidationResult paymentsAccountValidationResult = null;
        try {
            authorizationManager.setValidationServiceAuthorizationContext();
           paymentsAccountValidationResult = validationServiceClient.validatePaymentsAccounts(paymentsAccount, psid);
            logger.info("Payments Account validate for RealmId=" + paymentsAccount.getRealmId());
            return paymentsAccountValidationResult;
        } catch (Exception validationServicesException) {
            logger.info("Exception happened while validating PaymentsAccount");
            throw validationServicesException;
        } finally {
            authorizationManager.removeBrowserAuthorizationContext();
        }
    }

    @Override
    public SMSValidationResult validatePhysicalAddress(PhysicalAddress physicalAddress,AddressTypeEnum addressType) {
        SMSValidationResult physicalAddressValidationResult = null;
        try {
            authorizationManager.setValidationServiceAuthorizationContext();
            physicalAddressValidationResult = validationServiceClient.validatePhysicalAddress(physicalAddress,addressType);
            logger.info("Physical address validated for ");
            return physicalAddressValidationResult;
        } catch (Exception validationServicesException) {
            logger.error("Error in physical address validation  "+validationServicesException.getMessage());
            throw validationServicesException;
        } finally {
            //todo check if this is needed
            authorizationManager.removeBrowserAuthorizationContext();
        }
    }

    @Override
    public SMSValidationResult validateSSN(BusinessOwner businessOwner) {
        SMSValidationResult ssnValidationResult = null;
        try {
            authorizationManager.setValidationServiceAuthorizationContext();
            ssnValidationResult = validationServiceClient.validateSSN(businessOwner);
            logger.info("SSN validated for ");
            return ssnValidationResult;
        } catch (Exception validationServicesException) {
            logger.error("Error in SSN  validation  "+validationServicesException.getMessage());
            throw validationServicesException;
        } finally {
            //todo check if this is needed
            authorizationManager.removeBrowserAuthorizationContext();
        }
    }

    @Override
    public SMSValidationResult validateTIN(String legalName, String ein) {
        SMSValidationResult tinValidationResult = null;
        try {
            authorizationManager.setValidationServiceAuthorizationContext();
            tinValidationResult = validationServiceClient.validateTIN(legalName,ein);
            logger.info("EIN validated for ");
            return tinValidationResult;
        } catch (Exception validationServicesException) {
            logger.error("Error in EIN  validation  "+validationServicesException.getMessage());
            throw validationServicesException;
        } finally {
            //todo check if this is needed
            authorizationManager.removeBrowserAuthorizationContext();
        }
    }


}
