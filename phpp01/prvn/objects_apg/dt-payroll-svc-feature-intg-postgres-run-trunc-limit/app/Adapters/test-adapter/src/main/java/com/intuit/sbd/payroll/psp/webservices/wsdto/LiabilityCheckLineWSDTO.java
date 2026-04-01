package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 3:37:48 PM
 */
public class LiabilityCheckLineWSDTO {
    public BigDecimal amount;
    public QbdtTransactionInfoWSDTO qbdtTransactionInfoWSDTO;
    public String companyPayrollItem; //id
}
