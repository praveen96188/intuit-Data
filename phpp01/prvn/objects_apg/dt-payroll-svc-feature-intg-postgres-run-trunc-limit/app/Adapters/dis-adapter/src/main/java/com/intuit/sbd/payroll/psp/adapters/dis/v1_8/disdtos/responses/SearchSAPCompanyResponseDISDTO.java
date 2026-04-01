package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.SAPCompanyDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Response WS DTO for the query company request
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchSAPCompanyResponseDISDTO",propOrder = {"companies"})
public class SearchSAPCompanyResponseDISDTO extends ResponseDISDTO {

    @XmlElement(name = "Company")
    private List<SAPCompanyDISDTO> companies;

    public List<SAPCompanyDISDTO> getCompanies() {
        return companies;
    }

    public void setCompanies(List<SAPCompanyDISDTO> companies) {
        this.companies = companies;
    }

    public void clearElements() {
        this.companies = null;
    }
}
