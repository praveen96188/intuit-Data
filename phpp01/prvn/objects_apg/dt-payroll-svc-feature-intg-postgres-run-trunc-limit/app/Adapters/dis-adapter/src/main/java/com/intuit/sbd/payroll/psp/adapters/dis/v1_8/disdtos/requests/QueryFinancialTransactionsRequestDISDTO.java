package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType()
public class QueryFinancialTransactionsRequestDISDTO {
    @XmlElement(nillable = false, required = true)
    private List<String> transactionIds;

    @XmlElement(nillable = false, required = false)
    private String sourceCompanyId;

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }

    public List<String> getTransactionIds() {
        return transactionIds;
    }

    public void setTransactionIds(List<String> pTransactionIds) {
        transactionIds = pTransactionIds;
    }
}
