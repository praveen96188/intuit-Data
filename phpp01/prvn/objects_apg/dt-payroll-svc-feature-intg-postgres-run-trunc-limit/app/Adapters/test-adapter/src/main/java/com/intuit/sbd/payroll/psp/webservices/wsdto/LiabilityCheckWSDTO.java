package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 3:29:53 PM
 */
public class LiabilityCheckWSDTO {
    public BigDecimal amount;
    public boolean isRefund;
    public boolean isVoid;
    public Date periodEndDate;
    public String sourceId;
    public Date transactionDate;

    public QbdtTransactionInfoWSDTO qbdtTransactionInfo;
    public Collection<LiabilityCheckLineWSDTO> liabilityCheckLines;

}
