package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jul 18, 2008
 * Time: 8:48:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class RedebitImpoundDTO {

    private String originalFinancialTxId;
    private SpcfMoney amount;
    private DateDTO initiationDate;
    private SettlementTypeDTO settlementType;

    public RedebitImpoundDTO() {
        super();
    }

    public RedebitImpoundDTO(String pOriginalFinancialTxId, SpcfMoney pAmount, DateDTO pInitiationDate) {
        originalFinancialTxId = pOriginalFinancialTxId;
        amount = pAmount;
        initiationDate = pInitiationDate;
        settlementType = null;
    }

    public RedebitImpoundDTO(String pOriginalFinancialTxId, SpcfMoney pAmount, DateDTO pInitiationDate,
                             SettlementTypeDTO pSettlementType) {
        originalFinancialTxId = pOriginalFinancialTxId;
        amount = pAmount;
        initiationDate = pInitiationDate;
        settlementType = pSettlementType;
    }

    public String getOriginalFinancialTxId() {
        return originalFinancialTxId;
    }

    public void setOriginalFinancialTxId(String originalFinancialTxId) {
        this.originalFinancialTxId = originalFinancialTxId;
    }

    public SpcfMoney getAmount() {
        return amount;
    }

    public void setAmount(SpcfMoney amount) {
        this.amount = amount;
    }

    public DateDTO getInitiationDate() {
        return initiationDate;
    }

    public void setInitiationDate(DateDTO initiationDate) {
        this.initiationDate = initiationDate;
    }

    public SettlementTypeDTO getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(SettlementTypeDTO pSettlementType) {
        settlementType = pSettlementType;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (originalFinancialTxId == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.DDTransaction, null, "OriginalFinancialTransactionId");
        }

        if (amount == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.DDTransaction, null, "RedebitAmount");
        } else {

            if(amount.compareTo(new SpcfMoney("0.00")) <0){
                validationResult.getMessages().AmountNotPositive(EntityName.DDTransaction, amount.toString());
            }
        }

        if (initiationDate == null) {
            validationResult.getMessages().InvalidValue(EntityName.Date, null, "InitiationDate");
        } else {
            validationResult.merge(initiationDate.validate());
        }

        return validationResult;
    }
}
