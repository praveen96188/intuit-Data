package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgencyRulesAgencyIDFormatDISDTO")
public class AgencyRulesAgencyIDFormatDISDTO {
    @XmlElement(name = "AgencyIDFormat")
    private String agencyIDFormat;

    @XmlElement(name = "RegularExpression")
    private String regularExpression;

    public String getAgencyIDFormat() {
        return agencyIDFormat;
    }

    public void setAgencyIDFormat(String agencyIDFormat) {
        this.agencyIDFormat = agencyIDFormat;
    }

    public String getRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
    }
}
