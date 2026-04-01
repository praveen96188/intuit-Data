package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyServiceUpdatedDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Response WS DTO for the query company events request
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class QueryUpdatedCompaniesResponseDISDTO extends ResponseDISDTO {

    @XmlElement
    private List<CompanyServiceUpdatedDISDTO> companyServiceUpdated;

    public List<CompanyServiceUpdatedDISDTO> getCompanies() {
        return companyServiceUpdated;
    }

    public void setCompanies(List<CompanyServiceUpdatedDISDTO> companies) {
        this.companyServiceUpdated = companies;
    }

    public void clearElements() {
        this.companyServiceUpdated = null;
    }

}
