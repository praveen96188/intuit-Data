package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 5:15:01 PM
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class LiabilityAdjustmentWSDTO {
    public BigDecimal amount;
    public Date effectiveDate;
    public BigDecimal taxableWages;
    public BigDecimal totalWages;

    public QbdtTransactionInfoWSDTO qbdtTransactionInfo;    
}
