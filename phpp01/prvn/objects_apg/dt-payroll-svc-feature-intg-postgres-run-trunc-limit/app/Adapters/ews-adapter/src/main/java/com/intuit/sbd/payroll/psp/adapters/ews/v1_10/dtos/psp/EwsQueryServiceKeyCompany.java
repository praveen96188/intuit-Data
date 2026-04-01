package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "legalName",
    "serviceKeys"})
public class EwsQueryServiceKeyCompany {
    @XmlElement(name = "LegalName", required = false)
    protected String legalName;

    @XmlElement(name = "ServiceKeys", required = false)
    protected ArrayList<EwsQueryServiceKeyItem> serviceKeys;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public ArrayList<EwsQueryServiceKeyItem> getServiceKeys() {
        if (serviceKeys == null) {
            serviceKeys = new ArrayList<EwsQueryServiceKeyItem>();
        }
        return serviceKeys;
    }
}
