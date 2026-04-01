package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jan 4, 2008
 * Time: 9:38:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class BadDebtRecoverDTO {

    private String origTransactionId; // ID of existing DD/fee/tax writeoff FT

    private String sourcePayrollRunId;
    private SpcfMoney financialTxAmt;
    private DateDTO txDate;
    private SettlementTypeDTO settlementType;
    private boolean isCustomer=false; //is this bad debt recovery from the customer? (else from collection agency)

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

    public SettlementTypeDTO getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(SettlementTypeDTO settlementType) {
        this.settlementType = settlementType;
    }

    public String getOriginalTransactionId() {
        return origTransactionId;
    }

    public void setOriginalTransactionId(String pOrigTransactionId) {
        origTransactionId = pOrigTransactionId;
    }

    public boolean isCustomer() {
        return isCustomer;
    }

    public void setCustomer(boolean customer) {
        isCustomer = customer;
    }

    public ProcessResult validateBadDebtRecoverDTO() {
        ProcessResult validationResult = new ProcessResult();
        String txAmt=null;

        if (financialTxAmt != null) {
            txAmt = financialTxAmt.toString();
        }

        /**
         * Verify txn amount is a positive non-zero number
         * If not throw exception “For non-ACH transactions, the amount must
         * be a non-zero, positive number”
         */
        if (financialTxAmt == null || financialTxAmt.compareTo(new SpcfMoney("0.00")) <= 0) {
            validationResult.getMessages().AmountPositiveForNonACHTransactions(EntityName.DDTransaction, txAmt);
        }

        // make sure we have a recover type
        if (origTransactionId == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.FinancialTransaction, null, "WriteoffTransactionId");
        }

        return validationResult;
    }

    /**
     * Private method that validates that a calendar date is between 45 days in
     * the past and the current date and throws an exception if either are out
     * of range.
     *
     * @param pCurrentDate SpcfCalendar
     * @param pPastDate    SpcfCalendar
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
