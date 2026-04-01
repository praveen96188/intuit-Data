package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests;

import javax.xml.bind.annotation.*;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "QueryDepositFrequencyHistoryRequestDISDTO")
@XmlType()
public class AuthenticateRequestDISDTO {
    @XmlElement(nillable = false, required = false)
    private String username;

    @XmlElement(nillable = false, required = false)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String pUsername) {
        username = pUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pPassword) {
        password = pPassword;
    }

}
