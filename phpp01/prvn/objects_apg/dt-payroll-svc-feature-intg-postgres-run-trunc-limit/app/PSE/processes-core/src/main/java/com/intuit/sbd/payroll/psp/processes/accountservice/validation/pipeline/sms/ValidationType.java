package com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms;

public enum ValidationType {
    /**
     *See {@link
     *       com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps.factory.ValidationStepsFactory#getFullValidationSteps}
     */
    FULL_VALIDATION,

    /**
     * See {@link
     *      com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline.sms.validationsteps.factory.ValidationStepsFactory#getPartialValidationSteps}
     *
     */
    PARTIAL_VALIDATION,

    /**
     * TODO
     * we cant enforce that we have addressDTO and addressType
     * in all cases where the caller is calling AccountServiceValidationCore
     * So it's better to remove this from Validation Type
     */
    //ADDRESS_VALIDATION,
}
