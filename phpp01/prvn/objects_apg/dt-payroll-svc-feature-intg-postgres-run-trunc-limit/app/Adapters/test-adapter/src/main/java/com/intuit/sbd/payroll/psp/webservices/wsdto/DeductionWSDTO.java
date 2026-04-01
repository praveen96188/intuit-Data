package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Feb 1, 2010
 * Time: 2:41:09 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class DeductionWSDTO {
    public String id; // GUID
    public PayrollItemWSDTO payrollItem;
    public BigDecimal deductionAmount;
    public BigDecimal deductionYTDAmount;
    public Long payStubOrder;

    public QbdtPaylineInfoWSDTO qbdtPaylineInfoWSDTO;
}
