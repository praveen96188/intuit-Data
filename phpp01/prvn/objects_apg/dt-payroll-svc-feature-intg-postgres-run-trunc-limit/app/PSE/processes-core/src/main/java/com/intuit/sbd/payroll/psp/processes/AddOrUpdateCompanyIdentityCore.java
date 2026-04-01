package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

public class AddOrUpdateCompanyIdentityCore extends Process {

    private SpcfLogger spcfLogger = Application.getLogger(AddOrUpdateCompanyIdentityCore.class);

    private Company domainCompany;
    private String workflow;

    public AddOrUpdateCompanyIdentityCore(Company pCompany, String workflow) {
        this.domainCompany = pCompany;
        this.workflow = workflow;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Check if company exists
        if (domainCompany == null) {
            validationResult.getMessages().InvalidValue(EntityName.Company, "Company", "Company");
            return validationResult;
        }

        if(StringUtils.isEmpty(workflow)) {
            validationResult.getMessages().InvalidValue(EntityName.Company, "IdentityWorkflow",
                    workflow);
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult<>();

        enableIdentity();

        switch (workflow) {
            case "ENABLE_DIRECT_DEPOSIT":
                enableDirectDeposit();
                break;
            //for VMP OII BIT
            case ConstantValues.OII_FLAG_ENABLE_VMP_BIT:
                enableVMP();
                break;
            default:
                break;
        }
        return processResult;
    }

    private void enableIdentity() {
        if(!domainCompany.isOIIEnabled()) {
            domainCompany.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
            spcfLogger.info(String.format("Enabled OII Flag for sourceCompanyId=%s as part of workflow=%s", domainCompany.getSourceSystemCompanyId(), workflow));
        }
    }

    private void enableDirectDeposit() {
        domainCompany.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
        domainCompany.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
    }

    //Setting OII Flag for bit ENABLE_VMP
    private void enableVMP() {
        domainCompany.setWorkflowState(Workflows.ENABLE_VMP, WorkflowState.ENABLED);
    }
}
