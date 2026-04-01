package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyAgencyYearInfoDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryCompanyAgenciesYearInfoResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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