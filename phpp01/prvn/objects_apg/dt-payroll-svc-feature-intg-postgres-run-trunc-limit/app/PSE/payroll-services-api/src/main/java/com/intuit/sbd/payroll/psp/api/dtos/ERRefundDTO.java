package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jan 3, 2008
 * Time: 4:10:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ERRefundDTO {
    private String financialTxId;
    private SpcfMoney financialTxAmt;
    private DateDTO txDate;
    private SettlementTypeDTO settlementType;
    private boolean supressRefundEmail;

    public ERRefundDTO() {
        super();
    }

    public ERRefundDTO(String financialTxId, SpcfMoney financialTxAmt, DateDTO txDate, SettlementTypeDTO settlementType) {
        super();
        this.financialTxId = financialTxId;
        this.financialTxAmt = financialTxAmt;
        this.txDate = txDate;
        this.settlementType = settlementType;
        this.supressRefundEmail = false;
    }

    public ERRefundDTO(String financialTxId,
                       SpcfMoney financialTxAmt,
                       DateDTO txDate,
                       SettlementTypeDTO settlementType,
                       boolean pSupressRefundEmail) {
        super();
        this.financialTxId = financialTxId;
        this.financialTxAmt = financialTxAmt;
        this.txDate = txDate;
        this.settlementType = settlementType;
        this.supressRefundEmail = pSupressRefundEmail;
       }

    public SettlementTypeDTO getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(SettlementTypeDTO settlementType) {
        this.settlementType = settlementType;
    }

    public String getFinancialTxId() {
        return financialTxId;
    }

    public void setFinancialTxId(String financialTxId) {
        this.financialTxId = financialTxId;
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

    public boolean getSupressRefundEmail() {
        return supressRefundEmail;
    }

    public void setSupressRefundEmail(boolean pSupressRefundEmail) {
        this.supressRefundEmail = pSupressRefundEmail;
    }

    public ProcessResult validateAmount() {
        ProcessResult validationResult = new ProcessResult();

        if (financialTxAmt == null) {
            validationResult.getMessages()
                    .AmountPositiveForNonACHTransactions(EntityName.FinancialTransaction, financialTxId);

            return validationResult;
        }

        if(financialTxAmt.compareTo(new SpcfMoney("0.00")) <=0){
            validationResult.getMessages().AmountPositiveForNonACHTransactions(EntityName.FinancialTransaction, financialTxId);
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

        if (txDate == null) {
            result.getMessages()
                    .SettlementDateNotSpecified(EntityName.Date,
                    this.getSettlementType().toString());
            return result;
        }

        if (DateDTO.convertToSpcfCalendar(txDate).before(pPastDate)) {
            result.getMessages().SettlementDateTooFarInPast(EntityName.Date,
                    DateDTO.convertToSpcfCalendar(txDate).toString(),
                    DateDTO.convertToSpcfCalendar(txDate).toString(),
                    this.getSettlementType().toString());


        } else if (DateDTO.convertToSpcfCalendar(txDate).after(pCurrentDate)) {
            result.getMessages().SettlementDateTooFarInFuture(EntityName.Date,
                    DateDTO.convertToSpcfCalendar(txDate).toString(),
                    DateDTO.convertToSpcfCalendar(txDate).toString(),
                    this.getSettlementType().toString());
        }

        return result;
    }
}
