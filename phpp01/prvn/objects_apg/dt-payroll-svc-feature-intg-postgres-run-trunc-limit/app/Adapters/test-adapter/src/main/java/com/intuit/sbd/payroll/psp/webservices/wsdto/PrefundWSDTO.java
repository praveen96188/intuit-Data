package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Sep 30, 2009
 * Time: 8:22:45 AM
 */
public class PrefundWSDTO {
    public BigDecimal originalTransactionAmount;
    public BigDecimal newTransactionAmount;
    public String transactionType;
    public BigDecimal originalTaxTransactionAmount;
    public BigDecimal newTaxTransactionAmount;
}
