package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Feb 1, 2010
 * Time: 2:36:37 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class CompensationWSDTO {
    public String id; // GUID
    public PayrollItemWSDTO payrollItem;
    public BigDecimal compensationAmount;
    public BigDecimal compensationHoursWorked;
    public BigDecimal compensationYTDAmount;
    public Long payStubOrder;

    public QbdtPaylineInfoWSDTO qbdtPaylineInfoWSDTO;
}
