package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Nov 2, 2009
 * Time: 3:43:59 PM
 */
public class LedgerEntryWSDTO {
    public BigDecimal amount;
    public String financialTransactionId;
    public String sourceDdTransactionId;
    public Date createdDate;
    public Date settlementDate;
    public String settlementType;
    public String transactionState;
    public String transactionType;
    public String transactionCategory;
    public boolean isCredit;        
}
