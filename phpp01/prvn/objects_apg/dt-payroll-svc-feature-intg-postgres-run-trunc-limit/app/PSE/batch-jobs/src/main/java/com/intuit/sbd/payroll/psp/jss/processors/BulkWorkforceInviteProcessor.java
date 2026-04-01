package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.Workforce.BulkNewEmployeeInviteService;
import com.intuit.sbd.payroll.psp.batchjobs.Workforce.BulkWorkforceInviteConfig;
import com.intuit.sbd.payroll.psp.batchjobs.Workforce.BulkWorkforceInviteService;
import com.intuit.sbd.payroll.psp.batchjobs.Workforce.InvitationMode;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.CompanyEventStatus;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@ScheduledJob(name="BulkWorkforceInviteProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class BulkWorkforceInviteProcessor extends JSSBatchJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkWorkforceInviteProcessor.class);

    public BulkWorkforceInviteProcessor(String[] pArguments) {
        super(pArguments);
    }

    public BulkWorkforceInviteProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    private BulkWorkforceInviteConfig bulkWorkforceInviteConfig;
    private List<SpcfUniqueId> companyIds;
    private InvitationMode invitationMode;

    public static final String DEFAULT_TEMPLATE="WF_ERInviteEmailTemplate_Default";

    @Override
    protected void validateRuntimeParameters() {
        try {
            Application.beginUnitOfWork();
            String pCommandLineArg = getJobInstanceParameters().trim();
            LOGGER.info("job=Bulk_Workforce_Invite, Action=Command Line Arguments={}", pCommandLineArg);
            if (pCommandLineArg.trim().length() > 0){

                //set JSS, System parameter for configurable inputs
                setConfigParameters(pCommandLineArg);

                //Check for limit where invites have exceeded threshold. Total Invites are updated in system parameter after every job run.
                //These are then compared to the threshold given in JSS/System/Default Param at the starting of job
                //Will exit batch job if invite count is more than threshold
                if (bulkWorkforceInviteConfig.getWorkforceInviteCovered() >= bulkWorkforceInviteConfig.getMaxWorkforceInvitePerDay()) {
                    LOGGER.info("Skipping Batch Job Run, invite limit already exceeded, TotalInvites={} Limit={}",
                            bulkWorkforceInviteConfig.getWorkforceInviteCovered(), bulkWorkforceInviteConfig.getMaxWorkforceInvitePerDay());
                    companyIds = new ArrayList<>();
                    return;
                }

                //load companyId(s)
                fetchCompanyIds();
            }else{
                //to run this job with parameters, use systemParameters WORKFORCE_INVITE_MAX_RETRY,WORKFORCE_COMPANY_EVENTS_FETCH_HOURS
                //If no commandLine arguments, then InviteToNewEmployees
                LOGGER.info("job=Bulk_Workforce_Invite, workflow=sendInvitesToNewEmployees");
                invitationMode= InvitationMode.NewInvitesOnEmployeeAdd;
            }
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private void setConfigParameters(String pCommandLineArg) {
        String[] args = null;
         {
            args = pCommandLineArg.split(" ");
        }
        bulkWorkforceInviteConfig = new BulkWorkforceInviteConfig(args);
    }

    //If parameter for companyIds is passed in JSS parameter, then it will process invites for the companyId passed
    //Else it will query the database to get all eligible companies
    private void fetchCompanyIds() {
        if (Objects.nonNull(bulkWorkforceInviteConfig.getCompanyIds()) && bulkWorkforceInviteConfig.getCompanyIds().size() > 0) {
            companyIds = bulkWorkforceInviteConfig.getCompanyIds();
            LOGGER.info("job=Bulk_Workforce_Invite, Action=Retrieved CompanyIds Source=JSSParameter CompanyIds={}", companyIds);
        } else {
            companyIds = Company.findWorkforceEligibleCompanies(bulkWorkforceInviteConfig.getMaxCompaniesPerRun(),
                    bulkWorkforceInviteConfig.getLastPayrollRunDurationCompany(),
                    bulkWorkforceInviteConfig.getPublishStatusWorkforce(), bulkWorkforceInviteConfig.getMaxRowsToFetch(),
                    bulkWorkforceInviteConfig.isDDQuery());
            LOGGER.info("job=Bulk_Workforce_Invite, Action=Retrieved CompanyIds Source=Query CompanyIds={}", companyIds);
        }
    }

    @Override
    protected void execute() throws Exception {

        StopWatch timer = StopWatch.startTimer();
        if(invitationMode!= null && invitationMode.equals(InvitationMode.NewInvitesOnEmployeeAdd)){
            LOGGER.info("job=Bulk_Workforce_Invite,workflow=sendInvitesToNewEmployees, Action=Execute_starting");
            executeStep(ProcessNewEmployeeInvites.class);
        }else {
            LOGGER.info("job=Bulk_Workforce_Invite, Action=Execute_starting");
            executeStep(ProcessBulkWorkforceInvite.class);
        }

        LOGGER.info("job=Bulk_Workforce_Invite, Action=Completed {} ElapsedTime={}", getClass().getSimpleName(), timer.stop().getElapsedMillis());
    }

    public static class ProcessNewEmployeeInvites extends JSSBatchJobStep<BulkWorkforceInviteProcessor> {
        private BulkNewEmployeeInviteService bulkNewEmployeeInviteService = PayrollApplicationBeanFactory.getBean(BulkNewEmployeeInviteService.class);

        private static final int EMPLOYEE_PARTITION_SIZE = 10;
        private DomainEntitySet<CompanyEvent> companyEvents;

        @Override
        protected void execute() throws Exception {
            try {
                Application.beginUnitOfWork();
                LOGGER.info("job=Bulk_Workforce_Invite, workflow=sendInvitesToNewEmployees");
                fetchCompanyEvents();
                LOGGER.info("job=Bulk_Workforce_Invite_NewEmployees, Action=execute_starting_step");
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BulkWorkforceInviteProcessor));

                if (CollectionUtils.isEmpty(companyEvents)) {
                    LOGGER.info("job=Bulk_Workforce_Invite_NewEmployees, Action=CompanyEvents null");
                    return;
                }
                Map<InvitationMode, List<String>> invitationModeEmailTemplateListMap = new HashMap<>();
                List<String> emailTemplates = new ArrayList<>();
                emailTemplates.add(DEFAULT_TEMPLATE);
                invitationModeEmailTemplateListMap.put(InvitationMode.NewInvitesOnEmployeeAdd, emailTemplates);
                for (CompanyEvent companyEvent : companyEvents) {
                    bulkNewEmployeeInviteService.sendInvite(EMPLOYEE_PARTITION_SIZE, invitationModeEmailTemplateListMap, companyEvent);
                }
                Application.commitUnitOfWork();
            } catch (Exception e) {
                LOGGER.error("job=Bulk_Workforce_Invite_NewEmployees exception {} ", e.getStackTrace());
            } finally {
                Application.rollbackUnitOfWork();
            }

        }

        /**
         * fetch companyevents created in last 4 hours as buffer to take care of deployments or some downtime
         */
        private void fetchCompanyEvents() {
            List<EventTypeCode> eventTypeCodes = new ArrayList<>();
            eventTypeCodes.add(EventTypeCode.UnsyncedEmployeeInvite);
            SpcfCalendar to= PSPDate.getPSPTime();
            SpcfCalendar from = PSPDate.getPSPTime();
            int hours= SystemParameter.findIntValue(SystemParameter.Code.WORKFORCE_COMPANY_EVENTS_FETCH_HOURS, 4);
            from.addHours(hours * -1);
            companyEvents = CompanyEvent.findCompanyEventsByEventTypeAndStatus(eventTypeCodes, CompanyEventStatus.Active, from,to);
            LOGGER.info("job=Bulk_Workforce_Invite, workflow=sendInvitesToNewEmployees, action=fetchCompanyEvents, eventsCount={}", companyEvents.size());
        }

    }

    public static class ProcessBulkWorkforceInvite extends JSSBatchJobStep<BulkWorkforceInviteProcessor> {

        private BulkWorkforceInviteService bulkWorkforceInviteService = PayrollApplicationBeanFactory.getBean(BulkWorkforceInviteService.class);
        private List<CompletableFuture<Integer>> completableFutures = new ArrayList<>();
        private static final int COMPANY_PARTITION_SIZE = 10;
        private static final int EMPLOYEE_PARTITION_SIZE = 10;
        private static final boolean IS_RESEND = false;

        @Override
        protected void execute() throws Exception {

            LOGGER.info("job=Bulk_Workforce_Invite, Action=execute_starting,step");
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BulkWorkforceInviteProcessor));

            if(Objects.isNull(getBatchJobProcessor().companyIds)){
                LOGGER.info("job=Bulk_Workforce_Invite, Action=CompanyIds null");
                return;
            }

            List<List<SpcfUniqueId>> partitionCompanyIdList = ListUtils.partition(getBatchJobProcessor().companyIds, COMPANY_PARTITION_SIZE);
            Map<InvitationMode,List<String>> invitationModeEmailTemplateListMap = getBatchJobProcessor().bulkWorkforceInviteConfig.getInvitationModeEmailTemplatesListMap();
            Map<String,Object> queryParams = createQueryParamsFromConfig(getBatchJobProcessor().bulkWorkforceInviteConfig);

            for (List<SpcfUniqueId> partitionCompanyIdSubList : partitionCompanyIdList) {

                try {
                    CompletableFuture<Integer> completableFuture = bulkWorkforceInviteService.asyncInviteCompanies(partitionCompanyIdSubList,
                            EMPLOYEE_PARTITION_SIZE, IS_RESEND,
                            invitationModeEmailTemplateListMap,
                            queryParams);
                    completableFutures.add(completableFuture);

                } catch (Exception e) {
                    LOGGER.error("job=Bulk_Workforce_Invite, Action=Failed Execution Thread, Error={}", e.getMessage(), e);
                }
            }
            // Get Total Invites from all threads
            Integer totalInviteCount = getWorkforceInviteCount();
            // Update System Params
            updateTotalWorkforceInvite(totalInviteCount);
        }

        private Integer getWorkforceInviteCount() {
            Integer totalInviteCount=0;

            for ( CompletableFuture<Integer> completableFuture : completableFutures) {
                Integer inviteCount = 0;
                try {
                    inviteCount = completableFuture.get();

                } catch (Exception e) {
                    LOGGER.error("job=Bulk_Workforce_Invite, Action=Failed Thread Result, Error={}", e.getMessage(), e);
                }
                totalInviteCount += inviteCount;
            }
            return totalInviteCount;
        }

        private Map<String,Object> createQueryParamsFromConfig(BulkWorkforceInviteConfig bulkWorkforceInviteConfig) {
            Map<String,Object> queryParams = new HashMap<>();

            queryParams.put("lastPaidDurationEmployee",bulkWorkforceInviteConfig.getLastPaidDurationEmployee());
            queryParams.put("settlementDateDuration",bulkWorkforceInviteConfig.getSettlementDateDuration());
            queryParams.put("isDDQuery",bulkWorkforceInviteConfig.isDDQuery());

            return queryParams;
        }

        private void updateTotalWorkforceInvite(Integer totalInviteCount) {

            //Updating the total invitation count inside System Parameter. This parameter is used to communicate with results of all job run and decide whether to run another job or not.
            //If this value in next job run is greater than allowed threshold, the batch job will not run and exit
            if(totalInviteCount != 0) {
                try{
                    Application.beginUnitOfWork();
                    int updatedCount = SystemParameter.findIntValue(SystemParameter.Code.WORKFORCE_INVITE_COVERED) + totalInviteCount;
                    SystemParameter.update(SystemParameter.Code.WORKFORCE_INVITE_COVERED, String.valueOf(updatedCount));
                    LOGGER.info("job=Bulk_Workforce_Invite, Action=Updated System Parameter EmployeesInvited={} TotalEmployeesInvited={}", totalInviteCount, updatedCount);
                    PayrollServices.commitUnitOfWork();
                } finally {
                    Application.rollbackUnitOfWork();
                }

            }
        }
    }

}