package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import java.util.List;

/**
 * @author vdammur1
 */

public interface ICustomEmailWorkFlowProcessor<T> {
    void process(List<T> input, String fileName);
}
