package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 13, 2008
 * Time: 5:57:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class EntryDetailRecordWSDTO {
    public BigDecimal amount;
    public String traceNumber;    
    public String creditDebitIndicator;
    public Date settlementDate;
    public BankAccountWSDTO bankAccount;
    public String companyId;
    public String individualName; // First name + last name
    public String mmTransactionId;  // GUID
    public Collection<BankReturnWSDTO> bankReturns;
    public boolean isBankReturnsExists= false;
}
