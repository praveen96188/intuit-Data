package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPActionEvent;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollTransaction;
import com.intuit.sbd.payroll.psp.domain.SettlementType;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/FinancialTransactionDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType()
public class FinancialTransactionDISDTO {
//    private Date createdDate;
    @XmlElement
    private Date txnDate;

    @XmlElement
    private Double amount;

    @XmlElement
    private TransactionTypeCode txnType;

    @XmlElement
    private TransactionStateCode status;

    @XmlElement
    private SettlementType settlementType;

    @XmlElement
    private String sourcePayRunId;

    @XmlElement
    private String id;
//    private ArrayList<SAPActionEvent> actionCollection;

    @XmlElement
    private Boolean isCredit;

    @XmlElement
    private String returnCd;

    @XmlElement
    private String description;

    @XmlElement
    private String refundTransactionId;

    public FinancialTransactionDISDTO() {
    }

    public FinancialTransactionDISDTO(SAPPayrollTransaction pSapPayrollTransaction,String pRefundTransactionId) {
        txnDate = pSapPayrollTransaction.getTxnDate();
        amount = pSapPayrollTransaction.getAmount();
        txnType = pSapPayrollTransaction.getTxnType();
        status = pSapPayrollTransaction.getStatus();
        settlementType = pSapPayrollTransaction.getSettlementType();
        sourcePayRunId = pSapPayrollTransaction.getSourcePayRunId();
        id = pSapPayrollTransaction.getId();
        isCredit = pSapPayrollTransaction.isCredit();
        returnCd = pSapPayrollTransaction.getReturnCd();
        description = pSapPayrollTransaction.getDescription();
        refundTransactionId = pRefundTransactionId;
    }

    public Date getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(Date pTxnDate) {
        txnDate = pTxnDate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double pAmount) {
        amount = pAmount;
    }

    public TransactionTypeCode getTxnType() {
        return txnType;
    }

    public void setTxnType(TransactionTypeCode pTxnType) {
        txnType = pTxnType;
    }

    public TransactionStateCode getStatus() {
        return status;
    }

    public void setStatus(TransactionStateCode pStatus) {
        status = pStatus;
    }

    public SettlementType getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(SettlementType pSettlementType) {
        settlementType = pSettlementType;
    }

    public String getSourcePayRunId() {
        return sourcePayRunId;
    }

    public void setSourcePayRunId(String pSourcePayRunId) {
        sourcePayRunId = pSourcePayRunId;
    }

    public Boolean getCredit() {
        return isCredit;
    }

    public void setCredit(Boolean pCredit) {
        isCredit = pCredit;
    }

    public String getReturnCd() {
        return returnCd;
    }

    public void setReturnCd(String pReturnCd) {
        returnCd = pReturnCd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public String getRefundTransactionId() {
        return refundTransactionId;
    }

    public void setRefundTransactionId(String pRefundTransactionId) {
        refundTransactionId = pRefundTransactionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }
}
