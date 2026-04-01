package com.intuit.sbd.payroll.psp.webservices.wsdto;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 4, 2008
 * Time: 10:31:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyBankAccountWSDTO {
    public String id; // GUID
    public String sourceBankAccountID;  //Source Company Bank Account Id
    public String statusCode; //Status Cd;
    public long verfyRetryCount;
    public BankAccountWSDTO bankAccount;   
}
