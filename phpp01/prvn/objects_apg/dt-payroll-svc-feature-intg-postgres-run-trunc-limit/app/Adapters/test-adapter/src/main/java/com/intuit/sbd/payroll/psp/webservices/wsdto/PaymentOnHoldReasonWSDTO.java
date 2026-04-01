package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

/**
 * User: rnorian
 * Date: Jan 28, 2011
 * Time: 4:49:26 PM
 */
public class PaymentOnHoldReasonWSDTO {
    public String onHoldReasonCd;
    public Date effectiveDate;
    public Date expirationDate;
}
