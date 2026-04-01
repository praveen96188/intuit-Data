package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/AgencyRulesAgencyIDFormatDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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
