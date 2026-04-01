package com.intuit.sbd.payroll.psp.adapters.ivr.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * User: dweinberg
 * Date: Jun 14, 2010
 * Time: 2:25:34 PM
 */
@XmlRootElement()
@XmlType(name = "GetServiceKeyResponse")
public class GetServiceKeyResponse {

    private List<ServiceKeyInfo> serviceKeys;

    @XmlElementWrapper(name = "ServiceKeys")
    @XmlElement(name = "ServiceKeyInfo")
    public List<ServiceKeyInfo> getServiceKeys() {
        if (serviceKeys == null) {
            serviceKeys = new ArrayList<ServiceKeyInfo>();
        }
        return serviceKeys;
    }

    public void setServiceKeys(List<ServiceKeyInfo> serviceKeys) {
        this.serviceKeys = serviceKeys;
    }
}
