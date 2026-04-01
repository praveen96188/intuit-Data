package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "cloudService"
})
public class EwsServices extends EwsBaseServices implements Cloneable {

    @XmlElement(name = "Cloud", required = false)
    protected EwsBaseService cloudService;

    public EwsServices clone() throws CloneNotSupportedException {
        EwsServices clone = (EwsServices) super.clone();

        if (cloudService != null) {
            clone.setCloudService(cloudService.clone());
        }

        return clone;
    }

    public EwsBaseService getCloudService() {
        return cloudService;
    }

    public void setCloudService(EwsBaseService cloudService) {
        this.cloudService = cloudService;
    }

    public void validate() throws Exception {
        super.validate();
        if (cloudService != null)  {
          cloudService.validate();  
        }

    }
}
