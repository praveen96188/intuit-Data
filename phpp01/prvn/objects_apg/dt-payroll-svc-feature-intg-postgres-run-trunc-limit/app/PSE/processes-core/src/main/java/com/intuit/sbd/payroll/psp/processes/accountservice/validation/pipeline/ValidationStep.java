package com.intuit.sbd.payroll.psp.processes.accountservice.validation.pipeline;

public interface ValidationStep<OUTPUT> {

    OUTPUT process();
}
