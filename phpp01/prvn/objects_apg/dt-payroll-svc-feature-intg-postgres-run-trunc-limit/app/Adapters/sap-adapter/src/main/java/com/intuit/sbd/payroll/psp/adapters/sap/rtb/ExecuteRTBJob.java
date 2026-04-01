package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created by anandp233 on 2/23/14.
 */
public class ExecuteRTBJob {
    private static final SpcfLogger logger = PayrollServices.getLogger(ExecuteRTBJob.class);
    BaseRTBJob eIRTBJob;

    public JobResult runRTBJob(RTBJobEnum pRTBJob) throws Throwable {
        return runRTBJob(pRTBJob, null);
    }

    public JobResult runRTBJob(RTBJobEnum pRTBJob, byte[] fileBinary) throws Throwable {
        if (fileBinary != null) {
            eIRTBJob = ClassLocator.getInstance(pRTBJob.getClassName(), fileBinary);
        } else {
            eIRTBJob = ClassLocator.getInstance(pRTBJob.getClassName());
        }
        return eIRTBJob.execute(pRTBJob);

    }

}
