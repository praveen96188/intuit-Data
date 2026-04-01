package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyDepositFrequencyDISDTO;

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
@XmlType(name = "QueryDepositFrequencyHistoryResponseDISDTO",propOrder = {"companyDepositFrequencies"})
public class QueryDepositFrequencyHistoryResponseDISDTO extends ResponseDISDTO {

    @XmlElement(name = "CompanyPaymentMethods")
    private List<CompanyDepositFrequencyDISDTO> companyDepositFrequencies;

    public List<CompanyDepositFrequencyDISDTO> getCompanyDepositFrequencies() {
        return companyDepositFrequencies;
    }

    public void setCompanyDepositFrequencies(List<CompanyDepositFrequencyDISDTO> companyDepositFrequencies) {
        this.companyDepositFrequencies = companyDepositFrequencies;
    }

    @Override
    public void clearElements() {
        //@TODO Implement
    }

}