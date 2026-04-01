package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@XmlType(name = "FeeTransaction")
public class FeeTransaction {

    private BigDecimal feeAmount;
    private BigDecimal taxAmount;
    private FeeTypeEnum feeType;
    private int numberOfTransactions;
    private String transactionId;
    private Date settlementDate;
    private Boolean hasOffloaded;
    private List<String> associatedTransactionIds;

    @XmlElement(name = "FeeAmount", required = true)
    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal value) {
        this.feeAmount = value;
    }

    @XmlElement(name = "TaxAmount")
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal value) {
        this.taxAmount = value;
    }

    @XmlElement(name = "FeeType", required = true)
    public FeeTypeEnum getFeeType() {
        return feeType;
    }

    public void setFeeType(FeeTypeEnum value) {
        this.feeType = value;
    }

    @XmlElement(name = "NumberOfTransactions")
    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(int value) {
        this.numberOfTransactions = value;
    }

    @XmlElement(name = "TransactionId", required = true)
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String value) {
        this.transactionId = value;
    }

    @XmlElement(name = "SettlementDate")
    @XmlSchemaType(name = "dateTime")
    public Date getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(Date value) {
        this.settlementDate = value;
    }

    @XmlElement(name = "HasOffloaded")
    public Boolean getHasOffloaded() {
        return hasOffloaded;
    }

    public void setHasOffloaded(Boolean pOffloaded) {
        hasOffloaded = pOffloaded;
    }

    @XmlElementWrapper(name = "AssociatedTransactionIds")
    @XmlElement(name = "TransactionId")
    public List<String> getAssociatedTransactionIds() {
        return associatedTransactionIds;
    }

    public void setAssociatedTransactionIds(List<String> pAssociatedTransactionIds) {
        associatedTransactionIds = pAssociatedTransactionIds;
    }
}
