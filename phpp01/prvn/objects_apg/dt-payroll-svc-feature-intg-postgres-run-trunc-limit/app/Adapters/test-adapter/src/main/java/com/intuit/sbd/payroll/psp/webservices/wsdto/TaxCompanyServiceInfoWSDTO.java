package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 12/5/12
 * Time: 1:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class TaxCompanyServiceInfoWSDTO {
    public Integer lastQuarterToFile;
    public Boolean fileAnnualReturns;
    public Boolean finalAnnualReturns;
    public String w2DeliveryPref;
    public String clientPacketDeliveryPref;
    public Integer lastTaxYear;
    public Calendar lastPayrollDate;
    public Boolean inHouseW2;
    public Boolean includeOnSsaFile;
}
