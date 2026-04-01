package com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QBBankAccount", propOrder = {
    "accountId",
    "name"
})
public class QBBankAccount {
    @XmlElement(name = "AccountId")
    protected String accountId;

    @XmlElement(name = "Name")
    protected String name;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
