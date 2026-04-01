package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlType(name = "PaymentTransaction")
public class PaymentTransaction {
    
    private List<QBBillPaymentSplit> billPaymentSplits;
    private Date depositDate;
    private QBPayee payee;
    private String transactionId;
    private String memo;
    private TransactionTypeEnum TransactionType;
    private String sessionId;

    @XmlElementWrapper(name = "QBBillPaymentSplits", required = true)
    @XmlElement(name = "QBBillPaymentSplit", required = true)
    public List<QBBillPaymentSplit> getBillPaymentSplits() {
        if (billPaymentSplits == null) {
            billPaymentSplits = new ArrayList<QBBillPaymentSplit>();
        }
        return this.billPaymentSplits;
    }

    @XmlElement(name = "DepositDate", required = true, nillable = false)
    @XmlSchemaType(name = "dateTime")
    public Date getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(Date value) {
        this.depositDate = value;
    }

    @XmlElement(name = "Payee", required = true)
    public QBPayee getPayee() {
        return payee;
    }

    public void setPayee(QBPayee value) {
        this.payee = value;
    }

    @XmlElement(name = "TransactionId", required = true, nillable = false)
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String value) {
        this.transactionId = value;
    }

    @XmlElement(name = "Memo")
    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    @XmlElement(name = "SessionId")
    public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@XmlElement(name = "TransactionType")
    public TransactionTypeEnum getTransactionType() {
        return TransactionType;
    }

    public void setTransactionType(TransactionTypeEnum transactionType) {
        TransactionType = transactionType;
    }
}
