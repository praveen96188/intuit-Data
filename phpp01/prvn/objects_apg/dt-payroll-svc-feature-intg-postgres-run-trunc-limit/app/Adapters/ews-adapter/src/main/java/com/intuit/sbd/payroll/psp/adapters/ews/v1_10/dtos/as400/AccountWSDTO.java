/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
    @author Jeff Jones
 */

@XmlType(name = "AccountType")
public class AccountWSDTO implements Cloneable {

    private AccountStatusEnum status;

    public AccountWSDTO clone() throws CloneNotSupportedException {
        return (AccountWSDTO) super.clone();
    }

    @XmlAttribute(name = "Status", required = true)
    public AccountStatusEnum getStatus() {
        return status;
    }

    public void setStatus(AccountStatusEnum status) {
        this.status = status;
    }
}
