package com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Sriram Nutakki
 * Date created: 8/14/13
 */
@XmlRootElement(name = "CompanyList")
public class CompanyListDTO {

    private List<CompanyDTO> companies;

    @XmlElement(name="Company")
    public List<CompanyDTO> getCompanies() {
        return companies;
    }

    public void setCompanies(List<CompanyDTO> companies) {
        this.companies = companies;
    }

    public void addCompany(CompanyDTO company) {
        if (this.companies == null) {
            this.companies = new ArrayList<CompanyDTO>();
        }
        this.companies.add(company);
    }
}
