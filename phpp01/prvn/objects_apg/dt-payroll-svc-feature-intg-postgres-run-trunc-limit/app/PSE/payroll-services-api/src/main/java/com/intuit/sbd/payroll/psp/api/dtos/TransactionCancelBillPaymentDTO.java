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
public class TransactionCancelBillPaymentDTO {
    private String sourcePayrollRunId = null;
    private List<String> sourceBillPaymentIdList = null;
    private String transmissionId = null;
    private String requestId = null;



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

        if (sourceBillPaymentIdList != null) {
            for (String sourcePayCheckId : sourceBillPaymentIdList) {
                if ((sourcePayCheckId == null) || !Validator.isValidLength(sourcePayCheckId, 1, 50)) {
                    validationResult.getMessages().InvalidArgument(EntityName.PayCheck, sourcePayCheckId, sourcePayCheckId);
                }
            }
        }
        return validationResult;
    }

    public List<String> getSourceBillPaymentList() {
        return sourceBillPaymentIdList;
    }

    public void setSourceBillPaymentList(List<String> pSourceBillPaymentIdList) {
        this.sourceBillPaymentIdList = pSourceBillPaymentIdList;
    }
}