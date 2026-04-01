package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

import java.util.List;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Dec 7, 2007
 * Time: 6:45:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionCancelEEDTO {
    private String sourcePayrollRunId = null;
    private List<String> sourcePaycheckIdList = null;
    private String transmissionId = null;
    private String requestId = null;
    private boolean agentCancel;

    private Collection<LiabilityAdjustmentDTO> liabilityAdjustments;

    public String getSourcePayrollRunId() {
        return sourcePayrollRunId;
    }

    public void setSourcePayrollRunId(String pSourcePayrollRunId) {
        this.sourcePayrollRunId = pSourcePayrollRunId;
    }

    public String getTransmissionId() {
        return transmissionId;
    }

    public void setTransmissionId(String pTransmissionId) {
        this.transmissionId = pTransmissionId;
    }

    public Collection<LiabilityAdjustmentDTO> getPayrollTaxes() {
        return liabilityAdjustments;
    }

    public void setPayrollTaxes(Collection<LiabilityAdjustmentDTO> liabilityAdjustments) {
        this.liabilityAdjustments = liabilityAdjustments;
    }

    /**
	 * Obtains the request ID
	 *
	 * @return Request ID
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * Sets the request ID
	 *
	 * @param pRequestId Request ID to set
	 */
	public void setRequestId(String pRequestId) {
		this.requestId = pRequestId;
	}

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if ((sourcePayrollRunId == null) || !Validator.isValidLength(sourcePayrollRunId, 1, 50)) {
            validationResult.getMessages().SourcePayrollRunIdNotSpecified(EntityName.PayrollRun, sourcePayrollRunId);
        }

        if (sourcePaycheckIdList != null) {
            for (String sourcePayCheckId : sourcePaycheckIdList) {
                if ((sourcePayCheckId == null) || !Validator.isValidLength(sourcePayCheckId, 1, 50)) {
                    validationResult.getMessages().InvalidArgument(EntityName.PayCheck, sourcePayCheckId, sourcePayCheckId);
                }
            }
        }
        return validationResult;
    }

    public List<String> getSourcePaycheckIdList() {
        return sourcePaycheckIdList;
    }

    public void setSourcePaycheckIdList(List<String> pSourcePaycheckIdList) {
        this.sourcePaycheckIdList = pSourcePaycheckIdList;
    }

    public boolean isAgentCancel() {
        return agentCancel;
    }

    public void setAgentCancel(boolean agentCancel) {
        this.agentCancel = agentCancel;
    }
}
