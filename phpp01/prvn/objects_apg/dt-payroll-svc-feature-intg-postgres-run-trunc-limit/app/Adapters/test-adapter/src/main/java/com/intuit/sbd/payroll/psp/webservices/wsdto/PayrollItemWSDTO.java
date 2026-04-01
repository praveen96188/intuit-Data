package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Feb 1, 2010
 * Time: 2:39:16 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class PayrollItemWSDTO {
    public String payrollItemCode;
    public String sourcePayrollItemId;
    public String sourceDescription;
}
