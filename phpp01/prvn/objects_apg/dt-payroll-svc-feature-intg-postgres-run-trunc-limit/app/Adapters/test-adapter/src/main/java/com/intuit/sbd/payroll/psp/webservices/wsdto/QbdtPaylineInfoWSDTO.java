package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 11:40:23 AM
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class QbdtPaylineInfoWSDTO {
    public boolean expenseByJob;
    public String item;
    public String job;
    public double quantity;
    public String quantityType;
    public double rate;
    public String rateType;
    public String trackingClass;
    public String wcCode;
}
