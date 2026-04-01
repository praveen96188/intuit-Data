package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: rnorian
 * Date: Jan 21, 2010
 * Time: 9:50:43 PM
 */
public class DateDTOValidator {
    public ProcessResult validate(DateDTO dateDTO) {
        ProcessResult validationResult = new ProcessResult();
        if (dateDTO.getMonth() < 0 || dateDTO.getMonth() > 11) {
            validationResult.getMessages().InvalidValue(EntityName.Date, "", "Month");
        }

        if (dateDTO.getYear() <= 0) {
            validationResult.getMessages().InvalidValue(EntityName.Date, "", "Year");
        }

        if (dateDTO.getDay() <= 0 || dateDTO.getDay() > 31) {
            validationResult.getMessages().InvalidValue(EntityName.Date, "", "Day");
        }

        return validationResult;
    }
}
