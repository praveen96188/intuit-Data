package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheckStateCode;
import com.intuit.sbd.payroll.psp.gateways.wc.gateway.WorkersCompGatewayImpl;
import com.intuit.sbd.payroll.psp.gateways.wc.manager.WorkersCompGatewayManager;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDtoCompanyFileInfo;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.common.WorkersCompTransporter;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.service.WorkersCompServiceNext;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.service.WorkersCompServiceSplitLimit;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: michaelp696
 *
 * Migrated To JSS by: nloharuka
 * Date: 5/04/17
 * PSP-13042
 */

@ScheduledJob(name = "WorkersCompProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class WorkersCompProcessor extends JSSBatchJob {
    private WorkersCompGatewayImpl workersCompGateway;
    private WorkersCompTransporter wcTransporter;
    private WorkersCompServiceNext objWorkersCompServiceNext;
    private WorkersCompServiceSplitLimit objWorkersCompServiceSplitLimit;
    private List<PayrollDtoCompanyFileInfo> payrollInfoList;
    public WorkersCompProcessor(String[] pArguments) {
        super(pArguments);
        this.workersCompGateway = new WorkersCompGatewayImpl();
        objWorkersCompServiceNext= PayrollApplicationBeanFactory.getBean(WorkersCompServiceNext.class);
        objWorkersCompServiceSplitLimit= PayrollApplicationBeanFactory.getBean(WorkersCompServiceSplitLimit.class);
        wcTransporter= PayrollApplicationBeanFactory.getBean(WorkersCompTransporter.class);
    }
    public WorkersCompProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        this.workersCompGateway = new WorkersCompGatewayImpl();
        objWorkersCompServiceNext= PayrollApplicationBeanFactory.getBean(WorkersCompServiceNext.class);
        objWorkersCompServiceSplitLimit= PayrollApplicationBeanFactory.getBean(WorkersCompServiceSplitLimit.class);
        wcTransporter= PayrollApplicationBeanFactory.getBean(WorkersCompTransporter.class);

    }

    @Override
    protected void execute() {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.WorkersCompBatchJob);
        getLogger().info("Starting WorkersCompProcessor batch job");
        StopWatch timer = StopWatch.startTimer();
        if(isWCCSStopSuppport()) {
            executeStep(PullSubscriptionChanges.class);
        }
        PayrollServices.beginUnitOfWork();
        SpcfCalendar lastRunDate= SystemParameter.findCalendarValueByFormat("yyyy/MM/dd",SystemParameter.Code.WORKERS_COMP);
        PayrollServices.rollbackUnitOfWork();
        if(isWCSubscriptionCancelled()) {
            if (lastRunDate.getDay() == PSPDate.getPSPTime().getDay()
                    && lastRunDate.getYear() == PSPDate.getPSPTime().getYear()
                    && lastRunDate.getMonth() == PSPDate.getPSPTime().getMonth()) {
                getLogger().info("IF:Last run date for workers comp cancelled subs" + lastRunDate);
            } else {
                getLogger().info("ELSE:Last run date for workers comp cancelled subs" + lastRunDate);
                executeStep(CancelWCSubsHavingEndDateYesterday.class);
            }
        }

        //Upload data for next(Ap intego) customers
        if(isWCSFTPEnabled()) {
            executeStep(CreateWCFiles.class);
            executeStep(UploadFiles.class);
            executeStep(ArchiveFiles.class);
        }
        if(isWCSFTPSplitLimitEnabled())
        {
            executeStep(MarkDeleteEditedPaycheckToSent.class);
            executeStep(CreateWCSplitLimitFiles.class);
            executeStep(UploadFilesToSplitLimit.class);
            executeStep(ArchiveSplitLimitFiles.class);
        }
        //Upload data for splitLimit(trupay)/Next and splitLimit customers
        if(isWCAPIEnabled()) {
            executeStep(PushPayrollData.class);
        }
        if(isWCCSStopSuppport()) {
            executeStep(PushCompanyChanges.class);
        }
        getLogger().info("Completed WorkersCompProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }
    private boolean isWCSFTPEnabled() {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.PSP_WC_SFTP_ENABLE, false);
    }

    private boolean isWCAPIEnabled() {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.PSP_WC_API_ENABLE, true);
    }
    private boolean isWCSFTPSplitLimitEnabled() {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.PSP_WC_SFTP_SPLITLIMIT_ENABLE, false);
    }

    private boolean isWCCSStopSuppport() {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.PSP_WCCS_STOP_SUPPORT, false);
    }

    private boolean isWCSubscriptionCancelled() {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.PSP_WC_SUBSCRIPTION_CANCELLED, false);
    }

    public static class PullSubscriptionChanges extends JSSBatchJobStep<WorkersCompProcessor> {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.WorkersCompBatchJob);
            getLogger().info("Starting PullSubscriptionChanges step");
            StopWatch timer = StopWatch.startTimer();
            getBatchJobProcessor().workersCompGateway.getSubscriptionChangesFromWC();
            getLogger().info("Completed PullSubscriptionChanges step. Elapsed time: " + timer.stop().getElapsedTimeString());
        }
    }

    public static class PushPayrollData extends JSSBatchJobStep<WorkersCompProcessor> {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.WorkersCompBatchJob);
            getLogger().info("Starting PushPayrollData step");
            StopWatch timer = StopWatch.startTimer();
            getBatchJobProcessor().workersCompGateway.pushPayrollDataToWC();
            getLogger().info("Completed PushPayrollData step. Elapsed time: " + timer.stop().getElapsedTimeString());
        }
    }

    public static class MarkDeleteEditedPaycheckToSent extends JSSBatchJobStep<WorkersCompProcessor>{
        public void execute(){
            if(!(UploadFilesToSplitLimit.isWCSFTPSPLITLIMITPRDEnabled() &&
                    !WorkersCompGatewayManager.isWCForAllCustomers() &&
                    !WorkersCompGatewayManager.isWCForSplitLimitCustomers()) ) return;
            try {
                getBatchJobProcessor().getLogger().info("MarkDeleteEditedPaycheckToSent Started");
                PayrollServices.beginUnitOfWork();
                SpcfCalendar currentDate = PSPDate.getPSPTime();
                currentDate.addDays(-1);
                List<WorkersCompPaycheck> workersCompPaychecks = Application.executeNamedQuery("findWCPaychecksForSplitHavingStatusDeleteOrEdit",
                            new String[]{"currentDate"},
                            new Object[]{currentDate});
                if (workersCompPaychecks != null && (workersCompPaychecks.size() > 0)) {
                    getBatchJobProcessor().getLogger().info("Total number of workersComp paycheck: "+workersCompPaychecks.size()+" CurrentDate: "+currentDate);
                    WorkersCompPaycheck.markAsSent(workersCompPaychecks);
                    }
                PayrollServices.commitUnitOfWork();
                } finally {
                PayrollServices.rollbackUnitOfWork();
                }
            }
    }

    public static class CreateWCFiles extends JSSBatchJobStep<WorkersCompProcessor> {
            public void execute() {
                try {
                    PayrollServices.beginUnitOfWork();
                    getBatchJobProcessor().payrollInfoList = getBatchJobProcessor().objWorkersCompServiceNext.createPayrollDataforWC();
                }finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }

    public static class CreateWCSplitLimitFiles extends JSSBatchJobStep<WorkersCompProcessor> {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();
                SpcfCalendar currentDate = PSPDate.getPSPTime();
                currentDate.addDays(-1);
                getBatchJobProcessor().payrollInfoList = getBatchJobProcessor().objWorkersCompServiceSplitLimit.createPayrollDataforWC(currentDate, Arrays.asList(WorkersCompPaycheckStateCode.PendingNew));
            }finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class UploadFiles extends JSSBatchJobStep<WorkersCompProcessor> {
             public void execute() {
                 try {
                     PayrollServices.beginUnitOfWork();
                     if (isWCSFTPPRDEnabled()) {
                         getLogger().info("Inside PRD step");
                         getBatchJobProcessor().wcTransporter.uploadSftpFile(BatchUtils.getConfigString("wc_server_host_name"), BatchUtils.getConfigString("wc_server_prd_username"),
                                     BatchUtils.getConfigString("wc_server_prd_password"), BatchUtils.getConfigString("wc_server_send_dir"), BatchUtils.getConfigString("wc_server_destination_dir"), getBatchJobProcessor().payrollInfoList);
                     }
                     if (isWCSFTPPPDEnabled()) {
                         getLogger().info("Inside PPD step");
                         getBatchJobProcessor().wcTransporter.uploadSftpFile(BatchUtils.getConfigString("wc_server_host_name"), BatchUtils.getConfigString("wc_server_ppd_username"), BatchUtils.getConfigString("wc_server_ppd_password"),
                                 BatchUtils.getConfigString("wc_server_send_dir"), BatchUtils.getConfigString("wc_server_destination_dir"), getBatchJobProcessor().payrollInfoList);
                     }
                     PayrollServices.commitUnitOfWork();
                 }finally {
                     PayrollServices.rollbackUnitOfWork();
                     }
            }
             private boolean isWCSFTPPPDEnabled() {
                 return FeatureFlags.get().booleanValue(FeatureFlags.Key.WC_APINTEGO_PPD_ACTIVE, false);
             }

             private boolean isWCSFTPPRDEnabled() {
                 return FeatureFlags.get().booleanValue(FeatureFlags.Key.WC_APINTEGO_PRD_ACTIVE, true);
             }
    }
    public static class UploadFilesToSplitLimit extends JSSBatchJobStep<WorkersCompProcessor>{
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork();
                if (isWCSFTPSPLITLIMITPRDEnabled()) {
                    getLogger().info("Inside SplitLimit PRD step");
                    //name change to upload file
                getBatchJobProcessor().wcTransporter.uploadSftpFile(BatchUtils.getConfigString("wc_server_insurepay_prd_hostname"),
                        BatchUtils.getConfigString("wc_server_insurepay_username"), BatchUtils.getConfigString("wc_server_insurepay_prd_password"),
                        BatchUtils.getConfigString("wc_server_insurepay_send_dir"),
                        BatchUtils.getConfigString("wc_server_insurepay_destination_dir"), getBatchJobProcessor().payrollInfoList,
                        !WorkersCompGatewayManager.isWCForAllCustomers() && !WorkersCompGatewayManager.isWCForSplitLimitCustomers());
            }
                if(isWCSFTPSPLITLIMITPPDEnabled()){
                    getLogger().info("Inside SplitLimit PPD step");
                    getBatchJobProcessor().wcTransporter.uploadSftpFile(BatchUtils.getConfigString("wc_server_insurepay_ppd_hostname"),
                            BatchUtils.getConfigString("wc_server_insurepay_username"), BatchUtils.getConfigString("wc_server_insurepay_ppd_password"),
                            BatchUtils.getConfigString("wc_server_insurepay_send_dir"),
                            BatchUtils.getConfigString("wc_server_insurepay_destination_dir"), getBatchJobProcessor().payrollInfoList,
                            false);
                }
                PayrollServices.commitUnitOfWork();
            }finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
        public static boolean isWCSFTPSPLITLIMITPPDEnabled() {
            return FeatureFlags.get().booleanValue(FeatureFlags.Key.WC_INSURPAY_PPD_ACTIVE, false);
        }

        public static boolean isWCSFTPSPLITLIMITPRDEnabled() {
            return FeatureFlags.get().booleanValue(FeatureFlags.Key.WC_INSURPAY_PRD_ACTIVE, true);
        }
    }
    public static class ArchiveFiles extends JSSBatchJobStep<WorkersCompProcessor>  {
        private String mArchiveDir = BatchUtils.getConfigString("wc_server_archive_dir");
            public void execute() throws S3ConnectionException, S3UploadException {
                    for (PayrollDtoCompanyFileInfo dto : getBatchJobProcessor().payrollInfoList) {
                        String fileName = String.format("%s%s.pgp", BatchUtils.getConfigString("wc_server_send_dir"), dto.getFileName());
                        S3UploadUtils.archive(BatchJobType.WorkersCompProcessor.name(), mArchiveDir,fileName );
                    }
            }
        }

    public static class ArchiveSplitLimitFiles extends JSSBatchJobStep<WorkersCompProcessor>{
            private String mArchiveDir = BatchUtils.getConfigString("wc_server_insurepay_archive_dir");
            public void execute() throws S3ConnectionException, S3UploadException {

                for (PayrollDtoCompanyFileInfo dto : getBatchJobProcessor().payrollInfoList) {
                    String fileName = String.format("%s%s.pgp", BatchUtils.getConfigString("wc_server_insurepay_send_dir"), dto.getFileName());
                    S3UploadUtils.archive(BatchJobType.WorkersCompProcessor.name(), mArchiveDir,fileName );
                 }
            }
        }

    public static class CancelWCSubsHavingEndDateYesterday extends JSSBatchJobStep<WorkersCompProcessor>{
        @Override
        public void execute(){
            try {
                getLogger().info("Inside SelectAndUpdateCompaniesEndDateToday step");
                PayrollServices.beginUnitOfWork();
                List<String> sourceCompanyIds = getCompaniesEndDateYesterday();
                getLogger().info("Output of getCompaniesEndDateToday() is:"+sourceCompanyIds.toString());
                updateCompaniesSubsCancelled(sourceCompanyIds);
                SystemParameter.update(SystemParameter.Code.WORKERS_COMP, PSPDate.getPSPTime().toString());
                PayrollServices.commitUnitOfWork();
            }catch (Exception e){
                getLogger().info("Select and update to company and company service  fail", e);
            }
            finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
        private void updateCompaniesSubsCancelled( List<String> sourceCompanyIds)
        {
            for (String comp:sourceCompanyIds) {
                Company company = Company.findCompany(comp, com.intuit.sbd.payroll.psp.domain.SourceSystemCode.QBDT);
                CompanyService companyService = company.getCompanyService(ServiceCode.WorkersComp);
                companyService.setStatusCd(com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode.Cancelled);
                Application.save(companyService);
            }
        }
        private List<String>  getCompaniesEndDateYesterday() {
            getLogger().info("SelectCompaniesEndDateToday() for next customers");
            SpcfCalendar currentDate = PSPDate.getPSPTime();
            List<SubsTypeCode> subsCode=new ArrayList<SubsTypeCode>();
            subsCode.add(com.intuit.sbd.payroll.psp.domain.SubsTypeCode.Next);
            if(isCancelledSubsEnableForSplitLimit())
            {
                subsCode.add(com.intuit.sbd.payroll.psp.domain.SubsTypeCode.SplitLimit);
            }
            List<String> sourceCompanyIds =
                    Application.executeNamedQuery("findAllCompanieshavingTodaySubsEndDates", new String[]{"currentDate","subsCode"},
                            new Object[]{currentDate,subsCode});
            return sourceCompanyIds;
        }
        private boolean isCancelledSubsEnableForSplitLimit() {
            return FeatureFlags.get().booleanValue(FeatureFlags.Key.PSP_WC_CANCSUBS_SPLITLIMIT, false);
        }
    }

        public static class PushCompanyChanges extends JSSBatchJobStep<WorkersCompProcessor> {
            @Override
            public void execute() {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.WorkersCompBatchJob);
                getLogger().info("Starting PushCompanyChanges step");
                StopWatch timer = StopWatch.startTimer();

                getBatchJobProcessor().workersCompGateway.pushCompanyChanges();
                getLogger().info("Completed PushCompanyChanges step. Elapsed time: " + timer.stop().getElapsedTimeString());
            }
        }
}
