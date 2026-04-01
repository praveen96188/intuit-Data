package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
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
