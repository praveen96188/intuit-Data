package com.intuit.sbd.payroll.psp.processes.accountservice.validation;

import com.intuit.sbg.psp.validationservices.types.v1.SMSValidationResult;

public class AccountServiceValidationResultTransformer {

    public static String getFormattedValidationResultForSAP(SMSValidationResult smsValidationResult){

        StringBuffer errorStringBuffer = new StringBuffer("Error: ");
        StringBuffer warningStringBuffer = new StringBuffer("Warning: ");
        if(!smsValidationResult.isValidationPassed()){

            smsValidationResult.getErrors().forEach(errorDetail -> errorStringBuffer
                    .append(errorDetail.getEntity())
                    .append("  ")
                    .append(errorDetail.getMessage())
                    .append("\n"));

            smsValidationResult.getWarnings().forEach(errorDetail -> warningStringBuffer
                    .append(errorDetail.getEntity())
                    .append("  ")
                    .append(errorDetail.getMessage())
                    .append("\n"));
        }

        return errorStringBuffer.append("\n").append(warningStringBuffer).toString();
    }


    public static String getFormattedResultForPayrollPlugin(SMSValidationResult smsValidationResult){
        //TODO use propertyaccessor to create a payments account object response format
        return "hey";
    }
}
