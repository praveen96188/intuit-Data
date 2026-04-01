package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.statereports.StateReportBatchProcess;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;


/**
 * Creates state recon and coupon files.<br>
 * Manually scheduling a report:<br>
 * A date can be passed in otherwise, the current PSPDATE will be used.  A report will find the last time a report should
 * have been run based on the time.  For example, to run the monthly report for January 2011, you would pass in 20110201.
 * February first is used so that the code will go back to previous monthly it should have ran which is January.<br>
 * Here is the current list of reports:<br>
 * AL-CR4WH-PAYMENT<br>
 * IA-44105-PAYMENT<br>
 * MA-M941-PAYMENT<br>
 * NM-CRS1-PAYMENT<br>
 * ALL-COUPONS<br>
 * To specify a period for a report place a comma after the report with the DepositFrequencyCode in ALL CAPS.  For example,
 * to run AL's monthly report pass in "AL-CR4WH-PAYMENT,MONTHLY".  If a period isn't passed in, all supported periods will
 * be run.<br>
 * The ALL-COUPONS report cannot be run with a specific DepositFrequencyCode or PaymentTemplate.  All supported states and
 * PaymentTemplateFrequencies are used.
 * @author jesseanderson
 */
@ScheduledJob(name = "StateReport", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class)
public class StateReportProcessor extends JSSBatchJob {
    
    public StateReportProcessor(String[] pArguments) {
		super(pArguments);
	}

	public StateReportProcessor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}


    @Override
    protected void execute() {
    	getLogger().info("Starting state report offload batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(CreateStateReportFiles.class);

        getLogger().info("Completed state reports batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class CreateStateReportFiles extends JSSBatchJobStep<StateReportProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.StateReportBatchJob);

                new StateReportBatchProcess(getBatchJobProcessor().getJobInstanceParameters().trim()).createFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CreateStateReportFiles ", t);
            }
        }
    }
}
