package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/AgencyRulesLawDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgencyRulesLawDISDTO")
public class AgencyRulesLawDISDTO {
    @XmlElement(name = "LawId")
    private Integer lawId;

    @XmlElement(name = "LawAbbrev")
    private String lawAbbrev;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "TaxType")
    private String taxType;

    @XmlElement(name = "as400TaxCode")
    private String as400TaxCode;

    public Integer getLawId() {
        return lawId;
    }

    public void setLawId(Integer lawId) {
        this.lawId = lawId;
    }

    public String getLawAbbrev() {
        return lawAbbrev;
    }

    public void setLawAbbrev(String lawAbbrev) {
        this.lawAbbrev = lawAbbrev;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public String getAs400TaxCode() {
        return as400TaxCode;
    }

    public void setAs400TaxCode(String as400TaxCode) {
        this.as400TaxCode = as400TaxCode;
    }
}
