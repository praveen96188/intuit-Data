package com.intuit.sbd.payroll.psp.processes.accountservice.validation;


import com.google.gson.Gson;
import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbg.psp.validationservices.types.v1.ErrorDetail;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SyntacticAddressValidator {


    AddressTypeEnum addressType;

    public SyntacticAddressValidator(AddressTypeEnum addressType) {
        this.addressType = addressType;

    }

    /**
     * Does  syntactic validations on address, which was done
     * in moneymovement-onboarding-ui client side
     *
     * @param addressDTO
     * @return
     */
    public SMSValidationResult validateAddressSyntax(AddressDTO addressDTO) {


        SMSValidationResult smsValidationResult = new SMSValidationResult();

        String entityPrefix = "";
        if (this.addressType.equals(AddressTypeEnum.COMPANY)) {
            entityPrefix = "businessInfo.";
        } else if (this.addressType.equals(AddressTypeEnum.RESIDENCE)) {
            entityPrefix = "businessOwner[0].";
        }
        else if(this.addressType.equals(AddressTypeEnum.LEGAL)){
            entityPrefix = "complianceAddress.";
        }
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        AddressDTOProxy addressDTOProxy = createAddressDTOProxy(addressDTO);

        Set<ConstraintViolation<AddressDTOProxy>> violations = validator.validate(addressDTOProxy);

        boolean fullZipCodeValid = validateFullZipCode(addressDTOProxy);

        //make full zip code validation as warning not error
        //coz most of the times we wont get the postalcode extensions
        if (!fullZipCodeValid) {

            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setMessage("Full Zip Code does not satisfy the criterion");
            errorDetail.setType("StaticValidation");
            errorDetail.setEntity(StringUtils.join(entityPrefix, "postalCode"));

            smsValidationResult.getWarnings().add(errorDetail);
            smsValidationResult.setValidationPassed(true);

        }


        violations.forEach(violation -> {

            System.out.println("Validation constraint failed is: " + violation.getMessage());
            String propName = violation.getPropertyPath().toString();
            String violatedField = violation.getPropertyPath().toString();

            if (this.addressType.equals(AddressTypeEnum.COMPANY)) {
                propName = StringUtils.join("businessInfo.", propName);
            } else if (this.addressType.equals(AddressTypeEnum.RESIDENCE)) {
                propName = StringUtils.join("businessOwner[0].", propName);
            }
            else if (this.addressType.equals(AddressTypeEnum.LEGAL)) {
                propName = StringUtils.join("complianceAddress.", propName);
            }

            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setMessage(violation.getMessage());
            errorDetail.setType("StaticValidation");
            errorDetail.setEntity(propName);

            smsValidationResult.getErrors().add(errorDetail);
            smsValidationResult.setValidationPassed(false);

        });

        if (violations.size() > 0 ) {
            smsValidationResult.setValidationPassed(false);

        } else {
            System.out.println("Validation Successful");
            smsValidationResult.setValidationPassed(true);
        }
        return smsValidationResult;
    }

    private static boolean validateFullZipCode(AddressDTOProxy addressDTOProxy) {
        final String regex = "^\\d{5}(?:[-]\\d{4})?$";
        final String input = addressDTOProxy.getPostalCode() + "-" + addressDTOProxy.getZipCodeExtension();

        final Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
        final Matcher matcher = pattern.matcher(input);

        return matcher.find();
    }

    private static AddressDTOProxy createAddressDTOProxy(AddressDTO addressDTO) {
        AddressDTOProxy addressDTOProxy = new AddressDTOProxy();

        addressDTOProxy.setAddressLine1(addressDTO.getAddressLine1());
        addressDTOProxy.setAddressLine2(addressDTO.getAddressLine2());
        addressDTOProxy.setAddressLine3(addressDTO.getAddressLine3());
        addressDTOProxy.setCity(addressDTO.getCity());
        addressDTOProxy.setRegion(addressDTO.getState());
        addressDTOProxy.setPostalCode(addressDTO.getZipCode());
        addressDTOProxy.setZipCodeExtension(addressDTO.getZipCodeExtension());
        addressDTOProxy.setCountry(addressDTO.getCountry());
        return addressDTOProxy;
    }


}
