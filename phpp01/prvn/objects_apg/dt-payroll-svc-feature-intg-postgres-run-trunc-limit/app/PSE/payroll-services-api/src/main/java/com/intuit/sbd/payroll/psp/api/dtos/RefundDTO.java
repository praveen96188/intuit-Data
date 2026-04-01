package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;


/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Dec 11, 2007
 * Time: 3:51:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class RefundDTO {
    private String sourcePayrollRunId;
    private SpcfMoney financialTxAmt;
    private DateDTO txDate;
    private SettlementTypeDTO settlementType;
    private boolean refundTaxOnly;

    public SettlementTypeDTO getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(SettlementTypeDTO settlementType) {
        this.settlementType = settlementType;
    }

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

    public DateDTO getTxDate() {
        return txDate;
    }

    public void setTxDate(DateDTO txDate) {
        this.txDate = txDate;
    }

    public boolean isRefundTaxOnly() {
        return refundTaxOnly;
    }

    public void setRefundTaxOnly(boolean refundTaxOnly) {
        this.refundTaxOnly = refundTaxOnly;
    }

    public ProcessResult validateDDRefundDTO() {
        ProcessResult validationResult = new ProcessResult();

        if (financialTxAmt == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.DDTransaction, sourcePayrollRunId, "FinancialTransactionAmount");

            return validationResult;
        }

        if(financialTxAmt.compareTo(new SpcfMoney("0.00")) <=0){
            validationResult.getMessages().AmountNotPositive(EntityName.DDTransaction,financialTxAmt.toString());
        }

        return validationResult;
    }

    /**
     * Private method that validates that a calendar date is between 45 days in
     * the past and the current date and throws an exception if either are out
     * of range.
     *
     * @param pCurrentDate SpcfCalendar
     * @param pPastDate SpcfCalendar
     * @return result ProcessResult
     */
    public ProcessResult validateDate(SpcfCalendar pCurrentDate, SpcfCalendar pPastDate) {
        ProcessResult result = new ProcessResult();

        if (DateDTO.convertToSpcfCalendar(txDate).before(pPastDate)) {
            result.getMessages().SettlementDateTooFarInPast(EntityName.Date,
                    DateDTO.convertToSpcfCalendar(txDate).toString(),
                    DateDTO.convertToSpcfCalendar(txDate).toString(),
                    settlementType.toString());

        } else if (DateDTO.convertToSpcfCalendar(txDate).after(pCurrentDate)) {
            result.getMessages().SettlementDateTooFarInFuture(EntityName.Date,
                    DateDTO.convertToSpcfCalendar(txDate).toString(),
                    DateDTO.convertToSpcfCalendar(txDate).toString(),
                    settlementType.toString());
        }

        return result;
    }
}
