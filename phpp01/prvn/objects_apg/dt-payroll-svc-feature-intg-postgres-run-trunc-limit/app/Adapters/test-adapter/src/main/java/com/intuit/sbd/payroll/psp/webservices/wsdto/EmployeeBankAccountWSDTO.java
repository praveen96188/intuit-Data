package com.intuit.sbd.payroll.psp.webservices.wsdto;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Aug 11, 2008
 * Time: 2:50:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmployeeBankAccountWSDTO {
    public BankAccountWSDTO bankAccount;    //bank account associated with ee bank account
    public String id;                       //guid
    public String sourceBankAccountId;      //source employee bank account id
    public String statusCode;               //bank account status code

    public double amount;
    public String amountType;     
}
