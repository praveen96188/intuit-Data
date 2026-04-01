package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import java.math.BigDecimal;

/**
 * User: rnorian
 * Date: Apr 4, 2010
 * Time: 8:36:20 AM
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class TaxWSDTO {
    public String id;
    public String lawId;
    public BigDecimal liabilityTXAmt;
    public BigDecimal liabilityTotalWages;
    public BigDecimal liabilityTaxableWages;
    public BigDecimal liabilityYTDWages;
    public Long payStubOrder;
    public BigDecimal tipsTaxableWageAmount;
}
