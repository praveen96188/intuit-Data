package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Apr 10, 2008
 * Time: 2:13:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class NACHAValidationWSDTO {
    public boolean fileComparision;
    public boolean traceNumberComparision;
    public boolean psIdComparision;
    public boolean psIdEinComparision;
    public boolean overallValidation;
    public String fileType;
    public Collection<String> filevalidationErrors;
}
