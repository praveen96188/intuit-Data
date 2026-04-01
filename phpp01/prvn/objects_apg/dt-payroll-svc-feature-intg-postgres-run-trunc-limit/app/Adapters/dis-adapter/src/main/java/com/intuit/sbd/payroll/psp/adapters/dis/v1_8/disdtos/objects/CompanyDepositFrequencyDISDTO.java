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
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompanyDepositFrequencyDISDTO",propOrder = {"depositFrequency"
        ,"effectiveDate","modifiedDate","modifierId"})
public class CompanyDepositFrequencyDISDTO {

    @XmlElement(name = "DepositFrequency")
    private String depositFrequency;

    @XmlElement(name = "EffectiveDate")
    private Calendar effectiveDate;

    @XmlElement(name = "ModifiedDate")
    private Calendar modifiedDate;

    public String getDepositFrequency() {
        return depositFrequency;
    }

    @XmlElement(name = "ModifierId")
    private String modifierId;

    public void setDepositFrequency(String depositFrequency) {
        this.depositFrequency = depositFrequency;
    }

    public Calendar getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Calendar effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Calendar getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Calendar modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifierId() {
        return modifierId;
    }

    public void setModifierId(String modifierId) {
        this.modifierId = modifierId;
    }
}

