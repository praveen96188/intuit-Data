package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.ers.EntitlementActivation;
import com.intuit.sbd.payroll.psp.batchjobs.ers.EntitlementDisable;
import com.intuit.sbd.payroll.psp.batchjobs.ers.EntitlementUnitDeactivation;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 17, 2010
 * Time: 1:26:06 PM
 */
public class EntitlementProcessor extends BatchJobProcessor {

    public EntitlementProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    public void execute() {
        logger.info("Starting EntitlementProcessor batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new EntitlementUnitDeactivationStep());
        executeStep(new EntitlementActivationStep());
        executeStep(new EntitlementDisableStep());

        logger.info("Completed EntitlementProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class EntitlementActivationStep extends BatchJobProcessorStep {
       public void execute() {
           try {
               EntitlementActivation entitlementActivation = new EntitlementActivation();
               entitlementActivation.execute();
           } catch (Throwable t) {
               logger.error("Error in step EntitlementActivationStep", t);
           } finally {
               PayrollServices.rollbackUnitOfWork();
           }
       }
    }

    public class EntitlementUnitDeactivationStep extends BatchJobProcessorStep {
       public void execute() {
           try {
               EntitlementUnitDeactivation entitlementUnitDeactivation = new EntitlementUnitDeactivation();
               entitlementUnitDeactivation.execute();
           } catch (Throwable t) {
               logger.error("Error in step EntitlementUnitDeactivationStep", t);
           } finally {
               PayrollServices.rollbackUnitOfWork();
           }
       }
    }

    public class EntitlementDisableStep extends BatchJobProcessorStep {
       public void execute() {
           try {
               EntitlementDisable entitlementDisable = new EntitlementDisable();
               entitlementDisable.execute();
           } catch (Throwable t) {
               logger.error("Error in step EntitlementDisableStep", t);
           } finally {
               PayrollServices.rollbackUnitOfWork();
           }
       }
    }
}
