package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 19, 2008
 * Time: 9:10:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class BankReturnWSDTO {
    public String id; // GUID
    public String transactionId;  // GUID
    public String sourceEmployeeId;
    public String employeeDisplayName; // First name + last name
    public String traceNumber;
    public String bankReturnCd;
    public String returnStatus;
    public Date statusChangeDate;
    public String routingNumber;
    public String accountNumber;
    public String accountType;
    public String description;
    public Date createdDate;
}
