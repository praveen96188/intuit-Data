/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
    @author Jeff Jones
 */
@XmlType(name = "GetPayrollStatusType", propOrder = {"userID", "roleId"})
public class GetPayrollStatusWSDTO implements Cloneable {

    private String userID;
    private UserRoleEnum roleId;

    public GetPayrollStatusWSDTO clone() throws CloneNotSupportedException {
        return (GetPayrollStatusWSDTO) super.clone();
    }

    @XmlAttribute(name = "UserID", required = true)
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @XmlAttribute(name = "RoleID", required = false)
    public UserRoleEnum getRoleId() {
        return roleId;
    }

    public void setRoleId(UserRoleEnum roleId) {
        this.roleId = roleId;
    }

    public void validateUserID() throws Exception {
        if (this.userID == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("UserID", "GetPayrollStatus"));
        }

        if (!Validation.validateValue(this.userID, false, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("UserID", "GetPayrollStatus"));
        }
    }

}
