package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * User: rnorian
 * Date: Jan 21, 2010
 * Time: 11:20:30 PM
 */
public class AddressDTOValidator {
    public ProcessResult validate(AddressDTO addressDTO) {
        ProcessResult validationResult = new ProcessResult();

        if (addressDTO.getAddressLine1()== null || (!Validator.isValidLength(addressDTO.getAddressLine1(), 1, 80))) {
            validationResult.getMessages().InvalidValue(EntityName.Address, null, "AddressLine1");
        }

        if (!Validator.isValidLength(addressDTO.getAddressLine2(), 0, 80)) {
            validationResult.getMessages().InvalidValue(EntityName.Address, null, "AddressLine2");
        }

        if (!Validator.isValidLength(addressDTO.getAddressLine3(), 0, 80)) {
            validationResult.getMessages().InvalidValue(EntityName.Address, null, "AddressLine3");
        }

        if (addressDTO.getCity() == null || !(Validator.isValidLength(addressDTO.getCity(), 1, 255))) {
            validationResult.getMessages().InvalidValue(EntityName.Address, null, "City");
        }

        if (addressDTO.getState() == null ||
                !(Validator.isValidLength(addressDTO.getState(), 1, 21))) {
            validationResult.getMessages().InvalidValue(EntityName.Address, null, "State");
        }

        if (addressDTO.getZipCode() == null ||
               !(Validator.isValidLength(addressDTO.getZipCode(), 1, 13))) {
            validationResult.getMessages().InvalidValue(EntityName.Address, null, "ZipCode");
        }

        if (!Validator.isValidLength(addressDTO.getZipCodeExtension(), 0, 10)) {
            validationResult.getMessages().InvalidValue(EntityName.Address, null, "ZipCodeExtension");
        }

        if (!Validator.isValidLength(addressDTO.getCountry(), 0, 255)) {
            validationResult.getMessages().InvalidValue(EntityName.Address, null, "Country");
        }

        return validationResult;
    }
}
