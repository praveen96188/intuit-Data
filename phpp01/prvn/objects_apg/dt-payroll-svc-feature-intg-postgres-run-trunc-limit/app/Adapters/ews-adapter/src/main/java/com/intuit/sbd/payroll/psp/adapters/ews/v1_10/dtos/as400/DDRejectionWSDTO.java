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
import java.math.BigDecimal;

/**
    @author Jeff Jones
 */

@XmlType(name = "DDRejectionType", propOrder = {"checkDate", "fullName", "firstName", "middleName", "lastName",
        "checkAmt", "bankAccountNumber", "routingNumber", "description"})
public class DDRejectionWSDTO implements Cloneable {

    private String checkDate;
    private String fullName;
    private String firstName;
    private String middleName;
    private String lastName;
    private BigDecimal checkAmt;
    private String bankAccountNumber;
    private String routingNumber;
    private String description;

    public DDRejectionWSDTO clone() throws CloneNotSupportedException {
        return (DDRejectionWSDTO) super.clone();
    }

    public DDRejectionWSDTO() {
        this.fullName = null;
        this.firstName = null;
        this.middleName = null;
        this.lastName = null;
    }

    @XmlAttribute(name = "CheckDate", required = true)
    public String getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(String checkDate) {
        this.checkDate = checkDate;
    }

    @XmlAttribute(name = "FullName")
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @XmlAttribute(name = "FirstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @XmlAttribute(name = "MiddleName")
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @XmlAttribute(name = "LastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @XmlAttribute(name = "CheckAmt", required = true)
    public BigDecimal getCheckAmt() {
        return checkAmt;
    }

    public void setCheckAmt(BigDecimal checkAmt) {
        this.checkAmt = checkAmt.setScale(2);
    }

    @XmlAttribute(name = "BankAccountNumber", required = true)
    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    @XmlAttribute(name = "RoutingNumber", required = true)
    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    @XmlAttribute(name = "Description", required = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
