package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * User: rnorian
 * Date: Jan 21, 2010
 * Time: 11:23:56 PM
 */
public class AddressDTO401kValidator extends AddressDTOValidator {
    private String entitySourceId;

    // i.e. Hire Date, Birth Date, etc.
    private String instanceName;

    public AddressDTO401kValidator (String pSourceId, String pInstanceName) {
        entitySourceId = pSourceId;

        instanceName = pInstanceName;

        if (instanceName != null)
            instanceName = instanceName.trim();

        if (instanceName.length() > 0)
            instanceName = instanceName + " ";
    }

    @Override
    public ProcessResult validate(AddressDTO addressDTO) {
        ProcessResult validationResult = new ProcessResult();

        if (addressDTO.getAddressLine1() == null || addressDTO.getAddressLine1().trim().length() == 0) {
            validationResult.getMessages().RequiredAttribute(EntityName.Address, entitySourceId, instanceName + "line 1", MessageInfo.MessageLevel.INFO);
        } else if (!Validator.isValidLength(addressDTO.getAddressLine1(), 1, 80)) {
            validationResult.getMessages().InvalidLength(EntityName.Address, entitySourceId, instanceName + "line 1", 80);
        }

        if (!Validator.isValidLength(addressDTO.getAddressLine2(), 0, 80)) {
            validationResult.getMessages().InvalidLength(EntityName.Address, entitySourceId, instanceName + "line 2", 80);
        }

        if (!Validator.isValidLength(addressDTO.getAddressLine3(), 0, 80)) {
            validationResult.getMessages().InvalidLength(EntityName.Address, entitySourceId, instanceName + "line 3", 80);
        }

        if (addressDTO.getCity() == null || addressDTO.getCity().trim().length() == 0) {
            validationResult.getMessages().RequiredAttribute(EntityName.Address, entitySourceId, instanceName + "city", MessageInfo.MessageLevel.INFO);
        } else if (!Validator.isValidLength(addressDTO.getCity(), 1, 255)) {
            validationResult.getMessages().InvalidLength(EntityName.Address, entitySourceId, instanceName + "city", 255);
        }

        if (addressDTO.getState() == null || addressDTO.getState().trim().length() == 0) {
            validationResult.getMessages().RequiredAttribute(EntityName.Address, entitySourceId, instanceName + "state", MessageInfo.MessageLevel.INFO);
        } else if (!Validator.isValidLength(addressDTO.getState(), 1, 21)) {
            validationResult.getMessages().InvalidLength(EntityName.Address, null, instanceName + "state", 21);
        }

        if (addressDTO.getZipCode() == null || addressDTO.getZipCode().trim().length() == 0) {
            validationResult.getMessages().RequiredAttribute(EntityName.Address, entitySourceId, instanceName + "zip code", MessageInfo.MessageLevel.INFO);
        } else if (!Validator.isValidLength(addressDTO.getZipCode(), 1, 13)) {
            validationResult.getMessages().InvalidLength(EntityName.Address, null, instanceName + "zip code", 13);
        }

        if (!Validator.isValidLength(addressDTO.getZipCodeExtension(), 0, 10)) {
            validationResult.getMessages().InvalidLength(EntityName.Address, null, instanceName + "zip code extension", 10);
        }

        if (!Validator.isValidLength(addressDTO.getCountry(), 0, 255)) {
            validationResult.getMessages().InvalidLength(EntityName.Address, null, instanceName + "country", 255);
        }

        return validationResult;        
    }
}
