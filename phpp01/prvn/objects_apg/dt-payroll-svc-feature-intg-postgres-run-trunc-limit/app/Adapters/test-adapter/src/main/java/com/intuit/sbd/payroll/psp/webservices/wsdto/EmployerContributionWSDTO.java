package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Feb 1, 2010
 * Time: 2:42:15 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class EmployerContributionWSDTO {
    public String id; // GUID
    public PayrollItemWSDTO payrollItem;
    public BigDecimal contributionAmount;
    public BigDecimal taxableWagesAmount;
    public BigDecimal totalWagesAmount;
    public BigDecimal contributionYTDAmount;
    public Long payStubOrder;

    public QbdtPaylineInfoWSDTO qbdtPaylineInfoWSDTO;
}
