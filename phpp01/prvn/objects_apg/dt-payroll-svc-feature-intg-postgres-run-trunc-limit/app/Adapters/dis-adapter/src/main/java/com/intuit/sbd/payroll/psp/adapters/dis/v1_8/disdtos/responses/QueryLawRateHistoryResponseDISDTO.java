package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyLawRatesHistoryDISDTO;

import javax.xml.bind.annotation.*;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
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