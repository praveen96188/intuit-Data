package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.OfferingServiceChargeType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Dec 14, 2007
 * Time: 9:40:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class ERFeeAddDTO {

    SourceSystemCode sourceSystemCd;
    String sourceCompanyId;
    String sourcePayrollRunId;
    SettlementTypeDTO settlementTypeCode;
    Date txDate;
    SpcfMoney amount;
    OfferingServiceChargeType feeTypeCode;
    String memo;

    public ERFeeAddDTO() {
        super();
    }

    public ERFeeAddDTO(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pSourcePayrollRunId,
			SettlementTypeDTO pSettlementTypeCode, Date pTxDate, SpcfMoney pAmount,
            OfferingServiceChargeType pFeeTypeCode, String pMemo) {
        this.sourceSystemCd = pSourceSystemCd;
        this.sourceCompanyId = pSourceCompanyId;
        this.sourcePayrollRunId = pSourcePayrollRunId;
        this.settlementTypeCode = pSettlementTypeCode;
        this.txDate = pTxDate;
        this.amount = pAmount;
        this.feeTypeCode = pFeeTypeCode;
        this.memo = pMemo;
    }

    public SourceSystemCode getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(SourceSystemCode sourceSystemCd) {
        this.sourceSystemCd = sourceSystemCd;
    }

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }

    public String getSourcePayrollRunId() {
        return sourcePayrollRunId;
    }

    public void setSourcePayrollRunId(String sourcePayrollRunId) {
        this.sourcePayrollRunId = sourcePayrollRunId;
    }

    public SettlementTypeDTO getSettlementTypeCode() {
        return settlementTypeCode;
    }

    public void setSettlementTypeCode(SettlementTypeDTO settlementTypeCode) {
        this.settlementTypeCode = settlementTypeCode;
    }

    public Date getTxDate() {
        return txDate;
    }

    public void setTxDate(Date txDate) {
        this.txDate = txDate;
    }

    public SpcfMoney getAmount() {
        return amount;
    }

    public void setAmount(SpcfMoney amount) {
        this.amount = amount;
    }

    public OfferingServiceChargeType getFeeTypeCode() {
        return feeTypeCode;
    }

    public void setFeeTypeCode(OfferingServiceChargeType feeTypeCode) {
        this.feeTypeCode = feeTypeCode;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public ProcessResult validateFeeAddDTO() {
        ProcessResult validationResult = new ProcessResult();
       
        if (feeTypeCode == null) {
            validationResult.getMessages().InvalidValue(EntityName.Fee, null, "FeeType");
        }

        return validationResult;
    }
}
