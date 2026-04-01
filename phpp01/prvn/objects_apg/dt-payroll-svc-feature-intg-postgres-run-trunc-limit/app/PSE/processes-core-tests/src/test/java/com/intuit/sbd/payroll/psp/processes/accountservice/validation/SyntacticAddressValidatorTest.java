package com.intuit.sbd.payroll.psp.processes.accountservice.validation;


import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
public class SyntacticAddressValidatorTest {

    @Test
    public void addressLineShouldNotContainPObox(){

        SyntacticAddressValidator syntacticAddressValidator = new SyntacticAddressValidator(AddressTypeEnum.COMPANY);


        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("P O box  244");
        addressDTO.setAddressLine2("abc def");
        addressDTO.setState("CA");
        addressDTO.setCity("San Francisco");
        addressDTO.setZipCode("90404");
        addressDTO.setZipCodeExtension("9021");
        addressDTO.setCountry("US");



        SMSValidationResult smsValidationResult = syntacticAddressValidator.validateAddressSyntax(addressDTO);
        assertTrue(smsValidationResult.getErrors().get(0).getEntity().equals("businessInfo.addressLine1"));


    }

    @Test
    public void testValidationConstraintsFailuresForBusinessAddres(){

        SyntacticAddressValidator syntacticAddressValidator = new SyntacticAddressValidator(AddressTypeEnum.COMPANY);


        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("P O box  244");
        addressDTO.setAddressLine2("po  box");
        addressDTO.setState("XY");
        addressDTO.setCity("San Francisco%#$%");
        addressDTO.setZipCode("90404#");
        addressDTO.setZipCodeExtension("9021");
        addressDTO.setCountry("US");


        SMSValidationResult smsValidationResult = syntacticAddressValidator.validateAddressSyntax(addressDTO);
        assertEquals(smsValidationResult.getErrors().size(),3);
        assertEquals(smsValidationResult.getWarnings().size(),1);
        List<String> failedEntities = Arrays.asList(smsValidationResult.getErrors().get(0).getEntity(),
                smsValidationResult.getErrors().get(1).getEntity(),
                smsValidationResult.getErrors().get(2).getEntity()
                );
        assertTrue(failedEntities.contains("businessInfo.postalCode"));
        assertTrue(failedEntities.contains("businessInfo.addressLine1"));
        //assertTrue(failedEntities.contains("businessInfo.addressLine2"));
        assertTrue(failedEntities.contains("businessInfo.city"));


    }


    @Test
    public void testValidationConstraintsFailuresForPrimaryPrincipalAddres(){

        SyntacticAddressValidator syntacticAddressValidator = new SyntacticAddressValidator(AddressTypeEnum.RESIDENCE);


        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("P O box  244");
        addressDTO.setAddressLine2("po  box");
        addressDTO.setState("XY");
        addressDTO.setCity("San Francisco%#$%");
        addressDTO.setZipCode("90404#");
        addressDTO.setZipCodeExtension("9021");
        addressDTO.setCountry("US");


        SMSValidationResult smsValidationResult = syntacticAddressValidator.validateAddressSyntax(addressDTO);
        assertEquals(smsValidationResult.getErrors().size(),3);
        assertEquals(smsValidationResult.getWarnings().size(),1);
        List<String> failedEntities = Arrays.asList(smsValidationResult.getErrors().get(0).getEntity(),
                smsValidationResult.getErrors().get(1).getEntity(),
                smsValidationResult.getErrors().get(2).getEntity()

        );
        assertTrue(failedEntities.contains("businessOwner[0].postalCode"));
        assertTrue(failedEntities.contains("businessOwner[0].addressLine1"));
       // assertTrue(failedEntities.contains("businessOwner[0].addressLine2"));
        assertTrue(failedEntities.contains("businessOwner[0].city"));


    }

    @Test
    public void testFullZipCodeValidationConstraintsFailures(){

        SyntacticAddressValidator syntacticAddressValidator = new SyntacticAddressValidator(AddressTypeEnum.RESIDENCE);


        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("1500 broadway");
        addressDTO.setAddressLine2("abc apt");
        addressDTO.setState("NV");
        addressDTO.setCity("San Francisco");
        addressDTO.setZipCode("90404");
        addressDTO.setZipCodeExtension("902221");
        addressDTO.setCountry("US");


        SMSValidationResult smsValidationResult = syntacticAddressValidator.validateAddressSyntax(addressDTO);
        assertEquals(smsValidationResult.getErrors().size(),0);
        assertEquals(smsValidationResult.getWarnings().size(),1);
        List<String> failedEntities = Arrays.asList(smsValidationResult.getWarnings().get(0).getEntity());
        assertEquals(failedEntities.get(0),"businessOwner[0].postalCode");


    }
}
