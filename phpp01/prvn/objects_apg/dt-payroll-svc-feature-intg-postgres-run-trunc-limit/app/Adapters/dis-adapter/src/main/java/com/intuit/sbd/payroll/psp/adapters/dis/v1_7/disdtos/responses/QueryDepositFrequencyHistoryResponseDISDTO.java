package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyDepositFrequencyDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryDepositFrequencyHistoryResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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