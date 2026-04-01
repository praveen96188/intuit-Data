package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Mar 26, 2009
 * Time: 10:21:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class RAFEnrollmentWSDTO {
    public String id;
    public String fedTaxId;
    public String legalName;
    public String legalStreetAddress;
    public String legalCity;
    public String legalState;
    public String legalZipCode;

    public String f940TaxPeriod;
    public String f941TaxPeriod;
    public String f94xFTDPeriod;
    public String status;
    public String statusReason;
}
