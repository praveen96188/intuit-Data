package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.salestax.SalesTaxExceptionProcess;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/***
 * 
 * @author kmuthurangam
 *
 */
public class SalesTaxExceptionProcessor extends BatchJobProcessor {
	
    private SpcfCalendar mOffloadDate = null;
	
	public SalesTaxExceptionProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId,
			String pJobInstanceParameters) {
		super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
	}

    public SpcfCalendar getOffloadDate() {
        return mOffloadDate;
    }

    protected void validateRuntimeParameters() {
        mOffloadDate =  PSPDate.getPSPTime();
        String commandLine = getJobInstanceParameters().trim();
        String[] args = commandLine.split(" ");
        if (args.length > 0) {
            for (String arg : args) {
                // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
                if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                    SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);

                    mOffloadDate = SpcfCalendar.createInstance(clDate.getYear(),
                                                                 clDate.getMonth(),
                                                                 clDate.getDay(),
                                                                 0, 0,0,0,SpcfTimeZone.getLocalTimeZone());
                }
            }
        }
    }
    
    protected void validateStepRuntimeParameters(String stepName) {
        if (ReCalculateSalesTax.class.getSimpleName().equals(stepName)) {
            validateRuntimeParameters();
        } else {
            StringBuffer err = new StringBuffer();

            err.append("The specified job step \"").
                append(stepName).
                append("\" does not exist in batch processor ").
                append(this.getClass().getSimpleName()).
                append(". The valid steps that can be executed are {").
                append(ReCalculateSalesTax.class.getSimpleName()).append(" [offload-date] } ");

            throw new RuntimeException(err.toString());
        }
    }

	@Override
	protected void execute() {
		logger.info("Starting SalesTaxExceptionProcessor batch job for the Offload date: "+getOffloadDate().format("yyyyMM01"));
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ReCalculateSalesTax());

        logger.info("Completed SalesTaxExceptionProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

	}
	
	public class ReCalculateSalesTax extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.SalesTaxExceptionsBatchJob);

                // this step needs the date when the vertex sales-tax tables were updated...
                // that happens on the 1st of each month, so...
                // whatever offload-date we've been given, we take the first of that month               
                String vertexUpdateDate = getOffloadDate().format("yyyyMM01");

                PayrollServices.beginUnitOfWork();
                try {
                    SalesTaxExceptionProcess step = new SalesTaxExceptionProcess();
                    step.process(vertexUpdateDate);

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ReCalculateSalesTax ", t);
            }
        }
    }

}
