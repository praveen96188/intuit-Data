package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/SAPCompanyStatusDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPCompanyStatus", propOrder = {""})
public class SAPCompanyStatusDISDTO {

    @XmlElement(name = "FlaggedForFraud")
    private boolean flaggedForFraud;

    public boolean isFlaggedForFraud() {
        return flaggedForFraud;
    }

    public void setFlaggedForFraud(boolean flaggedForFraud) {
        this.flaggedForFraud = flaggedForFraud;
    }

    @XmlElement(name = "AvailableService")
    // services that can be added to a company
    private ArrayList<String> availableServices;

    public ArrayList<String> getAvailableServices() {
        return availableServices;
    }

    public void setAvailableServices(ArrayList<String> pAvailableServices) {
        availableServices = pAvailableServices;
    }

    @XmlElement(name = "SAPCompanyServiceStatus")
    private ArrayList<SAPCompanyServiceStatusDISDTO> serviceStatusCollection;

    public ArrayList<SAPCompanyServiceStatusDISDTO> getServiceStatusCollection() {
        return serviceStatusCollection;
    }

    public void setServiceStatusCollection(ArrayList<SAPCompanyServiceStatusDISDTO> serviceStatusCollection) {
        this.serviceStatusCollection = serviceStatusCollection;
    }

}
