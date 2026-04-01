package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.mtl.MTLOnHoldConfig;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ScheduledJob(name = "MTLCompanyToOnHoldProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class MTLCompanyToOnHoldProcessor extends JSSBatchJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(MTLCompanyToOnHoldProcessor.class);

    List<String> psIDsToPutOnHold = null;
    int chunkSize = 20;

    private static PSPRequestContextManager pspRequestContextManager;

    public MTLCompanyToOnHoldProcessor(String[] pArguments) {
        super(pArguments);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }
    public MTLCompanyToOnHoldProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }


    @Override
    protected void execute() throws Exception {
        LOGGER.info("Starting MTLCompanyToOnHoldProcessor batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(GetPSIDs.class);
        executeStep(ProcessPSIDs.class);

        LOGGER.info("Completed MTLCompanyToOnHoldProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class GetPSIDs extends JSSBatchJobStep<MTLCompanyToOnHoldProcessor> {
        @Override
        protected void execute() throws Exception {
            String pCommandLineArg = getBatchJobProcessor().getJobInstanceParameters().trim();
            LOGGER.info("MTLCompanyToOnHoldProcessor GetPSIDs - CommandLine Args " + pCommandLineArg);
            String[] args = null;

            if (pCommandLineArg.trim().length() > 0) {
                args = pCommandLineArg.split(" ");
            }

            MTLOnHoldConfig mtlConfig = new MTLOnHoldConfig(args);
            getBatchJobProcessor().psIDsToPutOnHold = new ArrayList<String>();
            getBatchJobProcessor().psIDsToPutOnHold = mtlConfig.getSourceCompanyPSIDs();
            getBatchJobProcessor().chunkSize = mtlConfig.getChunkSize();
            LOGGER.info("MTLCompanyToOnHoldProcessor GetPSIDs - PSIDs to be put on hold : " + getBatchJobProcessor().psIDsToPutOnHold.size());
        }
    }


    public static class ProcessPSIDs extends JSSBatchJobStep<MTLCompanyToOnHoldProcessor> {
        @Override
        protected void execute() throws Exception {
            LOGGER.info("MTLCompanyToOnHoldProcessor in ProcessPSIDs : Number of PSIDs to be put on hold : " + getBatchJobProcessor().psIDsToPutOnHold.size());
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.MTLCompanyToOnHoldProcessor));

            for (String psid : getBatchJobProcessor().psIDsToPutOnHold) {
                try {
                    Application.beginUnitOfWork();
                    LOGGER.info("MTLCompanyToOnHoldProcessor ProcessPSIDs Starting the OnHold for PSID : " + psid);
                    Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
                    pspRequestContextManager.setRequestContextCompany(company);

                    if (company != null) {
                        ProcessResult<Company> processResult = PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), psid, ServiceSubStatusCode.MTLHold);
                        LOGGER.info("MTLCompanyToOnHoldProcessor ProcessPSIDs={} SUCCESS={}", psid, processResult.isSuccess());
                    } else {
                        LOGGER.error("MTLCompanyToOnHoldProcessor ProcessPSIDs : Company Not Found PSID : " + psid);
                    }
                    LOGGER.info("MTLCompanyToOnHoldProcessor Commit Unit of Work");
                    Application.commitUnitOfWork();
                } finally {
                    pspRequestContextManager.clearRequestContextCompany();
                    Application.rollbackUnitOfWork();
                }
            }
        }
    }
}