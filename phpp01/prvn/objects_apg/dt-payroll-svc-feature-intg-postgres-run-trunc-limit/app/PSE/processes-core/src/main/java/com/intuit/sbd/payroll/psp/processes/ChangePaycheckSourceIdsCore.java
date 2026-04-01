package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

import java.util.HashSet;
import java.util.Set;

/**
 * User: dweinberg
 * Date: 5/20/11
 * Time: 10:43 AM
 * Changes paycheck IDs from payrolls submitted using different stream (DD-OFX) so as not to conflict with a new stream (Assisted OFX)
 */
public class ChangePaycheckSourceIdsCore extends Process {

    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private String sourcePayrollId;

    @SuppressWarnings({"FieldCanBeLocal"})
    private Company company;
    private PayrollRun payrollRun;

    public ChangePaycheckSourceIdsCore(SourceSystemCode sourceSystemCode, String sourceCompanyId, String sourcePayrollId) {
        this.sourceSystemCode = sourceSystemCode;
        this.sourceCompanyId = sourceCompanyId;
        this.sourcePayrollId = sourcePayrollId;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // validate the source system code
        if (sourceSystemCode == null) {
            validationResult.getMessages().SourceSystemCdNotSpecified(EntityName.SourceSystem, sourceCompanyId);
        }

        // validate the company id
        if ((sourceCompanyId == null) || !Validator.isValidLength(sourceCompanyId, 1, 50)) {
            validationResult.getMessages().CompanyIdNotSpecified(EntityName.Company, sourceCompanyId);
        }

        // retrieve the company
        company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(
                    EntityName.Company,
                    sourceCompanyId,
                    sourceSystemCode.toString(),
                    sourceCompanyId);
            return validationResult;
        }

        payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        if (payrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(
                    EntityName.PayrollRun,
                    sourcePayrollId,
                    sourcePayrollId,
                    sourceSystemCode.toString(),
                    sourceCompanyId);
            return validationResult;
        }

        if (company.isCompanyOnService(ServiceCode.ThirdParty401k)) {
            validationResult.getMessages().ServiceOperationNotAllowed(sourceSystemCode.toString(), sourceCompanyId, ServiceCode.ThirdParty401k.toString(), "Change paycheck source IDs");
        }

        //make sure not already changed
        Set<String> negativePaychecksIds = new HashSet<String>();
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            String paycheckId = paycheck.getSourcePaycheckId();

            int paycheckIdNum;
            try {
                paycheckIdNum = Integer.parseInt(paycheckId);
            } catch (NumberFormatException e) {
                validationResult.getMessages().GenericError(EntityName.PayCheck, paycheckId, "Cannot change non-numeric source ID for paycheck");
                return validationResult;
            }

            if (paycheckIdNum <= 0) {
                validationResult.getMessages().GenericError(EntityName.PayCheck, paycheckId, "Paycheck already changed");
            }

            negativePaychecksIds.add(Integer.toString(Integer.parseInt(paycheckId) * -1));
        }

        for (Paycheck supersededPaycheck : Paycheck.findPaychecks(company, negativePaychecksIds)) {
            validationResult.getMessages().GenericError(EntityName.PayCheck, supersededPaycheck.getSourcePaycheckId(), "Paycheck already changed");
        }

        if (payrollRun.getPayrollRunStatus() == PayrollStatus.Superseded) {
            validationResult.getMessages().GenericError(EntityName.PayrollRun, payrollRun.getSourcePayRunId(), "Payroll run already changed");
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        payrollRun.setPayrollRunStatus(PayrollStatus.Superseded);
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            Application.getSessionCache().removePrimaryKey(paycheck.getNaturalKey());
            paycheck.setSourcePaycheckId(Integer.toString(Integer.parseInt(paycheck.getSourcePaycheckId()) * -1));
            if(paycheck.getQbdtPaycheckInfo() != null && paycheck.getQbdtPaycheckInfo().getListId() != null) {
                paycheck.getQbdtPaycheckInfo().setListId("-" + paycheck.getQbdtPaycheckInfo().getListId());
            }
            Application.save(paycheck);
            paycheck.cache();
        }
        return processResult;
    }
}
