package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.OfferingServiceChargeType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.SpcfMoney;


/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jan 9, 2008
 * Time: 2:38:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class FeeTransferDTO {
    String sourcePayrollRunId;
    SpcfMoney financialTxAmt;
    OfferingServiceChargeType feeTypeCode;

    public String getSourcePayrollRunId() {
        return sourcePayrollRunId;
    }

    public void setSourcePayrollRunId(String sourcePayrollRunId) {
        this.sourcePayrollRunId = sourcePayrollRunId;
    }

    public SpcfMoney getFinancialTxAmt() {
        return financialTxAmt;
    }

    public void setFinancialTxAmt(SpcfMoney financialTxAmt) {
        this.financialTxAmt = financialTxAmt;
    }

    public OfferingServiceChargeType getFeeTypeCode() {
        return feeTypeCode;
    }

    public void setFeeTypeCode(OfferingServiceChargeType feeTypeCode) {
        this.feeTypeCode = feeTypeCode;
    }

    public ProcessResult validateFeeTransferDTO() {
        ProcessResult validationResult = new ProcessResult();
        String txAmt=null;

        if (financialTxAmt != null) {
            txAmt = financialTxAmt.toString();
        }

        /**
         * Verify txn amount is a positive non-zero number
         * If not throw exception �The amount must be a non-zero, positive number"
         */
        if (financialTxAmt == null || financialTxAmt.compareTo(new SpcfMoney("0.00")) <= 0) {
            validationResult.getMessages().AmountNotPositive(EntityName.DDTransaction,
                    txAmt);
            return validationResult;
        }

        if (feeTypeCode == null ||
                (feeTypeCode != OfferingServiceChargeType.DebitReturnFee && feeTypeCode != OfferingServiceChargeType.ReversalFee)) {
            String sourceId = null;
            if (null != feeTypeCode) {
                sourceId = feeTypeCode.toString();
            }
            validationResult.getMessages().InvalidValue(EntityName.Fee, sourceId, "FeeType");
        }

        return validationResult;
    }    
}
