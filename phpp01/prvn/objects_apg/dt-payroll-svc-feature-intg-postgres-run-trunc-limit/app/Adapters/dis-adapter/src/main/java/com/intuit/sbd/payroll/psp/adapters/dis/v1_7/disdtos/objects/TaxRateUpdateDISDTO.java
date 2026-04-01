package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/AgencyRulesLawDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class TaxRateUpdateDISDTO {
    @XmlElement(nillable = false, required = true)
    private BigDecimal rate;

    @XmlElement(nillable = false, required = true)
    private Integer lawId;

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal pRate) {
        rate = pRate;
    }

    public Integer getLawId() {
        return lawId;
    }

    public void setLawId(Integer pLawId) {
        lawId = pLawId;
    }
}
