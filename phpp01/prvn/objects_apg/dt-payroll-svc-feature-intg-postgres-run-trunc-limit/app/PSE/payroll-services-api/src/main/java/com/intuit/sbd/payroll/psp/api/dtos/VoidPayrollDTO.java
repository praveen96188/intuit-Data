package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

import java.util.List;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 20, 2009
 * Time: 2:21:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class VoidPayrollDTO {

    private String sourcePayrollRunId = null;
    private List<String> paycheckIdList = null;

    private Collection<LiabilityAdjustmentDTO> liabilityAdjustments;

    public void setSourcePayrollRunId(String sourcePayrollRunId) {
        this.sourcePayrollRunId = sourcePayrollRunId;
    }

    public String getSourcePayrollRunId() {
        return sourcePayrollRunId;
    }

    public void setPaycheckIdList(List<String> ddTransactionIdList) {
        this.paycheckIdList = ddTransactionIdList;
    }
    
    public List<String> getPaycheckIdList() {
        return paycheckIdList;
    }

    public Collection<LiabilityAdjustmentDTO> getPayrollTaxes() {
        return liabilityAdjustments;
    }

    public void setPayrollTaxes(Collection<LiabilityAdjustmentDTO> liabilityAdjustments) {
        this.liabilityAdjustments = liabilityAdjustments;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (sourcePayrollRunId == null) {
            validationResult.getMessages().SourcePayrollRunIdNotSpecified(EntityName.PayrollRun, sourcePayrollRunId);
        }

        if (!Validator.isValidLength(sourcePayrollRunId, 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollRun, sourcePayrollRunId, "SourcePayrollRunId");
        }

        if (paycheckIdList != null) {
            for (String transId : paycheckIdList) {
                if ((transId == null) || !Validator.isValidLength(transId, 1, 50)) {
                    validationResult.getMessages().InvalidValue(EntityName.Paycheck, transId, "PaycheckId");
                }
            }
        }

        return validationResult;
    }
}
