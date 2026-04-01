package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Aug 20, 2008
 * Time: 3:03:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class GEMSValidationWSDTO {
    public boolean fileComparision;
    public String batchID;
    public Collection<String> filevalidationErrors;
}
