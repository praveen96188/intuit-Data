package com.intuit.sbd.payroll.psp.gateways.iam;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IamUser {
    private String emailAddress;
    private String loginName;

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getLoginName() {
        return this.loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
