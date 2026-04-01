package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 28, 2008
 * Time: 12:01:47 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class ACHDetailRecordWSDTO {
    public BigDecimal amount;
    public String traceNumber;
    public String recordData;
    public String txpRecordData;
}
