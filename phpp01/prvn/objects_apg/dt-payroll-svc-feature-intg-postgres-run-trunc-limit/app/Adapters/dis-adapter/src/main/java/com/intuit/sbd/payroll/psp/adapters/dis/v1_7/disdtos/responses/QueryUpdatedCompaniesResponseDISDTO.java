package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyServiceUpdatedDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.SAPCompanyDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryUpdatedCompaniesResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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
