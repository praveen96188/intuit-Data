package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyLawRatesHistoryDISDTO;

import javax.xml.bind.annotation.*;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryLawRateHistoryResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryLawRateHistoryResponseDISDTO",propOrder = {"companyLawRateHistoryDISDTO"})
public class QueryLawRateHistoryResponseDISDTO extends ResponseDISDTO {

    @XmlElement(name = "CompanyLawRateHistory")
    private CompanyLawRatesHistoryDISDTO companyLawRateHistoryDISDTO;

    public CompanyLawRatesHistoryDISDTO getCompanyLawRateHistoryDISDTO() {
        return companyLawRateHistoryDISDTO;
    }

    public void setCompanyLawRateHistoryDISDTO(CompanyLawRatesHistoryDISDTO companyLawRateHistoryDISDTO) {
        this.companyLawRateHistoryDISDTO = companyLawRateHistoryDISDTO;
    }

    @Override
    public void clearElements() {
        //@TODO Implement
    }

}