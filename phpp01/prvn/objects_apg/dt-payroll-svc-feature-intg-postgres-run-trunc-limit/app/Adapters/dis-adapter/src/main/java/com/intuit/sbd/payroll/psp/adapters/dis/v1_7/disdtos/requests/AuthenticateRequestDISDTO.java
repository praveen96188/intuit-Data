package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;

import javax.xml.bind.annotation.*;

/**
 * $Author: jchickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/requests/AuthenticateRequestDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 11:23:11 $
 * $Author: jchickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "QueryDepositFrequencyHistoryRequestDISDTO")
@XmlType()
public class AuthenticateRequestDISDTO {
    @XmlElement(nillable = false, required = true)
    private String username;

    @XmlElement(nillable = false, required = true)
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
