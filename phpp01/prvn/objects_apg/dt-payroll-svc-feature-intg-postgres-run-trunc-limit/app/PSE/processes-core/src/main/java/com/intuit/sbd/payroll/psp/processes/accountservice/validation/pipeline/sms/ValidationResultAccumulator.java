package com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms;

import com.intuit.sbg.psp.validationservices.types.v1.ErrorDetail;
import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;

import java.util.ArrayList;
import java.util.List;

public class ValidationResultAccumulator {

    public  SMSValidationResult merge(SMSValidationResult result1, SMSValidationResult result2) {

        SMSValidationResult smsValidationResult = new SMSValidationResult();
        smsValidationResult.setValidationPassed(result1.isValidationPassed() && result2.isValidationPassed());

        List<ErrorDetail> smsValidationErrorList = new ArrayList<>();
        smsValidationErrorList.addAll(result1.getErrors());
        smsValidationErrorList.addAll(result2.getErrors());

        List<ErrorDetail> smsValidationWarningList = new ArrayList<>();
        smsValidationWarningList.addAll(result1.getWarnings());
        smsValidationWarningList.addAll(result2.getWarnings());

        smsValidationResult.setErrors(smsValidationErrorList);
        smsValidationResult.setWarnings(smsValidationWarningList);

        return smsValidationResult;
    }
}
