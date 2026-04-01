package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suganyas315 on 5/12/15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "industryType"
})
public class EwsQueryIndustryListResponse extends EwsResponse implements Cloneable {
    @XmlElement(name = "IndustryTypes", required = false)
    protected ArrayList<String> industryType;

    public ArrayList<String> getIndustryType() {
        if (industryType == null) {
            industryType = new ArrayList<String>();
        }
        return industryType;
    }
}
