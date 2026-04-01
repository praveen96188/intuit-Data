/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
    @author Jeff Jones
 */
@XmlType(name = "QuickBooksType", propOrder = {"appVer", "appId"})
public class QuickBooksWSDTO implements Cloneable {
    private String appVer;
    private String appId;

    public QuickBooksWSDTO() {
        this.appId = null;
        this.appVer = null;
    }

    public QuickBooksWSDTO clone() throws CloneNotSupportedException {
        return (QuickBooksWSDTO) super.clone();
    }

    @XmlElement(name = "AppVer", nillable = false, required = false)
    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    @XmlElement(name = "AppId", nillable = false, required = false)
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void validateAppId() throws Exception {
        if (!Validation.validateValue(this.appId, true, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AppId", "QuickBooks"));
        }
    }

    public void validateAppVer() throws Exception {
        if (!Validation.validateValue(this.appVer, false, OFXAPPVERObject.appVersionRegExPatten)) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AppVer", "QuickBooks"));
        }
    }

}
