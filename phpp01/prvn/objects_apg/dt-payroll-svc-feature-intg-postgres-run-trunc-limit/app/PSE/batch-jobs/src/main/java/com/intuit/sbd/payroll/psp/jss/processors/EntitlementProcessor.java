package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ers.EntitlementActivation;
import com.intuit.sbd.payroll.psp.batchjobs.ers.EntitlementDisable;
import com.intuit.sbd.payroll.psp.batchjobs.ers.EntitlementUnitDeactivation;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 17, 2010
 * Time: 1:26:06 PM
 */
@ScheduledJob(name = "EntitlementProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EntitlementProcessor extends JSSBatchJob {

  
    public EntitlementProcessor(String[] pArguments) {
        super(pArguments);
	}
	public EntitlementProcessor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	}
    @Override
    public void execute() {
        getLogger().info("Starting EntitlementProcessor batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(EntitlementUnitDeactivationStep.class);
        executeStep(EntitlementActivationStep.class);
        executeStep(EntitlementDisableStep.class);

        getLogger().info("Completed EntitlementProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class EntitlementActivationStep extends JSSBatchJobStep<EntitlementProcessor> {
       public void execute() {
           try {
               EntitlementActivation entitlementActivation = new EntitlementActivation();
               entitlementActivation.execute();
           } catch (Throwable t) {
        	   getLogger().error("Error in step EntitlementActivationStep", t);
           } finally {
               PayrollServices.rollbackUnitOfWork();
           }
       }
    }

    public static class EntitlementUnitDeactivationStep extends JSSBatchJobStep<EntitlementProcessor> {
       public void execute() {
           try {
               EntitlementUnitDeactivation entitlementUnitDeactivation = new EntitlementUnitDeactivation();
               entitlementUnitDeactivation.execute();
           } catch (Throwable t) {
               getLogger().error("Error in step EntitlementUnitDeactivationStep", t);
           } finally {
               PayrollServices.rollbackUnitOfWork();
           }
       }
    }

    public static class EntitlementDisableStep extends JSSBatchJobStep<EntitlementProcessor> {
       public void execute() {
           try {
               EntitlementDisable entitlementDisable = new EntitlementDisable();
               entitlementDisable.execute();
           } catch (Throwable t) {
        	   getLogger().error("Error in step EntitlementDisableStep", t);
           } finally {
               PayrollServices.rollbackUnitOfWork();
           }
       }
    }
}
