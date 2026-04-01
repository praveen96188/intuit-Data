package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 4:58:07 PM
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class QbdtPaycheckInfoWSDTO {
    public String accountName;
    public String checkNumber;
    public String cleared;
    public String memo;
    public boolean onService;
    public boolean prorate;
    public String trackingClass;
    public long token;
}
