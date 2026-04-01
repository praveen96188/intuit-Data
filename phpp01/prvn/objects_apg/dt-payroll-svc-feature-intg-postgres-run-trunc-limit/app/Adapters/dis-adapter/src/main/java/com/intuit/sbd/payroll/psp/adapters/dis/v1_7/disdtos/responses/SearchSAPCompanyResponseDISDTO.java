package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.SAPCompanyDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/SearchSAPCompanyResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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
