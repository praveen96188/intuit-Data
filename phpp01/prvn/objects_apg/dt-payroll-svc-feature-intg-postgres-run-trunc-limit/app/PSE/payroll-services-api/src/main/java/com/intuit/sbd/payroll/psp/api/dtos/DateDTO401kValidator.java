package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;

/**
 * User: rnorian
 * Date: Jan 21, 2010
 * Time: 9:47:20 PM
 */
public class DateDTO401kValidator extends DateDTOValidator {
    private EntityName owningEntity;
    private String entitySourceId;

    // i.e. Hire Date, Birth Date, etc.
    private String instanceName;

    private MessageInfo.MessageLevel messageLevel;

    public DateDTO401kValidator(EntityName pOwningEntity, String pSourceId, String pInstanceName) {
        this(pOwningEntity, pSourceId, pInstanceName, MessageInfo.MessageLevel.ERROR);
    }

    public DateDTO401kValidator(EntityName pOwningEntity, String pSourceId, String pInstanceName, MessageInfo.MessageLevel pMessageLevel) {

        entitySourceId = pSourceId;
        owningEntity = pOwningEntity;

        instanceName = pInstanceName;
        if (instanceName != null)
            instanceName = instanceName.trim();

        if (instanceName.length() > 0)
            instanceName = instanceName + " ";

        messageLevel = pMessageLevel;
    }

    public ProcessResult validate(DateDTO dateDTO) {
        ProcessResult validationResult = new ProcessResult(messageLevel);

        if (dateDTO.getMonth() < 0 || dateDTO.getMonth() > 11) {
            validationResult.getMessages().RangeValidationFailure(owningEntity, entitySourceId, instanceName + "month", 1, 12);
        }

        if (dateDTO.getYear() <= 0) {
            validationResult.getMessages().RangeValidationFailure(owningEntity, entitySourceId, instanceName + "year", 1);
        }

        if (dateDTO.getDay() <= 0 || dateDTO.getDay() > 31) {
            validationResult.getMessages().RangeValidationFailure(owningEntity, entitySourceId, instanceName + "day", 1, 31);
        }

        return validationResult;
    }
}
