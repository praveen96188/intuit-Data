package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.iam.PspToSmsTRONGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

@Slf4j
public class EnableSMSMigrationFlagCore extends Process implements IProcess  {
    public Company company;
    private String sourceCompanyId;
    private String tid;
    private SourceSystemCode sourceSystemCd;
    private final boolean isRealmReset;

    public EnableSMSMigrationFlagCore(Company company, String tid, boolean realmReset) {
        this.isRealmReset = realmReset;
        this.company = company;
        this.tid = tid;
    }

    public EnableSMSMigrationFlagCore(Company company, String tid) {
        this(company,tid,false);
    }

    public EnableSMSMigrationFlagCore(String sourceCompanyId, SourceSystemCode sourceSystemCd, String tid) {
        this(Company.findCompany(sourceCompanyId, sourceSystemCd),tid);
        this.sourceCompanyId = sourceCompanyId;
        this.sourceSystemCd = sourceSystemCd;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        String logPrefix = "job=PSPtoSMSMigration, Action=EnableSMSMigrationFlagCoreValidation, Status={}, psid={}, tid={}";

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            log.error(logPrefix, "CompanyDoesNotExist", sourceCompanyId, tid);
            return validationResult;
        }
        return validationResult;
    }

    public ProcessResult process() {

        ProcessResult<SMSMigrationStatus> pr = new ProcessResult<SMSMigrationStatus>();
        PspToSmsTRONGrantProcessor pspToSmsTRONGrantProcessor = new PspToSmsTRONGrantProcessor(company);

        String logPrefix = "job=PSPtoSMSMigration, Action=EnableSMSMigrationFlagCoreExecute, Status={}, psid={}, tid={}{}";

        ProcessResult iusProcessResult;
        log.info(logPrefix, "Start", sourceCompanyId, tid, StringUtils.EMPTY);

        //IUS grant update
        try {
            iusProcessResult = pspToSmsTRONGrantProcessor.execute();
        } catch (Exception e) {
            log.error(logPrefix, "SMSTronGrantFailure", sourceCompanyId, tid, ", errMsg=" + e.getMessage(), e);
            pr.setResult(SMSMigrationStatus.DataCollectionComplete);
            return pr;
        }

        //DB Flag update
        try {
            if (iusProcessResult.isSuccess()) {
                log.info(logPrefix, "SMSTronGrantSuccess", sourceCompanyId, tid, StringUtils.EMPTY);
                company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
                company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
                pr.setResult(SMSMigrationStatus.MigrationComplete);
                log.info(logPrefix, "Complete", sourceCompanyId, tid, StringUtils.EMPTY);
            } else {
                log.info(logPrefix, "SMSTronGrantNonSuccess", sourceCompanyId, tid, ", errMsg=" + iusProcessResult.getMessages());
                pr.setResult(SMSMigrationStatus.MigrationError);
                return pr;
            }
        } catch (Exception e) {
            if (!isRealmReset) {
                company.setWorkflowState(Workflows.OII, WorkflowState.DISABLED);
                company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.DISABLED);
            }
            log.error(logPrefix, "Error", sourceCompanyId, tid, ", errType=" + e.getClass().getSimpleName() + ", errMsg=" + e.getMessage(), e);
            pr.setResult(SMSMigrationStatus.MigrationError);
        }
        return pr;
    }

}
