package com.intuit.sbd.payroll.psp.jss.processors;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.accountservice.AccountServiceSyncCore;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJobs;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author vpandey
 * 10-Nov-2020
 */

@ScheduledJobs(
        {@ScheduledJob(name = "AccountServiceSyncExceptionProcessor", resourcePath = "/normal",
                autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),})
public class AccountServiceSyncExceptionProcessor extends JSSBatchJob {

    private static final int SMS_FAILURE_MAX_RETRY_COUNT = 50;

    private static PSPRequestContextManager pspRequestContextManager;


    public AccountServiceSyncExceptionProcessor(String[] pArguments) {
        super(pArguments);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public AccountServiceSyncExceptionProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }


    @Override
    protected void execute() throws Exception {

        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BatchJob));

        StopWatch timer = StopWatch.startTimer();
        executeStep(AccountServiceSyncExceptionProcessor.RetrySyncForFailedCompanies.class);

        getLogger().info("Completed AccountServiceSyncExceptionProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());


    }


    public static class RetrySyncForFailedCompanies extends JSSBatchJobStep<AccountServiceSyncExceptionProcessor> {


        @Override
        protected void execute() throws Exception {
            Map<String, Boolean> syncAttemptResultMap = new HashMap<>();

            Map<String, String> params = this.getBatchJobProcessor().getParameters();
            String batchJobName = this.getBatchJobProcessor().getBatchJobType().name();
            getLogger().info("batch job name is " + batchJobName);
            getLogger().info("AccountServiceSyncExceptionProcessor RetrySyncForFailedCompanies started  at " + new Date(System.currentTimeMillis()));

            try {
                Application.beginUnitOfWork();

                DomainEntitySet<SMSSyncFailure> rows = SMSSyncFailure.getAllPendingRecords();


                if (rows == null || rows.size() == 0) {
                    getLogger().info("No companies available in sync-failed list : terminating retry step");
                    return;

                }

                rows.stream()
                        .filter(row -> row.getSyncDirection().equals(SMSSyncDirection.ASToPSP))
                        .forEach(row -> {
                            String psid = (row).getSourceCompanyId();

                            Long realmId = row.getCompanyRealmId();
                            String realmIdStr = realmId.toString();
                            getLogger().info("The realm id is " + realmIdStr + " and psid is " + psid);
                            try {
                                retryAccountServiceSync(row, syncAttemptResultMap);

                            } catch (Exception e) {
                                getLogger().error("Error while retrying  sync for realmId " + realmId + " " + e.getMessage());
                            }
                        });

                Application.commitUnitOfWork();
            } finally {
                Application.rollbackUnitOfWork();
            }

        }


        private void retryAccountServiceSync(SMSSyncFailure smsSyncFailure, Map syncAttemptResultMap) {


            String realmId = String.valueOf(smsSyncFailure.getCompanyRealmId());
            Company company = Company.findActiveCompanyByRealmId(realmId);
            pspRequestContextManager.setRequestContextCompany(company);
            try {
                getLogger().info("Attempting sync for " + realmId);
                AccountServiceSyncCore accountServiceSyncCore = new AccountServiceSyncCore(realmId);
                ProcessResult processResult = accountServiceSyncCore.execute();

                if (processResult != null && processResult.isSuccess()) {
                    syncAttemptResultMap.put(realmId, true);

                    CompanyEvent.createSMSSyncEvent(realmId, company, EventTypeCode.SMSToPSPSyncSuccess, Optional.empty());
                    getLogger().info("Sync attempt successful for " + realmId + " , deleting the row ");
                    deleteEntry(smsSyncFailure);
                } else {

                    getLogger().info("Sync attempt failed for " + realmId + " because of " + processResult.getMessages());
                    Message lastMsg = processResult.getMessages().get(processResult.getMessages().size() - 1);
                    syncAttemptResultMap.put(realmId, false);
                    updateSMSSyncFailedAttributes(smsSyncFailure, lastMsg);
                    CompanyEvent.createSMSSyncEvent(realmId, company, EventTypeCode.SMSToPSPSyncFailure, Optional.of(lastMsg.getMessage()));
                }
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }

        private void updateSMSSyncFailedAttributes(BaseSMSSyncFailure smsSyncFailure, Message lastMessage) {

            getLogger().info("Updating  the retry timestamp " + smsSyncFailure.getCompanyRealmId());
            smsSyncFailure.setLastRetryTimeStamp(new SpcfCalendarImpl().toLocal());
            smsSyncFailure.setCount(smsSyncFailure.getCount() + 1);
            smsSyncFailure.setFailureReason(lastMessage.getMessage());

            if (smsSyncFailure.getCount() > SMS_FAILURE_MAX_RETRY_COUNT || isNonRemediable(lastMessage)) {
                smsSyncFailure.setStatus(SMSSyncJobStatus.NeverRetry);
            }
            Application.save(smsSyncFailure);
        }

        private boolean isNonRemediable(Message lastMessage) {
            if (lastMessage.getMessageCode().equals("10120")) {
                return true;
            }
            return false;
        }

        private void deleteEntry(BaseSMSSyncFailure smsSyncFailure) {
            getLogger().info("Deleting the row " + smsSyncFailure.getCompanyRealmId());
            Application.delete(smsSyncFailure);
        }


    }

}
