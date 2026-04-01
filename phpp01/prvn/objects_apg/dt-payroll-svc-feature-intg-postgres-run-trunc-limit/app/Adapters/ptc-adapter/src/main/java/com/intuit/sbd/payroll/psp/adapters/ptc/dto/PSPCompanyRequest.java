package com.intuit.sbd.payroll.psp.adapters.ptc.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: dweinberg
 * Date: 8/14/12
 * Time: 10:49 AM
 */
@XmlRootElement()
@XmlType(name = "PSPCompanyRequest")
public class PSPCompanyRequest {

    private String sourceSystemCode;

    private String psid;

    @XmlElement(required = true)
    public String getSourceSystemCode() {
        return sourceSystemCode;
    }

    public void setSourceSystemCode(String pSourceSystemCode) {
        sourceSystemCode = pSourceSystemCode;
    }

    @XmlElement(required = true)
    public String getPsid() {
        return psid;
    }

    public void setPsid(String pPsid) {
        psid = pPsid;
    }
}
