package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;

/**
 * User: praveenkumarh635
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "ein",
        "einState",
        "einCreationDate"
})

public class EwsEntitlementUnitInfo implements Cloneable {
    @XmlElement(name = "Ein", required = false)
    protected String ein;

    @XmlElement(name = "EinState", required = false)
    protected String einState;

    @XmlElement(name = "EinCreationDate", required = false)
    protected Calendar einCreationDate;

    public EwsEntitlementUnitInfo clone() throws CloneNotSupportedException {
        return (EwsEntitlementUnitInfo) super.clone();
    }

    public Calendar getEinCreationDate() {
        return einCreationDate;
    }

    public void setEinCreationDate(Calendar pEinCreationDate) {
        einCreationDate = pEinCreationDate;
    }

    public String getEinState() {
        return einState;
    }

    public void setEinState(String pEinState) {
        einState = pEinState;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String pEin) {
        ein = pEin;
    }
}
