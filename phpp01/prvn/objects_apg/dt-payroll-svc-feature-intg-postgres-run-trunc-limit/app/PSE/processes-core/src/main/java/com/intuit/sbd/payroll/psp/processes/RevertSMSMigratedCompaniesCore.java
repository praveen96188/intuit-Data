package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

@Slf4j
public class RevertSMSMigratedCompaniesCore extends Process implements IProcess {
    public Company company;
    private String sourceCompanyId;
    private String tid;
    private SourceSystemCode sourceSystemCd;

    public RevertSMSMigratedCompaniesCore(String sourceCompanyId, SourceSystemCode sourceSystemCd, String tid) {
        this.sourceCompanyId = sourceCompanyId;
        this.sourceSystemCd = sourceSystemCd;
        this.tid = tid;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        String logPrefix = "job=PSPtoSMSMigration, Action=RevertSMSMigratedCompaniesCoreValidation, Status={}, psid={}, tid={}";

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
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
        String logPrefix = "job=PSPtoSMSMigration, Action=RevertSMSMigratedCompaniesCoreExecute, Status={}, psid={}, tid={}{}";
        try {
            log.info(logPrefix, "Start", sourceCompanyId, tid, StringUtils.EMPTY);
            company.setWorkflowState(Workflows.OII, WorkflowState.DISABLED);
            company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.DISABLED);
            SMSMigration.setSMSMigrationStatus(sourceCompanyId, company, SMSMigrationStatus.MigrationReverted);
            pr.setResult(SMSMigrationStatus.MigrationReverted);

            CompanyEvent.createSMSMigrateRevertedEvent(company);

            log.info(logPrefix, "Complete", sourceCompanyId, tid, StringUtils.EMPTY);
        } catch (Exception e) {
            pr.setResult(SMSMigrationStatus.MigrationError);
            log.error(logPrefix, "Error", sourceCompanyId, tid, ", errType=" + e.getClass().getSimpleName() + ", errMsg=" + e.getMessage(), e);
        }
        return pr;
    }

}
