package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Apr 22, 2008
 * Time: 4:30:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class OnHoldReasonWSDTO {
    public String id; // GUID
    public String onHoldReasonCd;  // ServiceSubStatusCode.toString()
    public Date effectiveDate;
    public Date expirationDate;
    public String onHoldReasonNane; //Service

}
