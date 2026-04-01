package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
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
