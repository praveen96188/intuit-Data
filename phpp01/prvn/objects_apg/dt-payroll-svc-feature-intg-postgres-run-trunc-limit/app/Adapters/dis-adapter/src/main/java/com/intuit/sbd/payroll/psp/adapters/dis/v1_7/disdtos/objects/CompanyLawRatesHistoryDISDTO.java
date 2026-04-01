package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/CompanyLawRatesHistoryDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompanyLawRatesHistoryDISDTO",propOrder = {"companyLawRateDetails","companyLawNames"})
public class CompanyLawRatesHistoryDISDTO {
    @XmlElement(name = "CompanyLawRateDetail")
    private ArrayList<CompanyLawRateDetailDISDTO> companyLawRateDetails;

    @XmlElement(name = "CompanyLawNames")
    private ArrayList<String> companyLawNames;

    public ArrayList<CompanyLawRateDetailDISDTO> getCompanyLawRateDetails() {
        return companyLawRateDetails;
    }

    public void setCompanyLawRateDetails(ArrayList<CompanyLawRateDetailDISDTO> companyLawRateDetails) {
        this.companyLawRateDetails = companyLawRateDetails;
    }

    public ArrayList<String> getCompanyLawNames() {
        return companyLawNames;
    }

    public void setCompanyLawNames(ArrayList<String> companyLawNames) {
        this.companyLawNames = companyLawNames;
    }
}
