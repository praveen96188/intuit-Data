package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 3:33:13 PM
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class QbdtTransactionInfoWSDTO {
    public String accountName;
    public String agencyName;
    public String cleared;
    public boolean isDeleted;
    public String memo;
    public boolean onService;
    public String referenceNumber;
    public String trackingClass;
    
}
