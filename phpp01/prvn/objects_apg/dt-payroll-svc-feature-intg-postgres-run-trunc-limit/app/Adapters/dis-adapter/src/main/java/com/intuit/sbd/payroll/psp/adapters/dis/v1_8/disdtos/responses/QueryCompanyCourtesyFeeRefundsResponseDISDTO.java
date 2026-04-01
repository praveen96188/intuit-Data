package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.LedgerTransactionDISDTO;

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
@XmlType()
public class QueryCompanyCourtesyFeeRefundsResponseDISDTO extends ResponseDISDTO {

    @XmlElement
    private List<LedgerTransactionDISDTO> ledgerTransactions;

    public List<LedgerTransactionDISDTO> getLedgerTransactions() {
        return ledgerTransactions;
    }

    public void setLedgerTransactions(List<LedgerTransactionDISDTO> companyEvents) {
        this.ledgerTransactions = companyEvents;
    }

    public void clearElements() {
        this.ledgerTransactions = null;
    }


}
