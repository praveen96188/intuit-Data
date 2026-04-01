package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.PSPToSMSMigration.PSPToSMSMigrationConfig;
import com.intuit.sbd.payroll.psp.batchjobs.mtl.PSPToSMSMigrationService;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJobs;
import java.util.*;
import java.util.concurrent.CompletableFuture;

//*
// * @author schoudhary6
@ScheduledJobs(
        {@ScheduledJob(name = "PSPToSMSMigrationProcessor", resourcePath = "/normal",
                autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),})
public class PSPToSMSMigrationProcessor extends JSSBatchJob {

    public PSPToSMSMigrationProcessor(String[] pArguments) {
        super(pArguments);
    }

    private List<String> companyIds;
    private PSPToSMSMigrationConfig pspToSMSMigrationConfig;
    private boolean isRevertSMSMigratedCompaniesEnabled = false;
    private SMSMigrationStatus smsMigrationStatus;
    private static boolean isDebugLogEnabled;
    private static boolean isRiskLmtMigrationEnabled;


    public PSPToSMSMigrationProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void validateRuntimeParameters() {
        try {
            Application.beginUnitOfWork();
            String pCommandLineArg = getJobInstanceParameters().trim();
            getLogger().info("job=PSPtoSMSMigration, Action=Command Line Arguments={}", pCommandLineArg);
            String[] args = null;
            if (pCommandLineArg.trim().length() > 0) {
                args = pCommandLineArg.split(" ");
            }
            pspToSMSMigrationConfig = new PSPToSMSMigrationConfig(args);
        }
        catch (IllegalArgumentException e) {
            getLogger().error("job=PSPtoSMSMigration action=validateRuntimeParameters IllegalArgumentException while validating runtime parameters " + e);
            throw e;
        } catch (Exception e) {
            getLogger().error("job=PSPtoSMSMigration action=validateRuntimeParameters Error while validating runtime parameters");
            throw e;
        }
        finally {
            Application.rollbackUnitOfWork();
        }
    }

    /**
     * get company from command line argument or from PSP DB.
     */
    private void fetchCompanyIds() {

        if (Objects.nonNull(pspToSMSMigrationConfig.getSourceCompanyIds()) && pspToSMSMigrationConfig.getSourceCompanyIds().size() > 0) {
            companyIds = pspToSMSMigrationConfig.getSourceCompanyIds();
            isRevertSMSMigratedCompaniesEnabled = pspToSMSMigrationConfig.isRevertSMSMigratedCompanies();
            getLogger().info("job=PSPtoSMSMigration, Action=Retrieved CompanyIds Source=JSSParameter CompanyIds={}", companyIds);
        } else {
            //get company list from DB based on status that is to be migrated.
            companyIds = SMSMigration.getSMSMigrationCompanyIds(pspToSMSMigrationConfig.getCompanyCount(), pspToSMSMigrationConfig.getSmsMigrationStatus());
            getLogger().info("job=PSPtoSMSMigration, Action=Retrieved CompanyIds Source=Query CompanyIds={}", companyIds);
        }
    }

    @Override
    protected void execute() throws Exception {
        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.PSPToSMSMigrationProcessor));
        String logPrefix = "job=PSPtoSMSMigration, Action=ExecuteBatch, Status={}";
        getLogger().info(logPrefix, "Start");
        fetchCompanyIds();
        smsMigrationStatus = pspToSMSMigrationConfig.getSmsMigrationStatus();
        isDebugLogEnabled = pspToSMSMigrationConfig.isDebugLogEnabled();
        isRiskLmtMigrationEnabled = pspToSMSMigrationConfig.isRiskLmtMigrationEnabled();
        executeStep(PSPToSMSMigration.class);
        getLogger().info(logPrefix, "Complete");
    }

    public static class PSPToSMSMigration extends JSSBatchJobStep<PSPToSMSMigrationProcessor> {
        private List<String> companyIds;
        private List<CompletableFuture<SMSMigrationStatus>> completableFutures = new ArrayList<>();

        @Override
        protected void execute() throws Exception {
            String logPrefix="job=PSPtoSMSMigration, Action=ExecuteStep, ";
            PSPToSMSMigrationService pspToSMSMigrationService = PayrollApplicationBeanFactory.getBean(PSPToSMSMigrationService.class);
            PSPToSMSMigrationProcessor batchJobProcessor = getBatchJobProcessor();
            boolean isRevertSMSMigratedCompaniesEnabled = batchJobProcessor.isRevertSMSMigratedCompaniesEnabled;

            if (Objects.isNull(batchJobProcessor.companyIds)) {
                getLogger().info("{}Status=NoCompanyFound", logPrefix);
                return;
            } else {
                companyIds = batchJobProcessor.companyIds;
            }

            getLogger().info("{}Status=Start, CompanyCount={}, SMSMigrationStatus={}, isRevertSMSMigratedCompaniesEnabled={}, isDebugEnabled={}, isRiskLmtMigrationEnabled={}", logPrefix, companyIds.size(),
                    batchJobProcessor.smsMigrationStatus, batchJobProcessor.isRevertSMSMigratedCompaniesEnabled,
                    batchJobProcessor.isDebugLogEnabled, batchJobProcessor.isRiskLmtMigrationEnabled);

            for (String psId : companyIds) {
                getLogger().info("{}psid={}",logPrefix, psId);
                try {
                    CompletableFuture<SMSMigrationStatus> resp;
                    if (isRevertSMSMigratedCompaniesEnabled) {
                        resp = pspToSMSMigrationService.asyncRevertSMSMigratedCompany(psId);
                    } else if (batchJobProcessor.smsMigrationStatus == SMSMigrationStatus.MigrationReverted) {
                        resp = pspToSMSMigrationService.asyncEnableSMSRevertedCompany(psId);
                    } else {
                        resp = pspToSMSMigrationService.asyncMigrateCompany(psId, isDebugLogEnabled, batchJobProcessor.isRiskLmtMigrationEnabled);
                    }
                    completableFutures.add(resp);
                } catch (Exception e) {
                    getLogger().error("{}Status=Error, psid={}, errType={}, errMsg={}", logPrefix, psId, e.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
            for (CompletableFuture completableFuturesitem : completableFutures) {
                getLogger().info("{}Status=Complete, SMSMigrationStatus={}", logPrefix, completableFuturesitem.get());
            }
        }
    }
}
