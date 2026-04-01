package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;

@XmlType(name = "QBBillPaymentSplit")
public class QBBillPaymentSplit {

    private BigDecimal amount;
    private QBBankAccount bankAccount;
    private String sourceBillPaymentSplitId;
    private String ReferenceNumber;

    @XmlElement(name = "Amount", required = true, nillable = false)
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal pAmount) {
        amount = pAmount;
    }

    @XmlElement(name = "QBBankAccount", required = true)
    public QBBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(QBBankAccount pBankAccount) {
        bankAccount = pBankAccount;
    }

    @XmlElement(name = "SourceBillPaymentSplitId", required = false)
    public String getSourceBillPaymentSplitId() {
        return sourceBillPaymentSplitId;
    }

    public void setSourceBillPaymentSplitId(String pSourceBillPaymentSplitId) {
        sourceBillPaymentSplitId = pSourceBillPaymentSplitId;
    }

    @XmlElement(name = "ReferenceNumber")
    public String getReferenceNumber() {
        return ReferenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        ReferenceNumber = referenceNumber;
    }
}
