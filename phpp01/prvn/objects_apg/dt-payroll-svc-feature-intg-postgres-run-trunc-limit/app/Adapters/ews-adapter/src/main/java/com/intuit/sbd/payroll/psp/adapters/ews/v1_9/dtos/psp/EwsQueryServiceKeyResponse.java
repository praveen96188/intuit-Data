package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "companies"
})
public class EwsQueryServiceKeyResponse extends EwsResponse implements Cloneable {
    @XmlElement(name = "Companies", required = false)
    protected ArrayList<EwsQueryServiceKeyCompany> companies;

    public ArrayList<EwsQueryServiceKeyCompany> getCompanies() {
        if (companies == null) {
            companies = new ArrayList<EwsQueryServiceKeyCompany>();
        }
        return companies;
    }
}
