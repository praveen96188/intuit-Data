package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Company DIS DTO that will be returned by the WS
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class CompanyServiceUpdatedDISDTO {

    @XmlElement
    private String ein;

    @XmlElement
    private String psid;

    @XmlElement
    private Calendar dateCreated;

    public String getEin() {
        return ein;
    }

    public void setEin(String pEin) {
        ein = pEin;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String pPsid) {
        psid = pPsid;
    }

    public Calendar getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Calendar pDateCreated) {
        dateCreated = pDateCreated;
    }
}
