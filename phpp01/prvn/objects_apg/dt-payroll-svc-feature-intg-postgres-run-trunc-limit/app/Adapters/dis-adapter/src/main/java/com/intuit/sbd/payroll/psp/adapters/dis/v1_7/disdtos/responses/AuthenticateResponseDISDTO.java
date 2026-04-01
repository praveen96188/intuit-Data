package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.FinancialTransactionDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: jchickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/AuthenticateResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 11:23:11 $
 * $Author: jchickanosky $
 *
 * Response WS DTO for the query company events request
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class AuthenticateResponseDISDTO extends ResponseDISDTO {

    @XmlElement
    private String token;

    @XmlElement
    private String corpId;

    public String getToken() {
        return token;
    }

    public void setToken(String pToken) {
        token = pToken;
    }

    public void clearElements() {
        this.token = null;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String pCorpId) {
        corpId = pCorpId;
    }
}
