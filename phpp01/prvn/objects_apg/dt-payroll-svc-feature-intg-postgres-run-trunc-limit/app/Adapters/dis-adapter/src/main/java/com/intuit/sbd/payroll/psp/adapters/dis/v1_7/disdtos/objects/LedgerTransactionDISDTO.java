package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollTransaction;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTransaction;
import com.intuit.sbd.payroll.psp.domain.SettlementType;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;
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
public class LedgerTransactionDISDTO {
    @XmlElement
    private Double amount;

    @XmlElement
    private Calendar settlementDate;

    @XmlElement
    private String createdBy;

    @XmlElement
    private String transactionId;

    @XmlElement
    private String status;

    @XmlElement
    private Date createdDate;

    @XmlElement
    private String transactionType;

    @XmlElement
    private String settlementType;

    @XmlElement
    private String returnCd;

    public LedgerTransactionDISDTO() {
    }

    public LedgerTransactionDISDTO(SAPTransaction pSAPTransaction) {
        if (pSAPTransaction.getAmount() != null) {
            this.amount = new Double(pSAPTransaction.getAmount().doubleValue());
        }
        if (pSAPTransaction.getSettlementDate() != null) {
            this.settlementDate = Calendar.getInstance();
            this.settlementDate.setTimeInMillis(pSAPTransaction.getSettlementDate().getTime());
        }
        this.createdBy = pSAPTransaction.getCreatedBy();
        this.transactionId = pSAPTransaction.getTransactionId();
        this.status = pSAPTransaction.getStatus();
        this.createdDate = pSAPTransaction.getCreatedDate();
        this.transactionType = pSAPTransaction.getTransactionType();
        this.settlementType = pSAPTransaction.getSettlementType();
        this.returnCd = pSAPTransaction.getReturnCd();
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double pAmount) {
        amount = pAmount;
    }

    public Calendar getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(Calendar pSettlementDate) {
        settlementDate = pSettlementDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String pCreatedBy) {
        createdBy = pCreatedBy;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String pTransactionId) {
        transactionId = pTransactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String pStatus) {
        status = pStatus;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date pCreatedDate) {
        createdDate = pCreatedDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String pTransactionType) {
        transactionType = pTransactionType;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String pSettlementType) {
        settlementType = pSettlementType;
    }

    public String getReturnCd() {
        return returnCd;
    }

    public void setReturnCd(String pReturnCd) {
        returnCd = pReturnCd;
    }
}
