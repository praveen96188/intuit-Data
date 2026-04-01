package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;

/**
 * User: dweinberg
 * Date: Oct 22, 2010
 * Time: 4:14:46 PM
 */
public class AssistedEmployeeInfoWSDTO {
    public boolean isDeceased;
    public boolean qualifiesForAeic;
    public BigDecimal fedExtraWithholding;    
    public String liveState;    
}
