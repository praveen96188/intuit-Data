package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyAgencyYearInfoDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryCompanyAgenciesYearInfoResponseDISDTO",propOrder = {"companyAgencyYearInfoDISDTO"})
public class QueryCompanyAgenciesYearInfoResponseDISDTO extends ResponseDISDTO {

    @XmlElement(name = "CompanyAgencyYearInfo")
    private List<CompanyAgencyYearInfoDISDTO> companyAgencyYearInfoDISDTO;

    public List<CompanyAgencyYearInfoDISDTO> getCompanyAgencyYearInfoDISDTO() {
        return companyAgencyYearInfoDISDTO;
    }

    public void setCompanyAgencyYearInfoDISDTO(List<CompanyAgencyYearInfoDISDTO> companyAgencyYearInfoDISDTO) {
        this.companyAgencyYearInfoDISDTO = companyAgencyYearInfoDISDTO;
    }

    @Override
    public void clearElements() {
        //@TODO Implement
    }

}