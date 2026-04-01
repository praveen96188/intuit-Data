package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 19, 2008
 * Time: 9:09:17 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class BankAccountWSDTO {
    public String accountNumber;
    public String routingNumber;
    public String bankName;
    public String bankAccountType;
    public String bankAccountOwnerType;
    public String intuitBankAccountDesc;

}

